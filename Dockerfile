# Etap 1: Budowanie
FROM gradle:8.5-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew :backend:installDist --no-daemon

# Etap 2: Uruchamianie
FROM openjdk:11-jre-slim
EXPOSE 8080
COPY --from=build /home/gradle/src/backend/build/install/backend /app
WORKDIR /app
ENTRYPOINT ["./bin/backend"]
