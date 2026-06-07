1. Autoryzacja i Profil Użytkownika (Moduł API) 

1.1 Rejestracja pomyślna: Wpisz unikalny e-mail i hasło (min. 6 znaków). Kliknij "Zarejestruj". 

Oczekiwany rezultat: Aplikacja przechodzi do ekranu logowania lub ekranu głównego. Użytkownik pojawia się w bazie danych. 

1.2 Rejestracja błędna: Spróbuj zarejestrować konto na e-mail, który już istnieje w systemie. 

Oczekiwany rezultat: Pojawia się czytelny komunikat błędu (np. "Ten e-mail jest już zajęty"). Aplikacja nie zamyka się awaryjnie. 

1.3 Logowanie: Wpisz poprawne dane założonego konta i kliknij "Zaloguj". 

Oczekiwany rezultat: Aplikacja płynnie przechodzi na ekran główny. Token JWT zostaje zapisany w pamięci. 

1.4 Wylogowanie: Na ekranie profilu kliknij "Wyloguj". 

Oczekiwany rezultat: Powrót do ekranu logowania. Użycie przycisku "Wstecz" w telefonie nie pozwala na powrót do ekranu głównego bez ponownego logowania. 

2. Główny Dashboard i Operacje CRUD 

2.1 Ustalenie celu: Wejdź w ustawienia, zmień cel dzienny z domyślnego na 3000 ml. 

Oczekiwany rezultat: Cel zapisuje się i natychmiast odświeża wartość na pasku postępu na ekranie głównym. 

2.2 Szybkie dodawanie (Szklanka): Kliknij przycisk "+ 250 ml". 

Oczekiwany rezultat: Pasek postępu rośnie natychmiast. Wyświetla się krótki komunikat (Snackbar/Toast) "Dodano 250 ml". 

2.3 Własna objętość: Wybierz opcję "Inna ilość", wpisz 150 i zatwierdź. 

Oczekiwany rezultat: Klawiatura numeryczna chowa się poprawnie, a 150 ml zostaje dodane do dziennego bilansu. 

3. Historia i Wykresy 

3.1 Widok historii: Przejdź do zakładki "Historia". 

Oczekiwany rezultat: Wyświetla się lista dzisiejszych wpisów posortowana chronologicznie (od najnowszego), zgodna z tym, co wyklikano w teście nr 2. 

3.2 Usuwanie pomyłki: Usuń ostatni dodany wpis (np. 150 ml) za pomocą ikony kosza lub gestu swipe. 

Oczekiwany rezultat: Wpis znika z listy. Po powrocie na ekran główny, pasek postępu cofa się o 150 ml. 

3.3 Wykresy pobierane z API: Przejdź do ekranu "Statystyki/Wykresy". 

Oczekiwany rezultat: Pojawia się wskaźnik ładowania (loading spinner), a po chwili wyświetla się wykres słupkowy na podstawie danych pobranych z backendu. 

4. Funkcje Natywne i Obsługa Błędów (Kluczowe na zaliczenie) 

4.1 Działanie w trybie Offline (Baza Room): Całkowicie wyłącz Wi-Fi oraz transmisję danych w telefonie. Będąc na ekranie głównym, dodaj 250 ml wody. 

Oczekiwany rezultat: Aplikacja NIE WYRZUCA błędu ani się nie zacina. Pasek postępu rośnie. Wyświetla się komunikat "Zapisano offline". 

4.2 Synchronizacja po powrocie zasięgu: Włącz ponownie Wi-Fi w telefonie. Odczekaj chwilę. 

Oczekiwany rezultat: Aplikacja w tle wysyła zapisane offline 250 ml do bazy danych na backendzie. (Można to zweryfikować, sprawdzając historię na innym urządzeniu lub sprawdzając logi serwera). 

4.3 Powiadomienia Push (Przypomnienia): Zmień czas systemowy w telefonie o 3 godziny do przodu (lub na czas zdefiniowany w interwałach aplikacji) bez dodawania wody. 

Oczekiwany rezultat: Na pasku powiadomień Androida pojawia się natywne powiadomienie przypominające o wypiciu wody (z ikoną aplikacji i dźwiękiem/wibracją). 

4.4 Brak połączenia przy logowaniu (Obsługa stanów): Wyłącz internet i spróbuj się zalogować. 

Oczekiwany rezultat: Zamiast zablokowania aplikacji, pojawia się czytelny komunikat: "Brak połączenia z internetem. Sprawdź ustawienia sieci". 
