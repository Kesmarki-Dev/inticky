# InTicky - Ticket vs. Projekt Kezelés Elemzés

## Probléma Felvetés

Jelenleg a régi rendszerben mind a **kicsi support feladatok**, mind a **nagyobb fejlesztési projektek** ticketként vannak kezelve. A kérdés: **Ez a jó irány az új rendszerben is?**

## Különbségek Elemzése

### Support Ticketek Jellemzői

**Időtartam:**
- Általában rövid (órák, napok)
- Gyors válaszidő szükséges
- Gyors megoldás várható

**Komplexitás:**
- Általában egyszerűbb feladatok
- Jól definiált probléma
- Standard megoldási út

**Munkafolyamat:**
- Egyszerű státuszok (New → In Progress → Resolved → Closed)
- Support agent kezeli
- Ügyfél kommunikáció fontos

**Követelmények:**
- SLA (Service Level Agreement) követés
- Válaszidő mérés
- Ügyfél elégedettség

**Példák:**
- "A login gomb nem működik"
- "Hogyan kell exportálni az adatokat?"
- "Hibajelentés: dátum formátum hiba"

### Fejlesztési Projektek Jellemzői

**Időtartam:**
- Hosszabb (hetek, hónapok)
- Több fázisos
- Hosszú távú tervezés

**Komplexitás:**
- Komplex feladatok
- Több komponens érintett
- Több fejlesztő részvétel

**Munkafolyamat:**
- Projekt fázisok (Planning → Development → Testing → Deployment)
- Projekt menedzsment szükséges
- Több ticket/feladat egy projekthez kapcsolódhat

**Követelmények:**
- Költségkövetés
- Időkövetés (time tracking)
- Árajánlat és rendelés
- Számlázás
- Határidők és milestone-ok

**Példák:**
- "Új modul fejlesztése"
- "API integráció külső szolgáltatással"
- "Teljes redesign és újraírás"

## Megoldási Opciók

### Opció 1: Minden Ticket (Jelenlegi Megközelítés)

**Leírás:** Minden feladat (support és fejlesztés) ticketként van kezelve, de különböző kategóriákkal vagy típusokkal.

**Előnyök:**
- ✅ Egyszerű struktúra
- ✅ Egységes munkafolyamat
- ✅ Könnyű áttekintés (minden egy helyen)
- ✅ Könnyebb migráció (régi rendszerből)
- ✅ Egyszerűbb fejlesztés

**Hátrányok:**
- ❌ Nagy projektek nem illeszkednek jól a ticket modellhez
- ❌ Projekt menedzsment funkciók hiánya
- ❌ Nehezebb költségkövetés
- ❌ Nehezebb időkövetés projektekhez
- ❌ Keveredés a support és fejlesztés között

**Implementáció:**
```sql
tickets
├── type (VARCHAR) -- 'support', 'development', 'feature_request'
├── category_id
├── project_id (NULLABLE) -- Nagyobb projektekhez
└── ...
```

### Opció 2: Külön Projektek és Ticketek

**Leírás:** Support feladatok ticketek, fejlesztési projektek külön projekt entitás. Projektekhez kapcsolódhatnak ticketek (subtasks).

**Előnyök:**
- ✅ Tiszta elkülönítés (support vs. fejlesztés)
- ✅ Projekt menedzsment funkciók (Gantt, milestone-ok)
- ✅ Jobb költségkövetés projektekhez
- ✅ Jobb időkövetés
- ✅ Rugalmasabb struktúra

**Hátrányok:**
- ❌ Komplexebb struktúra
- ❌ Két különböző munkafolyamat
- ❌ Nehezebb áttekintés (két külön lista)
- ❌ Nehezebb migráció
- ❌ Több fejlesztési munka

**Implementáció:**
```sql
-- Support ticketek
tickets
├── type (VARCHAR) -- 'support' csak
└── ...

-- Fejlesztési projektek
projects
├── name
├── status
├── budget
├── timeline
└── ...

-- Projekt ticketek (subtasks)
project_tasks
├── project_id
├── title
├── status
└── ...
```

### Opció 3: Hibrid Megközelítés (Ajánlott)

**Leírás:** Minden feladat ticket, de a ticketek lehetnek:
- **Standalone ticketek** - Support feladatok, kisebb fejlesztések
- **Projekt ticketek** - Nagyobb projektekhez kapcsolódó feladatok

**Előnyök:**
- ✅ Egységes struktúra (minden ticket)
- ✅ Rugalmas (kis és nagy feladatok is)
- ✅ Projekt menedzsment lehetőség (projektekhez kapcsolt ticketek)
- ✅ Könnyebb áttekintés (egy lista, de szűrhető)
- ✅ Könnyebb migráció

**Hátrányok:**
- ❌ Valamennyire komplexebb, mint az Opció 1
- ❌ Projekt funkciók implementálása szükséges

**Implementáció:**
```sql
tickets
├── type (VARCHAR) -- 'support', 'development', 'feature'
├── project_id (NULLABLE) -- Ha projekt része
├── is_standalone (BOOLEAN) -- true ha nem projekt része
└── ...

projects
├── id
├── name
├── status
├── budget
└── ... (projekt specifikus mezők)

-- Projektekhez több ticket kapcsolódhat
-- Egy ticket maximum egy projekthez
```

**Működés:**
- **Support ticketek:** `project_id = NULL`, `type = 'support'`
- **Kisebb fejlesztések:** `project_id = NULL`, `type = 'development'`
- **Projekt feladatok:** `project_id = <project_id>`, `type = 'development'`

### Opció 4: Ticket Típusok + Projekt Kapcsolat

**Leírás:** Minden ticket, de különböző típusokkal és opcionális projekt kapcsolattal.

**Különbség az Opció 3-tól:** Nincs külön "standalone" flag, csak a `project_id` NULL vagy nem NULL.

**Előnyök:**
- ✅ Egyszerűbb, mint az Opció 3
- ✅ Minden ticket, de projekt kapcsolat opcionális
- ✅ Könnyű szűrés (projekt ticketek vs. standalone)

**Hátrányok:**
- ❌ Kevésbé explicit, mint az Opció 3

## Ajánlás: Opció 3 - Hibrid Megközelítés

### Indoklás

1. **Rugalmasság:** Mindkét használati esetet lefedi
2. **Egyszerűség:** Minden ticket, nincs két külön entitás
3. **Skálázhatóság:** Kis és nagy feladatok is kezelhetők
4. **Migráció:** Könnyebb átállás a régi rendszerből
5. **Multi-tenant:** Minden tenant más módon használhatja

### Implementációs Részletek

#### Ticket Típusok

```sql
ticket_types:
- 'support' - Support feladatok (hibajelentések, kérdések)
- 'bug' - Bug javítások (kisebb)
- 'feature' - Feature request (kisebb)
- 'development' - Fejlesztési feladat (kisebb vagy projekt része)
- 'task' - Általános feladat
```

#### Projekt Kapcsolat

- **Standalone ticketek:** `project_id = NULL`
  - Support feladatok
  - Kisebb fejlesztések
  - Gyors javítások
  
- **Projekt ticketek:** `project_id = <project_id>`
  - Nagyobb projektek részei
  - Több ticket egy projekthez
  - Projekt menedzsment funkciók

#### Projekt Funkciók

Projektekhez kapcsolt ticketek esetén:
- Projekt dashboard (összes projekt ticket)
- Projekt státusz (a ticketek alapján számolva)
- Projekt költségkövetés (ticketek időkövetése alapján)
- Projekt timeline (ticketek határidői alapján)
- Projekt jelentések

#### UI/UX Megfontolások

**Ticket lista nézet:**
- Szűrő: "Összes" / "Support" / "Fejlesztés" / "Projekt ticketek"
- Projekt oszlop (ha projekt része)
- Projekt badge/ikon

**Projekt nézet:**
- Projekt részletek
- Projekt ticketek listája
- Projekt statisztikák
- Projekt timeline

**Dashboard:**
- Support ticketek statisztikái
- Projekt statisztikák
- Külön widget-ek

## Döntési Kérdések

A végső döntéshez fontos információk:

1. **Mennyi a support vs. fejlesztési ticketek aránya?**
   - Ha főleg support → Opció 1 vagy 4
   - Ha sok fejlesztés → Opció 2 vagy 3

2. **Mekkora a legnagyobb fejlesztési projekt?**
   - Kis projektek (1-5 nap) → Opció 1 vagy 4
   - Nagy projektek (hetek, hónapok) → Opció 2 vagy 3

3. **Szükséges-e részletes projekt menedzsment?**
   - Nem → Opció 1 vagy 4
   - Igen → Opció 2 vagy 3

4. **Költségkövetés szükséges?**
   - Nem → Opció 1 vagy 4
   - Igen → Opció 2 vagy 3

5. **Időkövetés szükséges?**
   - Nem → Opció 1 vagy 4
   - Igen → Opció 2 vagy 3

## Következő Lépések

1. **Döntés meghozatala** - Melyik opciót választjuk?
2. **Adatbázis séma frissítése** - A választott opció alapján
3. **Funkció lista finomhangolása** - Projekt funkciók részletezése
4. **UI/UX tervezés** - Ticket és projekt nézetek
5. **Migrációs terv frissítése** - Régi adatok leképezése

## Választott Megoldás

✅ **Opció 2 - Külön Projektek és Ticketek** lett kiválasztva.

### Döntés Indoklása
- Tiszta elkülönítés support és fejlesztési feladatok között
- Projekt menedzsment funkciók szükségesek
- Költségkövetés és időkövetés fontos
- Nagyobb projektek komplex kezelése

### Implementációs Következmények
- Support feladatok = Ticketek (project_id = NULL)
- Fejlesztési projektek = Projektek entitás
- Projektekhez kapcsolódhatnak project_tasks (subtasks)
- Két külön munkafolyamat és UI nézet

