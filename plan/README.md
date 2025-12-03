# InTicky - Tervezési Dokumentáció

Ez a mappa tartalmazza az InTicky ticketing rendszer tervezési dokumentációját.

## Dokumentumok

### 📋 [00_projekt_attekintes.md](./00_projekt_attekintes.md)
A projekt áttekintése, céljai, kihívások és főbb jellemzők.

### 📝 [01_funkcio_lista.md](./01_funkcio_lista.md)
Részletes funkció lista, amely tartalmazza:
- Felhasználói autentikáció és engedélyezés
- Ticket kezelés
- Kommentárok és kommunikáció
- Felhasználó és csapat kezelés
- Kategóriák és konfiguráció
- Fájlkezelés
- Jelentések és statisztikák
- Multi-tenant funkciók
- És még sok más...

### 🔧 [02_technikai_kovetelmenyek.md](./02_technikai_kovetelmenyek.md)
Technológiai stack ajánlások és követelmények:
- Backend opciók (Node.js, Python, Java, Go)
- Frontend opciók (React, Vue, Angular)
- Adatbázis választás
- Deployment stratégia
- Biztonsági követelmények
- Monitoring és logging

### 🗄️ [03_adatbazis_tervezes.md](./03_adatbazis_tervezes.md)
Adatbázis struktúra tervezés:
- Főbb entitások (Users, Tickets, Comments, stb.)
- Multi-tenant modell
- Indexelési stratégia
- Biztonsági megfontolások
- Teljesítmény optimalizálás

### 🏢 [04_multi_tenant_architektura.md](./04_multi_tenant_architektura.md)
Multi-tenant architektúra részletes tervezése:
- Tenant azonosítási módszerek
- Adatizoláció stratégia
- Biztonsági implementáció
- Tenant onboarding folyamat
- Quota és limitációk

### 🔄 [05_migracios_terv.md](./05_migracios_terv.md)
Migrációs terv a régi Delphi rendszerből:
- Régi rendszer elemzése
- Adat migrációs stratégia
- Funkció leképezés
- Tesztelési terv
- Rollback terv

### 🤔 [06_ticket_vs_projekt_kezeles.md](./06_ticket_vs_projekt_kezeles.md)
Ticket vs. Projekt kezelés elemzése:
- Support ticketek vs. Fejlesztési projektek különbségei
- Megoldási opciók összehasonlítása
- ✅ Választott: Opció 2 - Külön Projektek és Ticketek
- Döntési kérdések

### 📁 [07_monorepo_struktura.md](./07_monorepo_struktura.md)
Monorepo struktúra tervezése:
- Repository mappák (frontend, backend, db, docs)
- Docker Compose setup
- CI/CD konfiguráció
- Git workflow

### 🧩 [08_modularis_felepites.md](./08_modularis_felepites.md)
Moduláris felépítés elemzése:
- Moduláris vs. Mikroszolgáltatások
- Modul aktiválás stratégiák
- Bonyolultság értékelés
- ✅ Választott: Opció 1 - Moduláris Mikroszolgáltatások

### 🚀 [09_kezdesi_utmutato.md](./09_kezdesi_utmutato.md)
Kezdési útmutató és roadmap:
- Projekt inicializálás lépései
- Repository és CI/CD setup
- Dokumentáció struktúra
- Fejlesztési fázisok
- Egyedi deployment modul (később)

## Dokumentumok Olvasási Sorrendje

1. **00_projekt_attekintes.md** - Kezdés itt, hogy megértsd a projekt céljait
2. **01_funkcio_lista.md** - Nézd meg, milyen funkciók lesznek
3. **06_ticket_vs_projekt_kezeles.md** - ✅ Döntve: Opció 2 - Külön Projektek és Ticketek
4. **02_technikai_kovetelmenyek.md** - ✅ Technológiai döntések meghozva
5. **08_modularis_felepites.md** - ✅ Döntve: Opció 1 - Moduláris Mikroszolgáltatások
6. **09_kezdesi_utmutato.md** - 🚀 **KEZDÉS ITT!** - Projekt inicializálás és fejlesztés indítása
7. **07_monorepo_struktura.md** - Repository struktúra tervezése
8. **03_adatbazis_tervezes.md** - Adatbázis struktúra
9. **04_multi_tenant_architektura.md** - Multi-tenant modell
10. **05_migracios_terv.md** - Migrációs stratégia

## Következő Lépések

### Azonnali Feladatok (Kezdés)

**Tervezés:**
- [x] **Döntés: Ticket vs. Projekt kezelés** ✅ Opció 2
- [x] **Technológiai stack döntések** ✅ Meghozva (React, Java, PostgreSQL, Azure, stb.)
- [x] **Backend framework döntés** ✅ Quarkus
- [x] **Moduláris felépítés döntés** ✅ Opció 1 - Moduláris Mikroszolgáltatások

**Fejlesztés indítása:**
- [ ] **Repository létrehozása** 🚀 Első lépés!
- [ ] **Monorepo struktúra** - Mappák létrehozása
- [ ] **Docker Compose setup** - Lokális fejlesztés
- [ ] **Backend projekt inicializálás** - Java (Quarkus)
- [ ] **Frontend projekt inicializálás** - React
- [ ] **Jenkins CI/CD pipeline** - Build és test
- [ ] **Dokumentáció kezdete** - Getting started útmutatók
- [ ] **AgentInSec AI Library integráció** - AI agent service setup

**Későbbi feladatok:**
- [x] **Backend framework döntés** ✅ Quarkus
- [x] **AI Agent integráció** ✅ AgentInSec AI Library v3.5.0
- [ ] **Modul lista véglegesítése** ⚠️ Melyik modulok lesznek és aktiválás stratégia (AI Agent modul hozzáadva)
- [ ] Régi Delphi rendszer adatbázis struktúrájának elemzése
- [ ] Régi rendszer funkcióinak dokumentálása
- [ ] **Egyedi deployment modul specifikáció** - Később

### Rövidesen
- [ ] API design részletes tervezése
- [ ] Frontend komponens struktúra tervezése
- [ ] Development environment setup
- [ ] CI/CD pipeline tervezése

## Dokumentumok Frissítése

Ez a tervezési fázis, így a dokumentumok folyamatosan frissülnek és bővülnek. 

**Fontos:** Minden változtatás előtt érdemes megbeszélni a csapattal.

## Megjegyzések

- A dokumentumok markdown formátumban vannak, könnyen szerkeszthetők
- Checklist-ek jelzik a befejezett feladatokat
- Ajánlások és opciók vannak felsorolva, ahol döntés szükséges
- Minden dokumentum tartalmaz "Következő lépések" részt

## Kapcsolat

Kérdések esetén vagy ha új tervezési elemeket szeretnél hozzáadni, vedd fel a kapcsolatot a projekt csapattal.

