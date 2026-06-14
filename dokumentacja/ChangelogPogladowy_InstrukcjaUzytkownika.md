# Changelog: 

## [v1.0.1] - 2026-06-13 (Aktualna wersja testowa w Google Play)

Dodano czytelne daty w zakładce "Historia", umożliwiające precyzyjne przeglądanie logów nawodnienia.

Wprowadzono drobne zmiany konieczne do dodania aplikacji do Sklepu Play

## [v1.0.0] - 2026-06-12 (Wydanie główne / Release Candidate)
Stabilizacja aplikacji, usunięcie krytycznych błędów i przygotowanie do testów w sklepie.

Ostatecznie rozwiązano problem z nieprawidłowym podwajaniem postępu nawodnienia podczas synchronizacji frontendu z serwerem.

Dodano brakujące wtyczki (application plugin), zaktualizowano .gitignore oraz wprowadzono ostateczne poprawki w kodzie głównego widoku.

## [v0.3.0] - 2026-06-12 (Faza funkcji zaawansowanych i Offline-First)
Niezawodność działania bez internetu oraz system powiadomień.

Zaimplementowano klasę SyncWorker, która umożliwia użytkownikowi dodawanie wpisów bez dostępu do sieci, a następnie automatycznie synchronizuje je z serwerem w tle.

Dodano WaterNotificationReceiver, wprowadzając mechanizm lokalnych przypomnień o piciu wody.

Utworzono TokenManager do bezpiecznego przechowywania i zarządzania tokenami sesji po stronie urządzenia.

Zmodyfikowano WaterViewModel oraz WaterApiService, aby obsłużyć nowy mechanizm synchronizacji i autoryzacji.

## [v0.2.0] - 2026-06-12 (Fundamenty Aplikacji Mobilnej)
SInicjalizacja środowiska Android, budowa interfejsu i połączenie z bazą.

Przeprowadzono refaktoryzację kodu, oficjalnie zmieniając nazwę aplikacji na WaterIO.

Dodano główną klasę modelu WaterEntry dla frontendu.

Utworzono WaterViewModel do dynamicznego zarządzania stanem śledzenia wypitej wody.

Pomyślnie połączono ekran główny (WaterDashboardScreen) z lokalną bazą danych (Room) oraz warstwą logiki (WaterViewModel).

Utworzono WaterApiService przygotowując aplikację do komunikacji z zewnętrznymi endpointami logowania i wysyłania danych.

## [v0.1.0] - 2026-06-11 (Inicjalizacja Backendu)
Skupienie: Baza danych, autoryzacja i stworzenie logiki serwerowej (API).

Zainicjowano projekt backendowy.

Wdrożono prostą bazę danych oraz zaimplementowano podstawowe operacje CRUD dla modelu wpisów.

Dodano i pomyślnie skonfigurowano system logowania i autoryzacji oparty na tokenach JWT.

Stworzono zestaw początkowych endpointów


 

# Instrukcja użytkownika: 

1. Pierwsze kroki i Logowanie 

Po otwarciu aplikacji zostaniesz powitany ekranem logowania. 

Jeśli jesteś nowym użytkownikiem, wybierz opcję "Zarejestruj się" na dole ekranu. Podaj swój adres e-mail oraz bezpieczne hasło (minimum 6 znaków). 

Jeśli posiadasz już konto, wpisz swoje dane i kliknij "Zaloguj". Twoje dane są chronione, a po pierwszym logowaniu aplikacja zapamięta Cię, abyś nie musiał wpisywać hasła za każdym razem. 

2. Ekran Główny i Dodawanie Wody 

Ekran główny (Dashboard) to Twoje centrum dowodzenia. 

Pasek postępu: Duży, okrągły wykres na środku ekranu pokazuje, ile wody wypiłeś w stosunku do Twojego dziennego celu. 

Szybkie dodawanie: Poniżej paska znajdują się przyciski reprezentujące najpopularniejsze naczynia: Szklanka (250 ml) oraz Butelka (500 ml). Wystarczy jedno kliknięcie tuż po wypiciu wody, aby zaktualizować swój wynik. 

Inna objętość: Jeśli wypiłeś niestandardową ilość (np. puszkę 330 ml), wybierz przycisk "Inna ilość", wpisz wartość na klawiaturze numerycznej i zatwierdź. 

3. Przeglądanie Historii i Korekta Błędów 

Każdy dodany wpis jest zapisywany w systemie. Aby go sprawdzić, przejdź do zakładki "Historia" (ikona na dolnym pasku nawigacji). 

Zobaczysz tam chronologiczną listę dzisiejszych wpisów wraz z dokładnymi godzinami. 

Usuwanie pomyłki: Jeśli omyłkowo kliknąłeś przycisk na ekranie głównym, możesz łatwo usunąć błędny wpis. Wystarczy przesunąć palcem po wpisie w lewo (swipe) lub kliknąć czerwoną ikonę kosza. Pasek postępu na ekranie głównym natychmiast się zaktualizuje. 

4. Statystyki i Wykresy 

Zakładka "Statystyki" pozwala na dłuższą analizę Twoich nawyków. Znajdziesz tam wykres słupkowy prezentujący Twoje spożycie wody z ostatnich 7 dni. Dzięki temu łatwo zidentyfikujesz, w które dni tygodnia zapominasz o odpowiednim nawodnieniu. 

5. Ustawienia i Tryb Offline 

W lewym górnym rogu ekranu znajdziesz ikonę profilu/ustawień (Zębatka). 

Cel dzienny: W tym miejscu możesz zmienić swój docelowy próg nawodnienia (domyślnie jest to 2000 ml). 

Działanie bez dostępu do sieci (Tryb Offline): Aplikacja Water Monitor została zaprojektowana tak, aby działać w każdych warunkach. Jeśli jesteś w podróży lub w miejscu bez zasięgu internetu, nadal możesz dodawać wypitą wodę. Aplikacja zapisze te dane w pamięci telefonu i automatycznie wyśle je na Twój profil, gdy tylko odzyskasz połączenie z siecią. 
