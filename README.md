# Wypożyczalnia Samochodów

Dwukomponentowa aplikacja webowa do zarządzania wypożyczalnią samochodów, zbudowana w technologii **Spring Boot** (backend) i **Angular** (frontend).

## Opis projektu

Aplikacja umożliwia kompleksowe zarządzanie flotą samochodów, bazą klientów oraz procesem wypożyczeń. System zawiera funkcjonalności CRUD dla wszystkich głównych encji oraz zaawansowaną logikę biznesową związaną z procesem wypożyczania i zwracania pojazdów.

## Technologie

### Backend
- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **PostgreSQL 17**
- **Maven**
- **JUnit** (testy jednostkowe)
- **Mockito** (mocki w testach)

### Frontend
- **Angular 19**
- **TypeScript**
- **Bootstrap 5**
- **Font Awesome**
- **Reactive Forms**
- **Standalone Components**

## Funkcjonalności

### Zarządzanie samochodami
- Dodawanie, edycja i usuwanie samochodów
- Wyświetlanie listy samochodów z informacją o statusie i dostępnymi akcjami do wykonania (edycja/usunięcie/wypożyczenie)
- Automatyczne zarządzanie statusem (DOSTĘPNY/WYPOŻYCZONY)

### Zarządzanie klientami
- Dodawanie, edycja i usuwanie klientów
- Walidacja unikalności adresu email
- Blokada usuwania klientów z aktywnymi wypożyczeniami

### System wypożyczeń
- Wypożyczanie samochodów z automatyczną kalkulacją kosztów
- Zwracanie samochodów z aktualizacją statusu
- Filtrowanie i sortowanie wypożyczeń
- Wyświetlanie aktywnych i historycznych wypożyczeń
- Integracja między modułami (wypożycz bezpośrednio z listy samochodów)

## Struktura projektu

```
wypozyczalnia/
├── car-rental-backend/          # Spring Boot API
│   ├── src/main/java/
│   │   ├── controller/          # REST Controllers
│   │   ├── service/             # Business Logic
│   │   ├── repository/          # Data Access Layer
│   │   ├── entity/              # JPA Entities
│   │   └── dto/                 # Data Transfer Objects
│   └── src/test/java/           # Testy jednostkowe
└── car-rental-frontend/         # Angular App
    ├── src/app/
    │   ├── components/          # Angular Components
    │   ├── services/            # HTTP Services
    │   ├── models/              # TypeScript Interfaces
    │   └── shared/              # Shared Components
```

## Instalacja i uruchomienie

### Wymagania
- Java 17
- Node.js 18
- Docker & Docker Compose
- Maven 3.6
- Angular CLI

### Backend (Spring Boot)

1. **Sklonuj repozytorium**
```bash
git clone https://github.com/pawel-rachocki/wypozyczalnia-app.git
cd car-rental/car-rental-backend
```

2. **Uruchom bazę danych PostgreSQL przez Docker**
```bash
# Uruchom PostgreSQL w kontenerze
docker-compose up -d postgres-db

# Sprawdź czy kontener działa
docker ps
```

3. **Skonfiguruj połączenie w application.properties**
```properties
spring.application.name=car-rental-backend

# DB
spring.datasource.url=jdbc:postgresql://localhost:5432/car_rental
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

4. **Uruchom aplikację**
```bash
cd car-rental-backend
mvn spring-boot:run
```

Backend będzie dostępny pod adresem: `http://localhost:8080`

### Frontend (Angular)

1. **Przejdź do katalogu frontend**
```bash
cd ../car-rental-frontend
```

2. **Zainstaluj zależności**
```bash
npm install
```

3. **Uruchom aplikację**
```bash
ng serve
```

Frontend będzie dostępny pod adresem: `http://localhost:4200`

## API Endpoints

### Samochody
- `GET /api/samochody` - Lista wszystkich samochodów
- `GET /api/samochody/{id}` - Szczegóły samochodu
- `POST /api/samochody` - Dodaj nowy samochód
- `PUT /api/samochody/{id}` - Aktualizuj samochód
- `DELETE /api/samochody/{id}` - Usuń samochód
- `GET /api/samochody/dostepne` - Lista dostępnych samochodów

### Klienci
- `GET /api/klienci` - Lista wszystkich klientów
- `GET /api/klienci/{id}` - Szczegóły klienta
- `POST /api/klienci` - Dodaj nowego klienta
- `PUT /api/klienci/{id}` - Aktualizuj klienta
- `DELETE /api/klienci/{id}` - Usuń klienta

### Wypożyczenia
- `GET /api/wypozyczenia` - Lista wszystkich wypożyczeń
- `GET /api/wypozyczenia/{id}` - Szczegóły wypożyczenia
- `POST /api/wypozyczenia/wypozycz` - Wypożycz samochód
- `PUT /api/wypozyczenia/{id}/zwroc` - Zwróć samochód
- `GET /api/wypozyczenia/aktywne` - Lista aktywnych wypożyczeń

## Testy

Projekt zawiera kompletne pokrycie testami jednostkowymi (47 testów).

### Uruchomienie testów
```bash
# Wszystkie testy
mvn test

# Testy konkretnej klasy
mvn test -Dtest=SamochodServiceTest
```

### Pokrycie testami
- **Repository Layer**: Testy integracyjne z bazą H2
- **Pozytywne i negatywne scenariusze**: Walidacja i obsługa błędów

## Architektura

### Backend - Clean Architecture
- **Controller Layer**: Obsługa HTTP requests
- **Service Layer**: Logika biznesowa i walidacja
- **Repository Layer**: Dostęp do danych
- **Entity Layer**: Modele domenowe

### Frontend - Component Architecture
- **Standalone Components**: Angular 19 approach
- **Reactive Forms**: Walidacja po stronie klienta
- **Services**: HTTP komunikacja z backend
- **Shared Components**: Footer, nawigacja

## Funkcje UI/UX

- **Responsive Design**: Działa na desktop i mobile
- **Intuicyjna nawigacja**: Navbar z dropdown menu
- **Real-time feedback**: Komunikaty sukcesu/błędu
- **Filtrowanie i sortowanie**: W tabelach wypożyczeń
- **Kalkulacja kosztów**: Live preview w formularzu wypożyczenia

## Autor

**Paweł Rachocki**
- Email: pawel.rachocki@outlook.com
- GitHub: [github.com/pawel-rachocki/wypozyczalnia-app](https://github.com/pawel-rachocki/wypozyczalnia-app)

## Demo

[Prezentacja działania aplikacji na YouTube](https://www.youtube.com/watch?v=demo_video_id)

## Licencja

Projekt wykonany na zadanie rekrutacyjne. Wszystkie prawa zastrzeżone.

---