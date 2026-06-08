### 1. Rejestracja nowego konta 

Jako nowy użytkownik, chcę założyć konto podając adres e-mail i hasło, aby moje dane o nawodnieniu były trwale i bezpiecznie przechowywane. 

Kryteria akceptacji: 

* Aplikacja posiada widoczny ekran rejestracji. 

* System waliduje, czy wpisany tekst jest poprawnym adresem e-mail. 

* Hasło musi mieć minimum 6 znaków. 

* Po udanej rejestracji (odpowiedź z API z kodem 201) użytkownik jest automatycznie przenoszony na ekran logowania lub główny dashboard. 

### 2. Logowanie do aplikacji 

Jako zarejestrowany użytkownik, chcę zalogować się do swojego konta, aby mieć dostęp do historii mojego nawodnienia. 

Kryteria akceptacji: 

* Formularz logowania akceptuje e-mail i hasło. 

* Wpisanie błędnych danych wyświetla jasny komunikat o błędzie (np. Toast/Snackbar: "Błędne dane logowania"). 

* Po pomyślnym logowaniu aplikacja zapisuje bezpiecznie token JWT i przenosi na ekran główny. 

### 3. Wylogowanie 

Jako użytkownik, chcę móc bezpiecznie wylogować się z aplikacji, aby inna osoba korzystająca z telefonu nie miała dostępu do moich danych. 

Kryteria akceptacji: 

* Przycisk "Wyloguj" jest dostępny na ekranie ustawień/profilu. 

* Kliknięcie usuwa token JWT z pamięci lokalnej (DataStore/SharedPreferences). 

* Użytkownik zostaje natychmiast przeniesiony na ekran logowania. 

### 4. Ustalenie dziennego celu (Update) 

Jako użytkownik, chcę ustawić mój indywidualny cel dziennego nawodnienia (np. 2500 ml), aby aplikacja mogła precyzyjnie obliczać mój postęp. 

Kryteria akceptacji: 

* Pole tekstowe w ustawieniach przyjmuje wyłącznie wartości liczbowe większe od 0. 

* Nowy cel zapisuje się w bazie i natychmiast aktualizuje widok na głównym ekranie. 

### 5. Podgląd dziennego postępu (Read) 

Jako użytkownik, chcę widzieć na głównym ekranie wizualny pasek postępu, aby błyskawicznie ocenić, jak dużo wody muszę jeszcze dziś wypić. 

Kryteria akceptacji: 

* Ekran główny wyświetla wykres kołowy lub pasek. 

* Widoczny jest tekst w formacie "Obecny stan / Cel ml" (np. "1250 / 2000 ml"). 

* Po osiągnięciu 100% pasek zmienia kolor (np. na zielony), aby dać poczucie zrealizowanego zadania. 

### 6. Szybkie dodawanie - Szklanka (Create) 

Jako użytkownik, chcę dodać wypitą szklankę wody jednym kliknięciem, aby nie tracić czasu na ręczne wpisywanie wartości. 

Kryteria akceptacji: 

* Na ekranie głównym istnieje dedykowany przycisk "+ 250 ml". 

* Kliknięcie natychmiastowo zwiększa aktualny stan na pasku postępu. 

* Dane są wysyłane asynchronicznie do bazy danych. 

### 7. Szybkie dodawanie - Butelka (Create) 

Jako użytkownik, chcę dodać wypitą butelkę wody (500 ml) jednym kliknięciem, aby proces wprowadzania danych był jak najszybszy. 

Kryteria akceptacji: 

* Na ekranie głównym znajduje się przycisk "+ 500 ml". 

* Jego użycie automatycznie przelicza i odświeża główny pasek postępu. 

### 8. Niestandardowa objętość (Create) 

Jako użytkownik, chcę ręcznie wpisać dowolną ilość wypitej wody (np. 150 ml), aby precyzyjnie notować mniejsze lub nietypowe pojemności. 

Kryteria akceptacji: 

* Użytkownik ma dostęp do pola tekstowego wywołującego klawiaturę numeryczną. 

* Zatwierdzenie wartości przelicza całkowity bilans i wysyła zapytanie do API. 

### 9. Dodawanie wody bez internetu (Local Storage / Offline) 

Jako użytkownik będący poza zasięgiem sieci, chcę wciąż móc dodawać wpisy o wypitej wodzie, aby nie stracić ciągłości moich pomiarów. 

Kryteria akceptacji: 

* Przy braku połączenia internetowego aplikacja nie wyświetla krytycznego błędu. 

* Wpis dodaje się do lokalnej bazy danych (Room) z wewnętrzną flagą isSynced = false. 

* Użytkownik otrzymuje informację: "Zapisano offline. Wymaga synchronizacji". 

### 10. Automatyczna synchronizacja danych 

Jako użytkownik powracający do zasięgu internetu, chcę, aby moje zapisane offline dane wysłały się na serwer, aby statystyki na innych moich urządzeniach były spójne. 

Kryteria akceptacji: 

* Aplikacja po wykryciu sieci odszukuje w lokalnej bazie wpisy z isSynced = false. 

* System wysyła zaległe wpisy do API (POST /water). 

* Po pomyślnej odpowiedzi serwera, flaga w lokalnej bazie zmienia się na isSynced = true. 

### 11. Przypomnienia o piciu (Push Notifications) 

Jako zapracowany użytkownik, chcę otrzymywać powiadomienie push, jeśli nie zanotowałem żadnego płynu od 3 godzin, aby aplikacja aktywnie pomagała mi utrzymać nawodnienie. 

Kryteria akceptacji: 

* Aplikacja korzysta z wbudowanych mechanizmów Androida do odmierzania czasu w tle. 

* Po upływie zdefiniowanego czasu pojawia się natywne powiadomienie telefonu (ikona aplikacji, dźwięk/wibracja). 

* Dodanie wpisu o wodzie resetuje licznik czasu powiadomienia od nowa. 

 

### 12. Podgląd historii dnia (Read) 

Jako użytkownik, chcę widzieć chronologiczną listę dzisiejszych porcji wody, aby sprawdzić, czy nie zapomniałem czegoś dodać lub czy nie kliknąłem przycisku podwójnie. 

Kryteria akceptacji: 

* Dostępny jest widok listy (RecyclerView/LazyColumn) prezentujący dzisiejsze wpisy. 

* Każdy element listy zawiera dokładną godzinę dodania oraz objętość (np. "14:30 - 250 ml"). 

### 13. Usuwanie pomyłki (Delete) 

Jako użytkownik przeglądający historię, chcę móc usunąć błędnie wprowadzony wpis, aby mój dzienny bilans pokazywał prawdę. 

Kryteria akceptacji: 

* Każdy element na liście historii posiada przycisk kosza/usuwania (lub obsługuje gest przesunięcia w bok - swipe-to-delete). 

* Usunięcie wpisu wymaga potwierdzenia. 

* Akcja natychmiast odejmuje wartość z głównego paska postępu i wysyła odpowiednie żądanie do API. 

### 14. Wykresy tygodniowe (Zgodnie z tematem) 

Jako użytkownik dbający o zdrowie, chcę widzieć moje spożycie z ostatnich 7 dni na wykresie, aby przeanalizować, w które dni tygodnia piję najmniej. 

Kryteria akceptacji: 

* Aplikacja posiada dedykowany ekran "Statystyki". 

* Dane pobierane są z API (GET /stats). 

* Widoczny jest prosty wykres słupkowy, gdzie oś Y to mililitry, a oś X to kolejne dni. 

### 15. System motywacyjny - Streaki (Zgodnie z tematem) 

Jako użytkownik, chcę wiedzieć, ile dni z rzędu udało mi się zrealizować mój dzienny cel, aby czuć motywację do nieprzerywania dobrej passy. 

Kryteria akceptacji: 

* Główny ekran wyświetla licznik "dni w dobrej passie" (np. "🔥5 dni"). 

Jeśli o północy wczorajszy cel nie został osiągnięty (wynik < 100%), licznik resetuje się do zera. 
