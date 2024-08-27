# DisableDockerDetector "liberty-maven-plugin does not support docker buildx-build. See https://github.com/OpenLiberty/ci.maven/issues/1589"
# open liberty base image
FROM icr.io/appcafe/open-liberty:23.0.0.12-kernel-slim-java17-openj9-ubi

ENV HUGGING_FACE_API_KEY=${param.hugging.face.api.key}

# Add config
COPY --chown=1001:0 liberty/wlp/usr/servers/defaultServer/server.xml /config/server.xml

# This script will add the requested XML snippets to enable Liberty features and grow image to be fit-for-purpose using featureUtility.
# Only available in 'kernel-slim'. The 'full' tag already includes all features for convenience.
RUN features.sh

# Add app and jdbc driver
COPY --chown=1001:0 jakartaee-microprofile-example.war /config/apps/

# This script will add the requested XML snippets, grow image to be fit-for-purpose and apply interim fixes
RUN configure.sh
