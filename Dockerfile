# Etap 1: Budowanie aplikacji
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Naprawa gradlew i budowanie
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew :backend:installDist --no-daemon -x test

# Etap 2: Uruchamianie aplikacji
# Używamy JRE 17, aby uniknąć błędów LinkageError z nowoczesnymi bibliotekami
FROM eclipse-temurin:17-jre
EXPOSE 8080

COPY --from=build /home/gradle/src/backend/build/install/backend /app
WORKDIR /app

# Jawne wskazanie wszystkich jarów i klasy startowej
ENTRYPOINT ["java", "-cp", "lib/*", "com.pam.waterio.ApplicationKt"]
