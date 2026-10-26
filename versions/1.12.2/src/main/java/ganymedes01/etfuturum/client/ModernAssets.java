package ganymedes01.etfuturum.client;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;

/** Client-only Mojang archive acquisition, performed during startup before resource loading. */
final class ModernAssets {
    private ModernAssets() { }
    static File prepare(File gameDirectory) throws Exception {
        String override = System.getProperty("uab.assets");
        if (override != null) {
            File file = new File(override);
            try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(file)) {
                java.util.zip.ZipEntry marker = zip.getEntry("version.json");
                if (marker == null) throw new IOException("The offline resource archive has no Minecraft version marker");
                try (Reader reader = new InputStreamReader(zip.getInputStream(marker), "UTF-8")) {
                    if (!new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString().equals("1.21.11")) throw new IOException("Offline UAB resources must be the Minecraft 1.21.11 client archive");
                }
            }
            return file;
        }
        File directory = new File(gameDirectory, "assets/uab");
        Files.createDirectories(directory.toPath());
        File archive = new File(directory, "minecraft-1.21.11-client.jar"), metadata = new File(directory, "minecraft-1.21.11.json");
        JsonObject version;
        if (metadata.isFile()) try (Reader reader = new InputStreamReader(new FileInputStream(metadata), "UTF-8")) { version = new JsonParser().parse(reader).getAsJsonObject(); }
        else {
            JsonObject manifest = json("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
            String url = null;
            for (JsonElement entry : manifest.getAsJsonArray("versions")) if (entry.getAsJsonObject().get("id").getAsString().equals("1.21.11")) url = entry.getAsJsonObject().get("url").getAsString();
            if (url == null) throw new IOException("Mojang metadata does not contain Minecraft 1.21.11");
            version = json(url);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(metadata), "UTF-8")) { new Gson().toJson(version, writer); }
        }
        JsonObject client = version.getAsJsonObject("downloads").getAsJsonObject("client");
        String expected = client.get("sha1").getAsString();
        if (archive.isFile() && hash(archive).equals(expected)) return archive;
        LogManager.getLogger("UAB").info("Downloading Minecraft 1.21.11 client resources from Mojang");
        Path temporary = Files.createTempFile(directory.toPath(), "minecraft-", ".part");
        try {
            try (InputStream input = open(client.get("url").getAsString())) { Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING); }
            if (!hash(temporary.toFile()).equals(expected)) throw new IOException("Mojang client resource checksum mismatch");
            Files.move(temporary, archive.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
        return archive;
    }
    static InputStream open(String url) throws IOException {
        URL address = new URL(url);
        if (!address.getProtocol().equals("https") || !(address.getHost().endsWith(".mojang.com") || address.getHost().equals("resources.download.minecraft.net"))) throw new IOException("Unrecognized Mojang resource host");
        URLConnection connection = address.openConnection(); connection.setConnectTimeout(15000); connection.setReadTimeout(60000);
        return connection.getInputStream();
    }
    static JsonObject json(String url) throws IOException { try (Reader reader = new InputStreamReader(open(url), "UTF-8")) { return new JsonParser().parse(reader).getAsJsonObject(); } }
    static String hash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) { byte[] buffer = new byte[65536]; int size; while ((size = input.read(buffer)) != -1) digest.update(buffer, 0, size); }
        StringBuilder value = new StringBuilder(); for (byte b : digest.digest()) value.append(String.format(java.util.Locale.ROOT, "%02x", b & 255)); return value.toString();
    }
}
