# Inticky Backend

Spring Boot 3.2.0 alkalmazás Java 17-tel és AgentInsec integrációval.

## Gyors kezdés

```bash
# Függőségek telepítése és alkalmazás futtatása
mvn clean install
mvn spring-boot:run
```

Az alkalmazás elérhető lesz: `http://localhost:8080/api`

## API Végpontok

### Nyilvános végpontok
- `GET /api/` - Alkalmazás információk
- `GET /api/health` - Egészség ellenőrzés

### Védett végpontok (Basic Auth szükséges)
- `GET /api/users` - Felhasználók listája
- `POST /api/users` - Új felhasználó létrehozása
- `GET /api/users/{id}` - Felhasználó részletei
- `PUT /api/users/{id}` - Felhasználó módosítása
- `DELETE /api/users/{id}` - Felhasználó törlése

## Adatbázis

H2 memória adatbázis használata fejlesztéshez:
- URL: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Felhasználónév: `sa`
- Jelszó: `password`

## Biztonság

Basic Authentication:
- Felhasználónév: `admin`
- Jelszó: `admin123`

## Konfiguráció

Az alkalmazás konfigurációja az `application.yml` fájlban található.

## Fejlesztési jegyzetek

- A kód moduláris felépítésű
- Minden komponens külön fájlban van a jobb karbantarthatóság érdekében
- JPA entitások validációval
- RESTful API tervezés
- Spring Security integráció
