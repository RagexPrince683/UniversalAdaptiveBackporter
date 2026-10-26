# Sourced by wrappers. Find a full JDK of the required major version.
select_java() {
    required=$1
    eval "explicit=\${JAVA${required}_HOME:-}"
    for candidate in "$explicit" "${JAVA_HOME:-}" "$HOME"/.jdks/* /usr/lib/jvm/* /opt/java/* /Library/Java/JavaVirtualMachines/*/Contents/Home; do
        [ -f "$candidate/release" ] && [ -x "$candidate/bin/javac" ] || continue
        actual=$(sed -n 's/^JAVA_VERSION="\([^" ]*\)".*/\1/p' "$candidate/release")
        case "$actual" in 1.8.*) major=8 ;; *) major=${actual%%.*}; major=${major%%-*} ;; esac
        if [ "$major" = "$required" ]; then
            JAVA_HOME=$candidate; export JAVA_HOME
            PATH="$JAVA_HOME/bin:$PATH"; export PATH
            return
        fi
    done
    echo "JDK $required is required to run this wrapper. Install it or set JAVA${required}_HOME." >&2
    return 1
}
