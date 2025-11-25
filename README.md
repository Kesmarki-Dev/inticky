# Inticky Project

Ez egy Spring Boot alapú alkalmazás AgentInsec integrációval.

## Projekt Struktúra

```
inticky/
├── backend/          # Spring Boot alkalmazás
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
└── README.md
```

## Backend (Spring Boot)

A backend egy Spring Boot 3.2.0 alkalmazás Java 17-tel.

### Futtatás

```bash
cd backend
mvn spring-boot:run
```

### Elérhető végpontok

- `GET /` - Alkalmazás információk
- `GET /health` - Egészség ellenőrzés
- `GET /h2-console` - H2 adatbázis konzol (fejlesztéshez)

### Alapértelmezett bejelentkezés

- Felhasználónév: `admin`
- Jelszó: `admin123`

### Technológiák

- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- H2 Database (memóriában)
- Maven

## Fejlesztés

A projekt moduláris felépítésű, könnyen bővíthető további komponensekkel (frontend, microservice-ek, stb.).
