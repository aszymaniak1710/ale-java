# Raport Techniczny: System Powiadomień Alertly

## 1. Architektura i Flow (Control Flow)
Proces dystrybucji powiadomień realizowany jest w architekturze warstwowej z wykorzystaniem wzorca Inwersji Sterowania (IoC) dostarczanego przez Spring Framework.

**Szczegółowa Analiza Procesu (Control Flow):**

1.  **Krok 1: Inicjalizacja (Scheduler)**
    *   **Klasa:** `pl.degree.alertly.application.jobs.AlertsSendingJob`
    *   **Opis:** Cykl rozpoczyna się od wyzwalacza czasowego. Metoda `sendProper()` jest adnotowana `@Scheduled(fixedRate = 30000)`, co zmusza kontener Springa do jej uruchamiania w stałych odstępach czasu. Jest to "serce" systemu, które nadaje rytm sprawdzania zagrożeń. Metoda ta nie przyjmuje żadnych argumentów i nie zwraca wartości, służąc jedynie jako starter.

2.  **Krok 2: Delegacja do Logiki Domenowej**
    *   **Klasa:** `pl.degree.alertly.application.service.AlertService`
    *   **Opis:** Job wywołuje metodę `sendProperAlerts()`. Jest to moment przejścia z warstwy infrastruktury (Job) do warstwy serwisowej (Service). Ważnym aspektem jest tu adnotacja `@Transactional`, która otwiera nową transakcję bazodanową. Gwarantuje ona, że wszystkie operacje odczytu i zapisu wykonane w tym cyklu będą spójne (ACID).

3.  **Krok 3: Agregacja Danych (Data Fetching)**
    *   **Repozytoria:** `UserAlertSettingsRepository`, `IncidentRepository`
    *   **Opis:** System pobiera "migawkę" stanu świata. Dzieje się to w dwóch zapytaniach SQL:
        *   `findAll()`: Pobiera konfigurację wszystkich użytkowników do pamięci RAM (List).
        *   `findByTimeAfter(...)`: Pobiera tylko te incydenty, które są "świeże" (nie starsze niż 2h).
        *   *Analiza techniczna:* Jest to podejście "eager loading". Zamiast odpytywać bazę dla każdego użytkownika osobno (co zabiłoby wydajność), ładujemy dane do pamięci aplikacji i dalsze operacje wykonujemy na obiektach Javy.

4.  **Krok 4: Pętla Przetwarzania (Processing Loop)**
    *   **Metoda:** `sendProperAlerts` (wnętrze)
    *   **Opis:** Odbywa się tu iteracja w pętli zagnieżdżonej(`O(N*M)`). Zewnętrzna pętla iteruje po incydentach, a wewnętrzna po użytkownikach. Dla każdej kombinacji wywoływana jest metoda `processAlertForUser`. To tutaj zapadają decyzje biznesowe.

5.  **Krok 5: Filtracja Logiczna (Business Rules)**
    *   **Metoda:** `isUserInterestedInIncident`
    *   **Opis:** Weryfikacja "twarda", czy użytkownik w ogóle *chce* wiedzieć o takim zdarzeniu. Sprawdzane są trzy warunki logiczne:
        1.  Czy kategoria incydentu znajduje się na liście subskrybowanych kategorii użytkownika?
        2.  Czy poziom incydentu jest akceptowany przez użytkownika?
        3.  Czy incydent mieści się w godzinach aktywności użytkownika (pomiędzy `from` a `to`)?
    *   Jeśli którykolwiek warunek jest niespełniony, proces dla tej pary jest przerywany.

6.  **Krok 6: Analiza Przestrzenna (Geospatial Analysis)**
    *   **Metoda:** `userInRadius`
    *   **Opis:** System wykonuje lookup lokalizacji użytkownika (`UserLocationRepository.findById`). Jeśli lokalizacja jest znana, obliczany jest dystans euklidesowy między punktem zdarzenia a użytkownikiem. Jeżeli dystans mieści się w zdefiniowanym przez użytkownika promieniu (`radius`), system uznaje, że użytkownik jest zagrożony.

7.  **Krok 7: Maszyna Stanów Powiadomień (State Machine)**
    *   **Metoda:** `processUserInRadius` + `UserMessagingRepository`
    *   **Opis:** System sprawdza w bazie (`findById`), czy powiadomienie zostało już wysłane.
        *   **Stan Pusty:** Pierwsze powiadomienie -> Wysyłka "ONCE" -> Zapis do bazy.
        *   **Stan ONCE:** Sprawdzenie "strefy krytycznej" (czy użytkownik zbliżył się jeszcze bardziej?). Jeśli tak -> Wysyłka "TWICE" -> Aktualizacja bazy.
        *   **Stan TWICE:** Brak akcji.
        *   **Reset:** Jeśli w Kroku 6 okaże się, że użytkownik opuścił strefę, rekord jest usuwany, co resetuje maszynę stanów do początku.

8.  **Krok 8: Integracja i Wysyłka (Integration)**
    *   **Klasa:** `pl.degree.alertly.infrastructure.sender.FirebaseSenderService`
    *   **Opis:** Po podjęciu decyzji o wysyłce, sterowanie trafia do serwisu infrastrukturalnego. Budowany jest obiekt `Message` z biblioteki Firebase Admin SDK, zawierający tytuł i treść. Metoda `FirebaseMessaging.getInstance().send(message)` wykonuje synchroniczne połączenie HTTP/2 do chmury Google, fizycznie zlecając dostarczenie powiadomienia na telefon.

**Kluczowe komponenty:**
*   `AlertsSendingJob` (@Component)
*   `AlertService` (@Service)
*   `FirebaseSenderService` (@Service)
*   `IncidentRepository`, `UserAlertSettingsRepository`, `UserMessagingRepository`, `UserLocationRepository` (Interfejsy JpaRepository)

### 1.1. Specyfikacja Logiki Biznesowej (Algorytm Decyzyjny)

System podejmuje decyzje o wysyłce w oparciu o zestaw sztywnych reguł (Business Rules). Poniżej znajduje się formalna specyfikacja tych warunków.

**A. Reguły Filtracji Wstępnej (Interest Check)**
Użytkownik jest uznany za "zainteresowanego" incydentem (`isUserInterestedInIncident`), wtedy i tylko wtedy, gdy koniunkcja poniższych trzech warunków jest prawdą:
1.  **Zgodność Kategorii:** `Incident.Category ∈ User.Categories`
2.  **Zgodność Poziomu:** `Incident.Level ∈ User.Levels`
3.  **Okno Czasowe:** `User.From < Incident.Time < User.To`
    *   *Analiza krytyczna:* Obecna implementacja (porównanie proste `isBefore`/`isAfter`) nie obsługuje poprawnie przedziałów nocnych przechodzących przez północ (np. 22:00 - 06:00). Dla takich ustawień warunek zawsze zwróci fałsz.

**B. Reguły Przestrzenne (Spatial Check)**
1.  **Zasięg Podstawowy:** Użytkownik znajduje się w zasięgu, jeśli:
    `Dystans(User, Incident) <= User.Radius`
    *   Gdzie `Dystans` to odległość euklidesowa na płaszczyźnie współrzędnych (uproszczenie, brak uwzględnienia krzywizny Ziemi w tym module).

**C. Maszyna Stanów i Eskalacja (State Machine)**
System zapobiega spamowaniu, ale pozwala na eskalację zagrożenia. Logika opiera się na historii w tabeli `user_messaging`.

*   **Scenariusz 1: Nowe Zagrożenie**
    *   **Warunek:** Brak wpisu w historii ORAZ Użytkownik w Zasięgu Podstawowym.
    *   **Akcja:** Wyślij powiadomienie `ONCE` ("Incydent w pobliżu").

*   **Scenariusz 2: Eskalacja (Krytyczne Zbliżenie)**
    *   **Warunek:** Istnieje wpis `ONCE` ORAZ `User.Radius >= 2 * Global.CloseDistance` ORAZ `Dystans <= Global.CloseDistance`.
    *   *Wyjaśnienie:* Warunek promienia (`>= 2*Close`) jest zabezpieczeniem biznesowym. Użytkownik, który ustawił sobie bardzo mały promień powiadomień (np. 100m przy CloseDistance=200m), nie powinien otrzymywać dwóch powiadomień naraz, ponieważ dla niego "zasięg" i "bliski zasięg" to to samo.
    *   **Akcja:** Wyślij powiadomienie `TWICE` ("Jesteś blisko incydentu!").

*   **Scenariusz 3: Reset (Opuszczenie Strefy)**
    *   **Warunek:** Użytkownik jest "Zainteresowany" (Reguła A spełniona), ale znajduje się **poza** Zasięgiem Podstawowym (Reguła B niespełniona).
    *   **Akcja:** Usuń wpis z historii powiadomień.
    *   **Cel:** Jeśli użytkownik wróci w strefę zagrożenia, zostanie potraktowany jak w Scenariuszu 1 (otrzyma nowe powiadomienie).

### 1.2. Struktura Metod Logiki Biznesowej (Implementacja)

Poniżej przedstawiono analizę kluczowych metod klasy `AlertService`, implementujących powyższą logikę.

**1. `processAlertForUser(IncidentEntity incident, UserAlertSettingsEntity user)`**
*   **Typ:** `void` (metoda sterująca).
*   **Logika:**
    ```java
    if (isUserInterestedInIncident(incident, user)) {
        if (userInRadius(incident, user)) {
            processUserInRadius(incident, user); // Scenariusz 1 i 2
        } else {
            resetUserMessaging(incident, user);  // Scenariusz 3 (Reset)
        }
    }
    ```
    *   Jest to główny węzeł decyzyjny algorytmu.

**2. `isUserInterestedInIncident(IncidentEntity incident, UserAlertSettingsEntity user)`**
*   **Typ:** `boolean`.
*   **Logika:** Zwraca koniunkcję (`&&`) trzech metod pomocniczych:
    *   `shouldSendByCategory` (sprawdza `List.contains`)
    *   `shouldSendByLevel` (sprawdza `List.contains`)
    *   `shouldSendByTimePeriod` (sprawdza `Incident.Time` vs `User.From/To`)

**3. `processUserInRadius(IncidentEntity incident, UserAlertSettingsEntity user)`**
*   **Typ:** `void`.
*   **Logika:**
    1.  Pobiera historię z bazy: `userMessagingRepository.findById(...)`.
    2.  `if (historia istnieje)`:
        *   `if (status == ONCE)`:
            *   Sprawdza eskalację: `isUserClose(...)`.
            *   Jeśli tak -> `send(..., TWICE)`.
    3.  `else` (historia nie istnieje):
        *   `send(..., ONCE)`.

**4. `isUserClose(IncidentEntity, String token, Integer radius)`**
*   **Typ:** `boolean`.
*   **Logika:** Decyduje o eskalacji (TWICE).
    *   Pobiera `globalCloseDistance` z konfiguracji (domyślnie 200).
    *   Sprawdza warunek bezpieczeństwa: `radius >= 2 * globalCloseDistance`.
    *   Sprawdza dystans fizyczny: `distance(...) <= globalCloseDistance`.

**5. `userInRadius(IncidentEntity, UserAlertSettingsEntity)`**
*   **Typ:** `boolean`.
*   **Logika:**
    1.  Pobiera `UserLocationEntity` z repozytorium. Jeśli brak -> zwraca `false`.
    2.  Oblicza `d = distance(incident.lat, incident.lon, user.lat, user.lon)`.
    3.  Zwraca `d <= user.radius`.

## 2. Mechanizm Wyzwalania
System wykorzystuje natywny mechanizm **Spring Scheduling**.

*   **Biblioteka:** `spring-context` (moduł scheduling).
*   **Konfiguracja:** Adnotacja `@Scheduled(fixedRate = 30000)` w klasie `AlertsSendingJob`.
*   **Częstotliwość:** Zadanie uruchamiane jest co **30 000 ms (30 sekund)**. Czas mierzony jest od momentu rozpoczęcia poprzedniego wykonania (fixedRate).
*   **Wątkowość:** Domyślnie Spring Scheduler używa jednego wątku. Oznacza to, że jeśli przetworzenie zajmie więcej niż 30 sekund, kolejne wykonanie zostanie opóźnione (kolejkowanie).

## 3. Logika Pobierania Danych
System działa w modelu "Pull" (aktywne odpytywanie bazy).

**Kryteria selekcji incydentów:**
*   **Źródło:** `IncidentRepository`.
*   **Metoda:** `findByTimeAfter(LocalDateTime time)`.
*   **Filtr SQL:** `WHERE time > :now_minus_120_minutes`.
*   **Cel:** Pobierane są tylko incydenty "żywe", tj. takie, które wydarzyły się w ciągu ostatnich 2 godzin.

**Kryteria selekcji użytkowników:**
*   **Źródło:** `UserAlertSettingsRepository`.
*   **Metoda:** `findAll()`.
*   **Uwaga wydajnościowa:** Pobierana jest **pełna tabela** ustawień użytkowników przy każdym cyklu. Przy dużej skali (np. 100k użytkowników) jest to wąskie gardło (n+1 problem, memory overhead).

**Kryteria parowania (Filtracja w pamięci):**
*   **Category Matching:** `user.getCategory().contains(incident.getCategory())`.
*   **Level Matching:** `user.getLevel().contains(incident.getLevel())`.
*   **Time Window:** `Time.isAfter(From) && Time.isBefore(To)`.

## 4. Transformacja i Formatowanie
System nie używa zewnętrznych silników szablonów. Wiadomości są budowane przy użyciu standardowej konkatenacji łańcuchów znaków w Javie.

**Klasa odpowiedzialna:** `FirebaseSenderService`.
**Metoda formatująca:** `String.format(...)`.

**Wzór wiadomości (Pattern):**
```java
String body = String.format(
    "%s (%s) w %s.",
    incident.getCategory(), // np. FIRE
    incident.getLevel(),    // np. CRITICAL
    incident.getDistrict()  // np. Manhattan
);
```
**DTO Mapping:** Brak dedykowanych DTO dla warstwy wysyłki. Encje domenowe (`IncidentEntity`) są przekazywane bezpośrednio do warstwy infrastruktury, co łamie zasadę separacji warstw (leaking domain model), ale upraszcza implementację w małej skali.

## 5. Kanały Komunikacji (Integracje)
Jedynym wspieranym kanałem jest **Push Notification** poprzez platformę Firebase (Google).

*   **Biblioteka klienta:** `firebase-admin` (SDK Java).
*   **Protokół:** HTTP/2 (via SDK).
*   **Typ wiadomości FCM:** Notification Message (klucz `notification` w JSON payload).
    *   `title`: Statyczny (zmienia się tylko dla ONCE/TWICE/SOS).
    *   `body`: Dynamiczny.
*   **Adresowanie:** Direct to Device Token (nie używa Topic Messaging).
*   **Autoryzacja:** Service Account Credentials (plik JSON, ładowany przy starcie aplikacji - poza zakresem analizowanego kodu `AlertService`, ale widoczny w konfiguracji Springa).

## 6. Obsługa Błędów i Odporność
Analiza wykazuje niski poziom odporności na błędy (Fault Tolerance).

*   **Retry:** **Brak**. Nieudana wysyłka nie jest ponawiana.
*   **Exception Handling:**
    *   W `FirebaseSenderService`: Wyjątek sprawdzany `FirebaseMessagingException` jest łapany, ale natychmiast opakowywany w niesprawdzany `RuntimeException`.
    *   W `AlertService`: Brak obsługi wyjątków. `RuntimeException` propaguje się w górę stosu.
*   **Konsekwencja Transakcyjna:** Metoda `sendProperAlerts` jest `@Transactional`. Wystąpienie błędu przy 99. użytkowniku spowoduje **Rollback** bazy danych dla wszystkich 98 poprzednich (cofnięcie zapisów w `UserMessaging`). W kolejnym cyklu (za 30s) system spróbuje wysłać powiadomienia ponownie do wszystkich, co może prowadzić do pętli błędu (Poison Pill).

## 7. Model Danych (Relacyjny)

**Tabela: `incidents`**
| Kolumna | Typ | Opis |
| :--- | :--- | :--- |
| `id` | BIGINT (PK) | Sekwencyjne ID |
| `category` | VARCHAR | Enum (FIGHT, FIRE...) |
| `level` | VARCHAR | Enum (LOW, HIGH...) |
| `latitude` | DOUBLE | Współrzędna Y |
| `width` | DOUBLE | Współrzędna X (używana jako longitude) |
| `time` | TIMESTAMP | Czas zdarzenia |

**Tabela: `user_alert_settings`**
| Kolumna | Typ | Opis |
| :--- | :--- | :--- |
| `token` | VARCHAR (PK) | ID Użytkownika |
| `radius` | INTEGER | Zasięg (metry/jednostki) |
| `category` | VARCHAR[] | Tablica (PostgreSQL Array) |
| `level` | VARCHAR[] | Tablica (PostgreSQL Array) |
| `device_id`| VARCHAR | Token FCM |

**Tabela: `user_messaging`** (Tabela asocjacyjna/stanowa)
| Kolumna | Typ | Opis |
| :--- | :--- | :--- |
| `incident_id`| BIGINT (PK, FK)| ID Incydentu |
| `device_id` | VARCHAR (PK) | ID Urządzenia (część klucza złożonego) |
| `message_quantity` | VARCHAR | Enum (ONCE, TWICE) |

*Relacje:* Brak sztywnych więzów Foreign Key w definicjach JPA (Luźne powiązania oparte o ID), choć baza danych może wymuszać integralność.

## 8. Wzorce Projektowe
W module zidentyfikowano następujące wzorce:

1.  **Repository Pattern:** (`IncidentRepository`, etc.) - Abstrakcja dostępu do danych, ukrywająca szczegóły zapytań SQL.
2.  **Inversion of Control (IoC) / Dependency Injection:** Wstrzykiwanie zależności przez konstruktor (`Lombok @RequiredArgsConstructor`) w serwisach.
3.  **Builder Pattern:** Używany w `FirebaseSenderService` do konstrukcji obiektu `Message` (`Message.builder()...build()`).
4.  **Transaction Script:** Logika w `AlertService` jest zorganizowana proceduralnie jako jedna długa transakcja realizująca całą logikę biznesową krok po kroku.
5.  **State Machine (uproszczona):** Logika w `AlertService` implementuje maszynę stanów dla cyklu życia powiadomienia (Start -> ONCE -> TWICE -> Koniec).
