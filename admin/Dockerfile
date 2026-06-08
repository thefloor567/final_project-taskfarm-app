FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY user/pom.xml user/pom.xml
COPY admin/pom.xml admin/pom.xml

RUN mvn dependency:go-offline -pl admin -am -q

COPY common/src common/src
COPY admin/src admin/src

RUN mvn clean package -pl admin -am -DskipTests -q


FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system team4 \
    && useradd --system --gid team4 --home-dir /app --shell /usr/sbin/nologin team4

COPY --from=builder --chown=team4:team4 /app/admin/target/taskfarm-admin.jar /app/app.jar

USER team4

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8081/admin/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
