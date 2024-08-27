# DisableDockerDetector "liberty-maven-plugin does not support docker buildx-build. See https://github.com/OpenLiberty/ci.maven/issues/1589"
# open liberty base image
FROM icr.io/appcafe/open-liberty:23.0.0.3-kernel-slim-java17-openj9-ubi

# Add config
COPY --chown=1001:0 liberty/wlp/usr/servers/defaultServer/server.xml /config/server.xml

# This script will add the requested XML snippets to enable Liberty features and grow image to be fit-for-purpose using featureUtility.
# Only available in 'kernel-slim'. The 'full' tag already includes all features for convenience.
RUN features.sh

# Add app and jdbc driver
COPY --chown=1001:0 javaee-cafe.war /config/apps/
COPY --chown=1001:0 liberty/wlp/usr/shared/resources/mssql-jdbc-8.2.2.jre8.jar /opt/ol/wlp/usr/shared/resources/

# This script will add the requested XML snippets, grow image to be fit-for-purpose and apply interim fixes
RUN configure.sh
