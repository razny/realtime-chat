# Aplikacja czatu w czasie rzeczywistym

[Obejrz demo wideo](https://www.dropbox.com/scl/fi/i5fnttj0kz5duo5hl5im1/demo_chat.mp4?rlkey=3dxylqx1uwy21fjqh9kyhudgz&st=6c90pfr4&dl=0)

Full-stackowa aplikacja czatu w czasie rzeczywistym zbudowana przy użyciu **Spring Boot**, **WebSocket (STOMP/SockJS)**, **RabbitMQ** i **MongoDB**. Oferuje komunikację w czasie rzeczywistym, przechowywanie historii wiadomości, monitorowanie aktywnych użytkowników oraz integrację z botami.

---

## Funkcje

- Wiadomości w czasie rzeczywistym za pomocą WebSocket (STOMP/SockJS)
- RESTful CRUD dla wiadomości czatu
- Przechowywanie wiadomości w MongoDB
- RabbitMQ do asynchronicznego przetwarzania wiadomości
- Śledzenie aktywnych użytkowników i ich transmisja
- Edytowanie/Usuwanie wiadomości (z obsługą ID klienta/serwera)
- Komendy botów (`/cat`, `/dog`)
- Responsywny frontend (HTML/CSS/JS, Bootstrap)
- Testy jednostkowe i integracyjne

---

## Architektura

- **gateway** — Obsługuje połączenia WebSocket, REST API, aktywnych użytkowników i boty
- **message-processor** — Przetwarza wiadomości asynchronicznie i przechowuje je w MongoDB

---

## Instrukcje uruchomienia

**Wymagania:**  
Java 17+, Maven, RabbitMQ, MongoDB

**1. Zbuduj wszystko:**

```sh
mvn clean install
```

**2. Uruchom RabbitMQ i MongoDB** (lokalnie lub za pomocą Dockera):

```sh
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
docker run -d --name mongo -p 27017:27017 mongo
```

**3. Uruchom backend:**

```sh
cd message-processor && mvn spring-boot:run
# W innym terminalu:
cd gateway && mvn spring-boot:run
```

**4. Otwórz aplikację:**  
Przejdź do [http://localhost:8080/chat](http://localhost:8080/chat)

---

## Testy

```sh
mvn test
```

Obejmuje zarówno testy jednostkowe, jak i integracyjne.

---

## Konfiguracja

Edytuj `application.properties` w każdym module, aby dostosować porty, bazę danych lub ustawienia RabbitMQ.

---

## Endpointy API

- `GET /api/messages` — Lista wszystkich wiadomości
- `POST /api/messages` — Utwórz wiadomość
- `GET /api/messages/{id}` — Pobierz wiadomość po ID
- `PUT /api/messages/{id}` — Zaktualizuj wiadomość
- `DELETE /api/messages/{id}` — Usuń wiadomość
- `GET /api/messages/recent?since=timestamp` — Ostatnie wiadomości
- `GET /activeUsers` — Lista aktywnych użytkowników
- `GET /api/cat` — Pobierz obraz kota (bot)
- `GET /api/dog` — Pobierz obraz psa (bot)

---

## Zrzuty ekranu

**Normalny widok**  
![Normalny widok](https://github.com/user-attachments/assets/413eacc5-5f94-46d7-bad6-73368aeeadc0)

**Edytowanie wiadomości**  
![Edytowanie wiadomości](https://github.com/user-attachments/assets/50a3fd7e-86d2-4a54-9689-c7b9bc0bfe94)

**Usuwanie wiadomości**  
![Usuwanie wiadomości](https://github.com/user-attachments/assets/ac7fdc1b-bbfe-430e-b2fb-520af57ab76d)
