# InTicky - Migrációs Terv

## Áttekintés

Ez a dokumentum a régi Delphi 2009-es ticketing rendszerből az új InTicky webes rendszerbe való migráció tervét tartalmazza.

## Régi Rendszer Elemzése

### Adatbázis Struktúra Elemzése

**Feladat:** A régi Sybase SQL Anywhere adatbázis struktúrájának részletes elemzése.

**Szükséges információk:**
- [ ] Táblák listája és struktúrája
- [ ] Kapcsolatok (foreign keys)
- [ ] Indexek
- [ ] Adatok mennyisége (rekordok száma)
- [ ] Egyedi mezők és funkciók
- [ ] Adatminőség (hiányzó adatok, duplikációk)

**Eszközök:**
- Sybase SQL Anywhere adatbázis kapcsolat
- Schema export eszközök
- Adatbázis dokumentáció (ha van)

### Funkció Elemzése

**Feladat:** A régi rendszer funkcióinak dokumentálása.

**Szükséges információk:**
- [ ] Felhasználói szerepkörök
- [ ] Ticket munkafolyamatok
- [ ] Jelentések és statisztikák
- [ ] Egyedi funkciók
- [ ] Integrációk
- [ ] Beállítások és konfigurációk

**Források:**
- Régi Delphi forráskód (ha elérhető)
- Felhasználói dokumentáció
- Felhasználói interjúk
- Használati minták elemzése

## Migrációs Stratégia

### Fázisok

#### 1. Fázis: Elemzés és Tervezés
- [ ] Régi adatbázis struktúra dokumentálása
- [ ] Adatleképezés tervezése (régi -> új struktúra)
- [ ] Funkció leképezés tervezése
- [ ] Migrációs scriptek tervezése
- [ ] Tesztelési terv készítése

#### 2. Fázis: Fejlesztés
- [ ] Migrációs scriptek fejlesztése
- [ ] Adatvalidációs scriptek
- [ ] Adattisztítási scriptek
- [ ] Rollback mechanizmus

#### 3. Fázis: Tesztelés
- [ ] Teszt adatbázis migrálása
- [ ] Adatintegritás ellenőrzése
- [ ] Funkciók tesztelése
- [ ] Teljesítmény tesztelés
- [ ] Felhasználói tesztelés

#### 4. Fázis: Átállás
- [ ] Backup készítése
- [ ] Adatok migrálása
- [ ] Validáció
- [ ] Go-live
- [ ] Monitoring

### Migrációs Módszerek

#### 1. Big Bang Migráció
**Leírás:** Egyszerre történik az átállás, nincs párhuzamos működés.

**Előnyök:**
- ✅ Egyszerűbb implementáció
- ✅ Nincs adatszinkronizáció szükség
- ✅ Gyorsabb befejezés

**Hátrányok:**
- ❌ Nagyobb kockázat
- ❌ Nincs visszaállási lehetőség
- ❌ Minden felhasználó egyszerre vált

**Ajánlás:** Kis adatmennyiség esetén

#### 2. Fázisos Migráció
**Leírás:** Fokozatosan történik az átállás, párhuzamos működés lehet.

**Előnyök:**
- ✅ Kisebb kockázat
- ✅ Fokozatos tesztelés
- ✅ Visszaállási lehetőség

**Hátrányok:**
- ❌ Komplexebb implementáció
- ❌ Adatszinkronizáció szükséges
- ❌ Hosszabb időtartam

**Ajánlás:** Nagy adatmennyiség vagy kritikus rendszer esetén

#### 3. Dual-Write Migráció
**Leírás:** Mindkét rendszerbe írunk, de csak az egyikből olvasunk.

**Előnyök:**
- ✅ Könnyebb visszaállás
- ✅ Fokozatos váltás
- ✅ Adatszinkronizáció automatikus

**Hátrányok:**
- ❌ Komplex implementáció
- ❌ Dupla írási műveletek
- ❌ Konzisztencia kezelés

**Ajánlás:** Kritikus rendszerek esetén

### Választott Módszer

**Ajánlás:** Fázisos migráció + Big Bang kombináció

1. **Előkészítés:** Adatok migrálása, tesztelés
2. **Soft launch:** Korlátozott felhasználókkal
3. **Fokozatos bővítés:** Több felhasználó átállítása
4. **Full migration:** Mindenki átállítása
5. **Régi rendszer leállítása**

## Adat Migráció

### Adatleképezés

#### Felhasználók (Users)

**Régi struktúra -> Új struktúra:**
```
Régi: users
├── user_id
├── username
├── password (hashelt)
├── email
├── full_name
└── role

Új: users
├── id (UUID generálás)
├── tenant_id (új tenant létrehozása vagy meglévő)
├── email
├── password_hash (átalakítás szükséges lehet)
├── first_name, last_name (full_name split)
├── role (mapping szükséges)
└── created_at (régi created_date vagy most)
```

**Migrációs lépések:**
1. Régi felhasználók exportálása
2. Tenant létrehozása (ha multi-tenant)
3. Jelszavak kezelése (reset vagy hash konverzió)
4. Felhasználók importálása
5. Email validáció

#### Ticketek (Support Feladatok)

**Megjegyzés:** Opció 2 alapján csak support feladatok lesznek ticketek. Fejlesztési projektek külön migrálódnak.

**Régi struktúra -> Új struktúra:**
```
Régi: tickets
├── ticket_id
├── ticket_number
├── title
├── description
├── status
├── priority
├── category
├── type (support/development) -- FONTOS: szűrés szükséges!
├── assigned_to
├── created_by
├── created_date
└── updated_date

Új: tickets (csak support típusúak!)
├── id (UUID)
├── tenant_id
├── ticket_number (formátum konverzió lehet szükséges)
├── title
├── description
├── type (DEFAULT 'support')
├── status (mapping szükséges)
├── priority (mapping szükséges)
├── category_id (kategória lookup)
├── assigned_to_id (user lookup)
├── created_by_id (user lookup)
├── created_at
└── updated_at
```

**Migrációs lépések:**
1. Régi ticketek szűrése (csak support típusúak)
2. Kategóriák migrálása
3. Státuszok migrálása
4. Support ticketek migrálása
5. Kapcsolatok létrehozása (assigned_to, created_by)
6. Ticket számok validálása

#### Projektek (Fejlesztési Projektek)

**Régi struktúra -> Új struktúra:**
```
Régi: tickets (development típusúak)
├── ticket_id
├── ticket_number
├── title
├── description
├── status
├── priority
├── category
├── project_id (ha volt)
├── assigned_to
├── created_by
├── budget (ha volt)
├── created_date
└── updated_date

Új: projects
├── id (UUID)
├── tenant_id
├── project_number (új generálás: PRJ-001)
├── name (title-ből)
├── description
├── status (mapping szükséges)
├── customer_id (user lookup)
├── manager_id (assigned_to lookup)
├── budget (ha volt)
├── created_at
└── updated_at
```

**Migrációs lépések:**
1. Régi ticketek szűrése (csak development típusúak)
2. Projektek létrehozása (development ticketekből)
3. Projekt számok generálása
4. Projekt státuszok mapping
5. Projekt kapcsolatok (customer, manager)

#### Projekt Feladatok (Project Tasks)

**Régi struktúra -> Új struktúra:**
```
Régi: tickets (development típusúak, ha volt project_id)
├── ticket_id
├── title
├── description
├── status
├── priority
├── assigned_to
└── ...

Új: project_tasks
├── id (UUID)
├── tenant_id
├── project_id (projekt lookup)
├── task_number (PRJ-001-001 formátum)
├── title
├── description
├── status (mapping szükséges)
├── priority (mapping szükséges)
├── assigned_to_id (user lookup)
├── created_at
└── updated_at
```

**Migrációs lépések:**
1. Régi development ticketek azonosítása (project_id alapján)
2. Projekt feladatok létrehozása
3. Feladat számok generálása
4. Kapcsolatok létrehozása

#### Kommentek

**Megjegyzés:** Opció 2 alapján a kommentek lehetnek ticketekhez (support) és projekt feladatokhoz is.

**Régi struktúra -> Új struktúra:**
```
Régi: comments
├── comment_id
├── ticket_id
├── user_id
├── content
├── created_date
└── is_internal

Új: comments (ticketekhez)
├── id (UUID)
├── tenant_id
├── ticket_id (lookup - csak support ticketek)
├── user_id (lookup)
├── content
├── created_at
└── is_internal
```

**Új: project_task_comments (projekt feladatokhoz)**
```
project_task_comments
├── id (UUID)
├── tenant_id
├── project_task_id (lookup)
├── user_id (lookup)
├── content
├── created_at
└── is_internal
```

**Migrációs lépések:**
1. Kommentek szűrése (ticket típus alapján)
2. Support ticket kommentek migrálása → comments
3. Development ticket kommentek migrálása → project_task_comments (ha project_task-hoz kapcsolódnak)
4. Kapcsolatok létrehozása

#### Csatolmányok

**Feladat:** Fájlok migrálása.

**Lehetőségek:**
1. Fájlok másolása új helyre
2. Fájl path-ok frissítése
3. Metadata migrálása

**Figyelembe veendő:**
- Fájl elérési útvonalak
- Fájl tárolási hely (lokális, S3, stb.)
- Fájl integritás ellenőrzés

### Adattisztítás

**Feladatok:**
- [ ] Duplikált rekordok eltávolítása
- [ ] Hiányzó kötelező mezők kitöltése
- [ ] Érvénytelen adatok javítása
- [ ] Kapcsolatok validálása (orphan records)
- [ ] Adatkonzisztencia ellenőrzés

### Adatvalidáció

**Ellenőrzések:**
- [ ] Rekordok száma (régi vs. új)
- [ ] Kritikus mezők kitöltöttsége
- [ ] Foreign key kapcsolatok
- [ ] Adatintegritás
- [ ] Üzleti logika validáció

## Funkció Migráció

### Funkció Leképezés

**Feladat:** Régi funkciók -> Új funkciók mapping.

**Példa:**
```
Régi funkció                    -> Új funkció
─────────────────────────────────────────────────
Ticket létrehozás              -> Ticket létrehozás (webes)
Ticket státusz változtatás     -> Ticket státusz módosítás
Felhasználó hozzárendelés       -> Ticket hozzárendelés
Email értesítések               -> Email értesítések (új rendszer)
Jelentések                      -> Dashboard és jelentések
```

### Hiányzó Funkciók

**Feladat:** Azonosítani, mely funkciók nem lesznek azonnal elérhetőek.

**Stratégia:**
1. Kritikus funkciók prioritása
2. Alternatív megoldások
3. Fejlesztési roadmap

### Új Funkciók

**Feladat:** Azonosítani, milyen új funkciókat adunk hozzá.

**Példák:**
- Real-time értesítések
- Modern UI/UX
- Mobil responsive design
- API hozzáférés
- Webhook-ok
- Advanced keresés

## Migrációs Scriptek

### Script Struktúra

```
migration/
├── 01_extract_data.sql          # Régi adatbázisból export
├── 02_transform_data.js         # Adatátalakítás
├── 03_validate_data.js           # Adatvalidáció
├── 04_import_users.js            # Felhasználók import
├── 05_import_categories.js       # Kategóriák import
├── 06_import_tickets.js          # Ticketek import
├── 07_import_comments.js         # Kommentek import
├── 08_import_attachments.js      # Csatolmányok import
├── 09_verify_integrity.js        # Integritás ellenőrzés
└── 10_rollback.js                # Visszaállítás
```

### Script Követelmények

- **Idempotens** - Többszöri futtatás biztonságos
- **Logging** - Részletes naplózás
- **Error handling** - Hibakezelés és visszaállítás
- **Progress tracking** - Előrehaladás követése
- **Dry-run mode** - Tesztelés mód

## Tesztelési Terv

### Tesztelési Fázisok

#### 1. Unit Tesztek
- [ ] Migrációs scriptek tesztelése
- [ ] Adatátalakítás logika tesztelése
- [ ] Validációs szabályok tesztelése

#### 2. Integration Tesztek
- [ ] Teljes migrációs folyamat tesztelése
- [ ] Adatintegritás ellenőrzése
- [ ] Teljesítmény tesztelése

#### 3. User Acceptance Tesztek
- [ ] Felhasználói funkciók tesztelése
- [ ] Adatok helyességének ellenőrzése
- [ ] Munkafolyamatok tesztelése

#### 4. Load Tesztek
- [ ] Nagy adatmennyiség migrálása
- [ ] Teljesítmény ellenőrzése
- [ ] Skálázhatóság tesztelése

### Teszt Adatok

- [ ] Régi adatbázis másolata
- [ ] Teszt tenant létrehozása
- [ ] Szintetikus adatok generálása (ha szükséges)

## Rollback Terv

### Visszaállítási Stratégia

1. **Backup** - Régi adatbázis teljes backup
2. **Snapshot** - Új adatbázis snapshot migráció előtt
3. **Rollback script** - Automatikus visszaállítás
4. **Kommunikáció** - Felhasználók értesítése

### Rollback Trigger Pontok

- Adatintegritás hiba
- Kritikus funkció hiánya
- Teljesítmény problémák
- Felhasználói visszajelzések

## Kommunikációs Terv

### Érintett Felek

- Fejlesztő csapat
- Support csapat
- Ügyfelek
- Management

### Kommunikáció Fázisok

1. **Előzetes értesítés** - Migráció bejelentése
2. **Készültség** - Tesztelés és képzés
3. **Migráció nap** - Értesítés és útmutatás
4. **Poszt-migráció** - Feedback és support

## Időzítés és Erőforrások

### Időbecslés

- **Elemzés:** 1-2 hét
- **Fejlesztés:** 2-4 hét
- **Tesztelés:** 1-2 hét
- **Migráció:** 1 nap
- **Poszt-migráció support:** 1 hét

### Szükséges Erőforrások

- Backend fejlesztő
- Database adminisztrátor
- QA tesztelő
- Projekt menedzser
- Support csapat

## Kockázatok és Kezelésük

### Fő Kockázatok

1. **Adatvesztés**
   - **Kockázat:** Magas
   - **Kezelés:** Többszörös backup, validáció

2. **Funkció hiánya**
   - **Kockázat:** Közepes
   - **Kezelés:** Funkció mapping, alternatív megoldások

3. **Teljesítmény problémák**
   - **Kockázat:** Közepes
   - **Kezelés:** Load tesztelés, optimalizálás

4. **Felhasználói ellenállás**
   - **Kockázat:** Alacsony
   - **Kezelés:** Képzés, dokumentáció, support

## Következő Lépések

1. **Régi adatbázis elérése** - Kapcsolat létrehozása
2. **Schema export** - Struktúra dokumentálása
3. **Adatminta export** - Teszteléshez
4. **Funkció lista bővítése** - Régi rendszer alapján
5. **Migrációs scriptek tervezése** - Részletes specifikáció
6. **Tesztelési környezet** - Setup

