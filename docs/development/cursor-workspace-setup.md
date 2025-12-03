# InTicky - Cursor Workspace Setup Útmutató

## Célközönség

Ez a dokumentum bemutatja, hogyan lehet különböző Cursor agent-eket beállítani különböző területeken dolgozni az InTicky monorepo-ban.

## Áttekintés

Az InTicky monorepo-ban több területet kell kezelni:
- **Backend** - Java Quarkus mikroszolgáltatások
- **Database** - PostgreSQL migrációk és séma
- **Frontend** - React TypeScript alkalmazás
- **Documentation** - Technikai dokumentáció
- **CI/CD & Testing** - Jenkins pipeline és tesztelés

Külön Cursor agent-ek használatával minden terület saját kontextussal és szabályokkal dolgozhat.

## Megoldási Opciók

### Opció 1: Külön Cursor Workspace-ek (Ajánlott) ⭐

**Előnyök:**
- ✅ Teljes izoláció területek között
- ✅ Külön chat history
- ✅ Külön kontextus
- ✅ Párhuzamos munka
- ✅ Jobb teljesítmény (kisebb fájlhalmaz)

**Setup:**

1. **Backend Workspace:**
   ```
   Cursor Window 1
   Workspace: /Users/ncs/Desktop/projects/inticky/backend/
   Chat: "Backend Agent"
   ```

2. **Database Workspace:**
   ```
   Cursor Window 2
   Workspace: /Users/ncs/Desktop/projects/inticky/db/
   Chat: "Database Agent"
   ```

3. **Frontend Workspace:**
   ```
   Cursor Window 3
   Workspace: /Users/ncs/Desktop/projects/inticky/frontend/
   Chat: "Frontend Agent"
   ```

4. **Documentation Workspace:**
   ```
   Cursor Window 4
   Workspace: /Users/ncs/Desktop/projects/inticky/docs/
   Chat: "Documentation Agent"
   ```

5. **CI/CD Workspace:**
   ```
   Cursor Window 5
   Workspace: /Users/ncs/Desktop/projects/inticky/
   Chat: "CI/CD Agent"
   Focus: .github/, deployment/, docker-compose.yml
   ```

**Használat:**
- Nyiss külön Cursor ablakokat
- Minden ablakban nyisd meg a megfelelő mappát
- Külön chat session-ök minden workspace-ben

### Opció 2: .cursorrules Fájlok (Kiegészítő)

Minden területhez saját `.cursorrules` fájl van létrehozva:

- **Root:** `.cursorrules` - Általános szabályok
- **Backend:** `backend/.cursorrules` - Backend specifikus
- **Database:** `db/.cursorrules` - Adatbázis specifikus
- **Frontend:** `frontend/.cursorrules` - Frontend specifikus
- **Docs:** `docs/.cursorrules` - Dokumentáció specifikus
- **CI/CD:** `.github/.cursorrules` - CI/CD specifikus

**Előnyök:**
- ✅ Kontextus-specifikus szabályok
- ✅ Automatikus betöltés a megfelelő mappában
- ✅ Konzisztens coding standards

### Opció 3: Külön Chat Session-ök (Alternatíva)

Ugyanabban a Cursor ablakban, de külön chat-ekkel:

1. **Chat 1:** "Backend Agent"
   - Kijelölt fájlok: `backend/**/*.java`
   - Fókusz: Java, Quarkus, mikroszolgáltatások

2. **Chat 2:** "Database Agent"
   - Kijelölt fájlok: `database/**/*.sql`
   - Fókusz: PostgreSQL, migrációk, séma

3. **Chat 3:** "Frontend Agent"
   - Kijelölt fájlok: `frontend/src/**/*.tsx`
   - Fókusz: React, TypeScript

4. **Chat 4:** "Documentation Agent"
   - Kijelölt fájlok: `docs/**/*.md`
   - Fókusz: Markdown, dokumentáció

5. **Chat 5:** "CI/CD Agent"
   - Kijelölt fájlok: `.github/`, `deployment/`, `docker-compose.yml`
   - Fókusz: Jenkins, Docker, Azure

## Ajánlott Setup

### Kombinált Megközelítés

**1. Külön Cursor Workspace-ek** (fő megoldás)
- Külön ablakok különböző mappákkal
- Teljes izoláció és jobb teljesítmény

**2. .cursorrules fájlok** (kiegészítés)
- Automatikus kontextus betöltés
- Terület-specifikus szabályok

**3. Külön chat session-ök** (opcionális)
- Ugyanabban az ablakban, de külön chat-ek
- Fájl kijelölés alapján

## Lépésről Lépésre Setup

### 1. Backend Workspace Létrehozása

**Lépések:**
1. Nyiss egy új Cursor ablakot
2. `File > Open Folder...`
3. Válaszd ki: `/Users/ncs/Desktop/projects/inticky/backend/`
4. Hozz létre egy új chat-et: "Backend Agent"
5. A `.cursorrules` fájl automatikusan betöltődik

**Használat:**
- Csak backend fájlok láthatók
- Backend specifikus kontextus
- Backend coding standards alkalmazva

### 2. Database Workspace Létrehozása

**Lépések:**
1. Nyiss egy új Cursor ablakot
2. `File > Open Folder...`
3. Válaszd ki: `/Users/ncs/Desktop/projects/inticky/db/`
4. Hozz létre egy új chat-et: "Database Agent"
5. A `.cursorrules` fájl automatikusan betöltődik

**Használat:**
- Csak database fájlok láthatók
- Database specifikus kontextus
- SQL best practices alkalmazva

### 3. Frontend Workspace Létrehozása

**Lépések:**
1. Nyiss egy új Cursor ablakot
2. `File > Open Folder...`
3. Válaszd ki: `/Users/ncs/Desktop/projects/inticky/frontend/`
4. Hozz létre egy új chat-et: "Frontend Agent"
5. A `.cursorrules` fájl automatikusan betöltődik

**Használat:**
- Csak frontend fájlok láthatók
- React/TypeScript specifikus kontextus
- Frontend coding standards alkalmazva

### 4. Documentation Workspace Létrehozása

**Lépések:**
1. Nyiss egy új Cursor ablakot
2. `File > Open Folder...`
3. Válaszd ki: `/Users/ncs/Desktop/projects/inticky/docs/`
4. Hozz létre egy új chat-et: "Documentation Agent"
5. A `.cursorrules` fájl automatikusan betöltődik

**Használat:**
- Csak dokumentáció fájlok láthatók
- Markdown specifikus kontextus
- Dokumentáció írási szabályok alkalmazva

### 5. CI/CD Workspace Létrehozása

**Lépések:**
1. Nyiss egy új Cursor ablakot
2. `File > Open Folder...`
3. Válaszd ki: `/Users/ncs/Desktop/projects/inticky/`
4. Hozz létre egy új chat-et: "CI/CD Agent"
5. Kijelölés: `.github/`, `deployment/`, `docker-compose.yml`
6. A `.cursorrules` fájl automatikusan betöltődik

**Használat:**
- CI/CD konfigurációk láthatók
- DevOps specifikus kontextus
- Pipeline és deployment szabályok alkalmazva

## .cursorrules Fájlok Tartalma

### Root .cursorrules
- Általános projekt kontextus
- Technológiai stack áttekintés
- Projekt struktúra

### backend/.cursorrules
- Java Quarkus best practices
- Multi-tenant izoláció szabályok
- Service struktúra
- Modul aktiválás

### db/.cursorrules
- PostgreSQL best practices
- Multi-tenant adatizoláció
- RLS policy-k
- Migrációk szabályai

### frontend/.cursorrules
- React TypeScript best practices
- Komponens struktúra
- State management
- API integráció

### docs/.cursorrules
- Markdown formátum
- Dokumentáció struktúra
- Kód példák követelményei
- Linkelési szabályok

### .github/.cursorrules
- Jenkins pipeline szabályok
- Docker best practices
- Testing stratégiák
- Git workflow

## Használati Példák

### Backend Agent Használata

**Workspace:** `backend/`

**Példa kérés:**
```
"Készíts egy új ticket-service endpoint-ot, ami listázza a ticketeket tenant alapján"
```

**Agent válasz:**
- Használja a backend/.cursorrules szabályokat
- Multi-tenant izolációt alkalmazza
- Quarkus best practices-et követi
- Panache repository-t használ

### Database Agent Használata

**Workspace:** `database/`

**Példa kérés:**
```
"Készíts egy migrációt, ami hozzáad egy új indexet a tickets táblához"
```

**Agent válasz:**
- Használja a database/.cursorrules szabályokat
- Tenant ID-t mindig figyelembe veszi
- Flyway naming konvenciót követi
- RLS policy-ket ellenőrzi

### Documentation Agent Használata

**Workspace:** `docs/`

**Példa kérés:**
```
"Frissítsd a backend setup dokumentációt az új Qdrant konfigurációval"
```

**Agent válasz:**
- Használja a docs/.cursorrules szabályokat
- Markdown formátumot követi
- Kód példákat tartalmaz
- Linkeket ad hozzá

## Best Practices

### 1. Workspace Választás

**Használj külön workspace-eket, ha:**
- ✅ Különböző területeken dolgozol párhuzamosan
- ✅ Nagy fájlhalmazokkal dolgozol
- ✅ Teljes izolációt szeretnél

**Használj ugyanazt a workspace-t, ha:**
- ✅ Kereszt-területi változtatásokat végzel
- ✅ Kisebb módosításokat csinálsz
- ✅ Gyors váltás kell területek között

### 2. Chat Session Kezelés

**Tippek:**
- Nevezd el a chat-eket: "Backend Agent", "Database Agent", stb.
- Használj külön chat-eket különböző feladatokhoz
- Töröld a régi chat-eket, ha már nem relevánsak

### 3. Fájl Kijelölés

**Agent mode-ban:**
- Kijelölhetsz specifikus fájlokat/mappákat
- Az agent csak a kijelölt fájlokra fókuszál
- Hasznos, ha csak egy részen akarsz dolgozni

### 4. Kontextus Kezelés

**Tippek:**
- A `.cursorrules` fájlok automatikusan betöltődnek
- Manuálisan is hivatkozhatsz dokumentációkra
- Használj @ fájl hivatkozásokat a chat-ben

## Troubleshooting

### Problem: Agent nem használja a .cursorrules fájlt

**Megoldás:**
1. Ellenőrizd, hogy a fájl a megfelelő mappában van-e
2. Restart Cursor
3. Nyisd meg újra a workspace-t

### Problem: Túl sok fájl látható

**Megoldás:**
1. Használj külön workspace-eket
2. Vagy használj fájl kijelölést agent mode-ban
3. Vagy használj `.cursorignore` fájlt

### Problem: Kontextus elveszik

**Megoldás:**
1. Használj külön chat session-öket
2. Nevezd el a chat-eket
3. Hivatkozz dokumentációkra @ fájl hivatkozással

## További Információk

- [Cursor dokumentáció](https://docs.cursor.com/)
- [Backend Setup](./backend/setup.md)
- [Frontend Setup](./frontend/setup.md)
- [Database Setup](./database/setup.md)

