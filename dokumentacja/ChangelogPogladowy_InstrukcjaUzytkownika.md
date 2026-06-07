# Changelog: 

[v1.0.0] - 2026-06-02 (Wersja Release / Prezentacja) Dodano 

Wykresy słupkowe prezentujące spożycie wody z ostatnich 7 dni na ekranie Statystyk. 

System motywacyjny (Streaki) na ekranie głównym. 

Opcję ręcznego wpisywania niestandardowej objętości wypitej wody. 

Mechanizm powiadomień Push przypominający o piciu wody po 3 godzinach braku aktywności. 

Pełną obsługę trybu offline (zapis w bazie Room i automatyczna synchronizacja z API po odzyskaniu sieci). 

Zmieniono 

Ulepszono design ekranu logowania i dodano obsługę błędów sieci. 

Zoptymalizowano zapytania do bazy danych, aby przyspieszyć ładowanie wykresów. 

Naprawiono 

Błąd powodujący zamykanie aplikacji przy wpisaniu ujemnej wartości wody. 

Problem z nieodświeżającym się paskiem postępu po usunięciu wpisu z historii. 

[v0.5.0] - (Wersja Beta) Dodano 

Ekran główny (Dashboard) z okrągłym paskiem postępu (Circular Progress Bar). 

Przyciski szybkiego dodawania wody (+250ml, +500ml). 

Ekran "Historia" z listą dzisiejszych wpisów i opcją ich usuwania. 

Ustawienia pozwalające na zdefiniowanie własnego celu dziennego. 

[v0.1.0] - (Initial MVP) Dodano 

Inicjalizacja projektu w Android Studio (Kotlin). 

Podstawowy ekran autoryzacji (Rejestracja i Logowanie). 

Konfiguracja API i integracja autoryzacji JWT. 

 

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
