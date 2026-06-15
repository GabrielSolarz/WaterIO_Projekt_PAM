# Etap 1: Budowanie aplikacji
FROM gradle:8.5-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Naprawa końcówek linii Windows -> Linux dla skryptu Gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Budujemy dystrybucję backendu
# Używamy -Pandroid.skip, aby uniknąć błędów związanych z brakiem SDK Androida na serwerze
RUN ./gradlew :backend:installDist --no-daemon -x test

# Etap 2: Uruchamianie aplikacji
FROM eclipse-temurin:11-jre
EXPOSE 8080

# Kopiujemy pliki z etapu budowania
COPY --from=build /home/gradle/src/backend/build/install/backend /app
WORKDIR /app

# Naprawa końcówek linii w wygenerowanym skrypcie startowym
RUN sed -i 's/\r$//' bin/backend && chmod +x bin/backend

# Uruchamiamy aplikację
ENTRYPOINT ["./bin/backend"]
