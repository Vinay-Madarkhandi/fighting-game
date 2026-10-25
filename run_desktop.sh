#!/usr/bin/env bash
set -e

# Pinned JDK 21 compatible with Gradle 8.7 & LibGDX
if [ -d "/home/vinay/.jdks/ms-21.0.9" ]; then
    export JAVA_HOME="/home/vinay/.jdks/ms-21.0.9"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# Auto-detect Linux display environment (Wayland / Xwayland / X11)
export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-/run/user/$(id -u)}"

if [ -S "$XDG_RUNTIME_DIR/wayland-0" ]; then
    export WAYLAND_DISPLAY="${WAYLAND_DISPLAY:-wayland-0}"
fi

if [ -z "$DISPLAY" ]; then
    AUTH_FILE=$(ls "$XDG_RUNTIME_DIR"/.mutter-Xwaylandauth* 2>/dev/null | head -n 1)
    if [ -n "$AUTH_FILE" ]; then
        export DISPLAY=":1"
        export XAUTHORITY="${XAUTHORITY:-$AUTH_FILE}"
    else
        export DISPLAY=":0"
    fi
fi

exec ./gradlew :desktop:run "$@"
