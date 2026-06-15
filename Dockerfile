# Etap 1: Budowanie aplikacji
FROM gradle:8.5-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Naprawa uprawnień do wykonywania skryptu gradlew (rozwiązuje błąd 126)
RUN chmod +x gradlew

# Budujemy dystrybucję backendu
RUN ./gradlew :backend:installDist --no-daemon

# Etap 2: Uruchamianie aplikacji
FROM eclipse-temurin:11-jre
EXPOSE 8080

# Kopiujemy pliki z etapu budowania
COPY --from=build /home/gradle/src/backend/build/install/backend /app
WORKDIR /app

# Naprawa uprawnień do skryptu startowego wewnątrz obrazu
RUN chmod +x bin/backend

# Uruchamiamy aplikację
ENTRYPOINT ["./bin/backend"]
