FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY user/pom.xml user/pom.xml
COPY admin/pom.xml admin/pom.xml

RUN mvn dependency:go-offline -pl user -am -q

COPY common/src common/src
COPY user/src user/src

RUN mvn clean package -pl user -am -DskipTests -q


FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system team4 \
    && useradd --system --gid team4 --home-dir /app --shell /usr/sbin/nologin team4

COPY --from=builder --chown=team4:team4 /app/user/target/taskfarm-user.jar /app/app.jar

USER team4

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
