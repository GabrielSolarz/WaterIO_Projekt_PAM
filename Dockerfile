# Etap 1: Budowanie aplikacji
FROM gradle:8.5-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Naprawa gradlew i budowanie dystrybucji
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew :backend:installDist --no-daemon -x test

# Etap 2: Uruchamianie aplikacji
FROM eclipse-temurin:11-jre
EXPOSE 8080

# Kopiujemy pliki (w tym katalog lib z bibliotekami jar)
COPY --from=build /home/gradle/src/backend/build/install/backend /app
WORKDIR /app

# URUCHAMIAMY JAVĘ BEZPOŚREDNIO
# To omija skrypt shellowy i rozwiązuje problem błędnych parametrów (status 1)
ENTRYPOINT ["java", "-cp", "lib/*", "com.pam.waterio.ApplicationKt"]
