# Dokumentacja API - WaterIO Backend

Backend aplikacji WaterIO został zbudowany w oparciu o framework **Ktor**. Obsługuje autoryzację JWT, bazę danych SQL (H2) oraz pełną logikę nawodnienia.

## Informacje ogólne
*   **Base URL:** `http://localhost:8080` (dla emulatora `http://10.0.2.2:8080`)
*   **Content-Type:** `application/json`
*   **Autoryzacja:** Większość endpointów wymaga nagłówka: `Authorization: Bearer <token_jwt>`

---

## 1. Moduł Autoryzacji

### Rejestracja
`POST /register`
*   **Body:**
    ```json
    {
      "email": "user@example.com",
      "password": "twoje_haslo"
    }
    ```
*   **Odpowiedź (201 Created):** `{"status": "User registered"}`
*   **Błędy:** 409 Conflict (Email zajęty).

### Logowanie
`POST /login`
*   **Body:** Takie samo jak przy rejestracji.
*   **Odpowiedź (200 OK):** 
    ```json
    {
      "token": "eyJhbGciOiJIUzI1..."
    }
    ```
*   **Błędy:** 401 Unauthorized (Błędne dane).

---

## 2. Zarządzanie Nawodnieniem (Zabezpieczone JWT)

### Pobieranie historii użytkownika
`GET /water`
*   **Odpowiedź (200 OK):**
    ```json
    [
      { "id": "uuid", "amountMl": 250, "timestamp": 1718115000000 },
      { "id": "uuid", "amountMl": 500, "timestamp": 1718116000000 }
    ]
    ```

### Dodawanie wody (Wspiera Offline-First)
`POST /water`
*   **Body:**
    ```json
    {
      "amountMl": 250,
      "timestamp": 1718115000000
    }
    ```
    *(Pole `timestamp` jest opcjonalne - jeśli brak, serwer nada obecny czas)*
*   **Odpowiedź (201 Created):** Pełny obiekt zapisanego wpisu.

### Usuwanie wpisu
`DELETE /water/{id}`
*   **Odpowiedź (200 OK):** `{"status": "Deleted"}`

---

## 3. Cel Dzienny i Profil (Zabezpieczone JWT)

### Pobieranie celu
`GET /user/goal`
*   **Odpowiedź:** `{"goalMl": 2000}`

### Aktualizacja celu
`POST /user/goal`
*   **Body:** `{"goalMl": 2500}`
*   **Odpowiedź:** `{"goalMl": 2500}`

---

## 4. Statystyki i Grywalizacja (Zabezpieczone JWT)

### Statystyki tygodniowe (do wykresów)
`GET /stats`
*   **Odpowiedź:** Lista sum mililitrów z ostatnich 7 dni, zgrupowana po dacie.
    ```json
    [
      { "date": "2024-06-12", "totalMl": 1500 },
      { "date": "2024-06-11", "totalMl": 2250 }
    ]
    ```

### Licznik Streak (Dni z rzędu)
`GET /streak`
*   **Odpowiedź:**
    ```json
    { "streak": 5 }
    ```
*   **Logika:** Liczy ile dni z rzędu (włącznie z dzisiejszym) użytkownik osiągnął swój `goalMl`.

---

## Modele danych (Kotlin)
```kotlin
data class WaterEntry(val id: String? = null, val amountMl: Int, val timestamp: Long? = null)
data class AuthRequest(val email: String, val password: String)
data class DailyGoal(val goalMl: Int)
data class DailyStat(val date: String, val totalMl: Int)
```
