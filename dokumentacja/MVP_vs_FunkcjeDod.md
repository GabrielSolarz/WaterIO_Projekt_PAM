# MVP vs funkcje dodatkowe  

## ZAKRES MVP (Minimum Viable Product - Wymagane do zaliczenia) 

* Aby projekt uznać za kompletny i gotowy do prezentacji (release v1.0), aplikacja musi posiadać następujące funkcje: 

### Moduł Autoryzacji (Zgodność z wymogiem integracji API): 

* Rejestracja i logowanie użytkownika (zabezpieczone tokenem JWT). 

### Dashboard Nawodnienia (Główna wartość biznesowa): 

* Ustawienie stałego, dziennego celu nawodnienia (np. 2000 ml). 

* Wizualny pasek postępu (Circular Progress Bar) obrazujący stopień realizacji celu w bieżącym dniu. 

### Operacje Domenowe (Zgodność z wymogiem CRUD): 

* Przyciski szybkiego dodawania typowych objętości wody (szklanka 250 ml, butelka 500 ml). 

* Możliwość ręcznego wpisania niestandardowej objętości. 

* Przeglądanie historii dodanej wody z bieżącego dnia wraz z możliwością usunięcia błędnego wpisu. 

### Tryb Offline-First (1. funkcja natywna): 

* Wykorzystanie lokalnej bazy danych urządzenia (Room). Aplikacja pozwala na dodawanie wypitej wody bez dostępu do sieci, przechowując stan lokalnie i synchronizując go z backendem po odzyskaniu połączenia. 

### Powiadomienia Push (2. funkcja natywna): 

* Lokalne powiadomienia przypominające użytkownikowi o konieczności napicia się wody w przypadku braku aktywności (np. po 3 godzinach od ostatniego wpisu). 

### Podstawowe Statystyki: 

* Prosty wykres słupkowy pobierający z API historię spożycia wody z ostatnich 7 dni, realizujący wymóg prezentacji danych z tematu projektu. 

## FUNKCJE DODATKOWE (Backlog / Nice-to-have) 

* Elementy niekrytyczne dla działania systemu, zaplanowane do wdrożenia w dalszych etapach rozwoju (release v1.x): 

### Rozszerzony System Motywacyjny (Gamifikacja): 

* Implementacja "streaków" (licznik nieprzerwanych dni z rzędu z osiągniętym celem dziennym) oraz wirtualnych odznak za np. 7, 30, 100 dni poprawnego nawadniania. 

### Personalizacja Przypomnień: 

* Rozbudowany panel ustawień powiadomień, pozwalający zdefiniować godziny snu, podczas których powiadomienia push są wyciszone (np. 22:00 - 06:00). 

### Kalkulator Zapotrzebowania: 

* Automatyczne wyliczanie zalecanego celu dziennego (zamiast sztywnego wpisywania) na podstawie wagi, płci i wprowadzanej ręcznie aktywności fizycznej w danym dniu. 

### Tryb Ciemny (Dark Mode): 

* Pełne wsparcie dla systemowego trybu ciemnego, poprawiające UX podczas używania aplikacji wieczorami 
