# Etap 1: Budowanie aplikacji
FROM gradle:8.5-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Budujemy dystrybucję backendu
RUN ./gradlew :backend:installDist --no-daemon

# Etap 2: Uruchamianie aplikacji
# Używamy nowszego i wspieranego obrazu Eclipse Temurin zamiast starego openjdk
FROM eclipse-temurin:11-jre
EXPOSE 8080
# Kopiujemy pliki z etapu budowania
COPY --from=build /home/gradle/src/backend/build/install/backend /app
WORKDIR /app
# Uruchamiamy skrypt startowy wygenerowany przez Gradle
ENTRYPOINT ["./bin/backend"]
