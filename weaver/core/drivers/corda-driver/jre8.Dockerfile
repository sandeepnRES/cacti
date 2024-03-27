FROM debian:12

CMD ["bash"]
RUN /bin/sh -c set -eux; apt-get update; apt-get install -y --no-install-recommends ca-certificates curl netbase wget ; rm -rf /var/lib/apt/lists/*
RUN /bin/sh -c set -ex; if ! command -v gpg > /dev/null; then apt-get update; apt-get install -y --no-install-recommends gnupg dirmngr ; rm -rf /var/lib/apt/lists/*; fi
RUN /bin/sh -c set -eux; apt-get update; apt-get install -y --no-install-recommends bzip2 unzip xz-utils fontconfig libfreetype6 ca-certificates p11-kit ; rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/local/openjdk-8
RUN /bin/sh -c { echo '#/bin/sh'; echo 'echo "$JAVA_HOME"'; } > /usr/local/bin/docker-java-home && chmod +x /usr/local/bin/docker-java-home && [ "$JAVA_HOME" = "$(docker-java-home)" ] # backwards compatibility
ENV PATH=/usr/local/openjdk-8/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
ENV LANG=C.UTF-8
ENV JAVA_VERSION=8u402

RUN /bin/sh -c set -eux; arch="$(dpkg --print-architecture)"; case "$arch" in 'amd64') downloadUrl='https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u402-b06/OpenJDK8U-jre_x64_linux_hotspot_8u402b06.tar.gz'; ;; 'arm64') downloadUrl='https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u402-b06/OpenJDK8U-jre_aarch64_linux_hotspot_8u402b06.tar.gz'; ;; *) echo >&2 "error: unsupported architecture: '$arch'"; exit 1 ;; esac; wget --progress=dot:giga -O openjdk.tgz "$downloadUrl"; wget --progress=dot:giga -O openjdk.tgz.asc "$downloadUrl.sign"; export GNUPGHOME="$(mktemp -d)"; gpg --batch --keyserver keyserver.ubuntu.com --recv-keys EAC843EBD3EFDB98CC772FADA5CD6035332FA671; gpg --batch --keyserver keyserver.ubuntu.com --keyserver-options no-self-sigs-only --recv-keys CA5F11C6CE22644D42C6AC4492EF8D39DC13168F; gpg --batch --list-sigs --keyid-format 0xLONG CA5F11C6CE22644D42C6AC4492EF8D39DC13168F | tee /dev/stderr | grep '0xA5CD6035332FA671' | grep 'Andrew Haley'; gpg --batch --verify openjdk.tgz.asc openjdk.tgz; gpgconf --kill all; rm -rf "$GNUPGHOME"; mkdir -p "$JAVA_HOME"; tar --extract --file openjdk.tgz --directory "$JAVA_HOME" --strip-components 1 --no-same-owner ; rm openjdk.tgz*; { echo '#!/usr/bin/env bash'; echo 'set -Eeuo pipefail'; echo 'trust extract --overwrite --format=java-cacerts --filter=ca-anchors --purpose=server-auth "$JAVA_HOME/lib/security/cacerts"'; } > /etc/ca-certificates/update.d/docker-openjdk; chmod +x /etc/ca-certificates/update.d/docker-openjdk; /etc/ca-certificates/update.d/docker-openjdk; find "$JAVA_HOME/lib" -name '*.so' -exec dirname '{}' ';' | sort -u > /etc/ld.so.conf.d/docker-openjdk.conf; ldconfig; java -version

