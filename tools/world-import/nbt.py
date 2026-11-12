"""Bounded Java NBT reader/writer, including modified UTF-8 and long arrays."""
from dataclasses import dataclass
from io import BytesIO
import struct

MAX_BYTES = 64 * 1024 * 1024


@dataclass
class Tag:
    kind: int
    value: object


def string(value):
    return Tag(8, value)


def compound(**values):
    return Tag(10, values)


def sequence(kind, values=()):
    return Tag(9, (kind, list(values)))


def read(data):
    if len(data) > MAX_BYTES:
        raise ValueError("NBT exceeds 64 MiB limit")
    stream = BytesIO(data)

    def take(size):
        if size < 0 or size > MAX_BYTES:
            raise ValueError("Invalid NBT length")
        result = stream.read(size)
        if len(result) != size:
            raise ValueError("Truncated NBT")
        return result

    def number(fmt):
        return struct.unpack(">" + fmt, take(struct.calcsize(fmt)))[0]

    def text():
        raw = take(number("H")).replace(b"\xc0\x80", b"\0")
        return raw.decode("utf-8", "surrogatepass").encode("utf-16", "surrogatepass").decode("utf-16")

    def payload(kind, depth=0):
        if depth > 64:
            raise ValueError("NBT exceeds depth limit")
        if 1 <= kind <= 6:
            return Tag(kind, number({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[kind]))
        if kind == 8:
            return string(text())
        if kind in (7, 11, 12):
            count = number("i")
            size = {7: 1, 11: 4, 12: 8}[kind]
            raw = take(count * size)
            return Tag(kind, raw if kind == 7 else list(struct.unpack(">" + {11: "i", 12: "q"}[kind] * count, raw)))
        if kind == 9:
            child, count = number("B"), number("i")
            if count < 0 or count > MAX_BYTES // 4 or child == 0 and count:
                raise ValueError("Invalid NBT list")
            return sequence(child, (payload(child, depth + 1) for _ in range(count)))
        if kind == 10:
            values = {}
            while True:
                child = number("B")
                if child == 0:
                    return Tag(10, values)
                name = text()
                if name in values:
                    raise ValueError("Duplicate NBT compound key")
                values[name] = payload(child, depth + 1)
        raise ValueError("Unsupported NBT type: " + str(kind))

    kind = number("B")
    if kind != 10:
        raise ValueError("NBT root must be a compound")
    name, result = text(), payload(kind)
    if stream.read(1):
        raise ValueError("Trailing NBT data")
    return name, result


def write(root, name=""):
    def number(fmt, value):
        return struct.pack(">" + fmt, value)

    def text(value):
        # Java's writeUTF encodes supplementary characters as two surrogate code units.
        units = value.encode("utf-16-be", "surrogatepass")
        raw = "".join(chr(int.from_bytes(units[i:i+2], "big")) for i in range(0, len(units), 2)).encode("utf-8", "surrogatepass").replace(b"\0", b"\xc0\x80")
        return number("H", len(raw)) + raw

    def payload(tag):
        kind, value = tag.kind, tag.value
        if 1 <= kind <= 6:
            return number({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[kind], value)
        if kind == 8:
            return text(value)
        if kind in (7, 11, 12):
            return number("i", len(value)) + (bytes(value) if kind == 7 else b"".join(number("i" if kind == 11 else "q", n) for n in value))
        if kind == 9:
            child, values = value
            if any(v.kind != child for v in values):
                raise ValueError("Mixed NBT list types")
            return number("B", child) + number("i", len(values)) + b"".join(payload(v) for v in values)
        if kind == 10:
            return b"".join(number("B", child.kind) + text(key) + payload(child) for key, child in value.items()) + b"\0"
        raise ValueError("Invalid NBT type")

    return number("B", root.kind) + text(name) + payload(root)
