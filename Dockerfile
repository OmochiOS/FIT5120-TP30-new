FROM eclipse-temurin:21-jre-jammy

ENV APP_HOME=/opt/amityspace
ENV SERVER_PORT=8080
ENV JAVA_OPTS=""

WORKDIR ${APP_HOME}

COPY deploy/release/AmitySpace-0.0.1-SNAPSHOT.jar ${APP_HOME}/app.jar

EXPOSE 8080

ENTRYPOINT ["/bin/sh", "-lc", "exec java ${JAVA_OPTS} -jar /opt/amityspace/app.jar --server.port=${SERVER_PORT} --server.address=0.0.0.0"]
