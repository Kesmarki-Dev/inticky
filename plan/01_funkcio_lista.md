# InTicky - Funkció Lista

## 1. Felhasználói Autentikáció és Engedélyezés

### 1.1 Bejelentkezés
- [ ] Email/jelszó alapú bejelentkezés
- [ ] Jelszó visszaállítás
- [ ] Multi-tenant: Tenant kiválasztása bejelentkezéskor
- [ ] Session kezelés
- [ ] Kétfaktoros autentikáció (2FA) - opcionális

### 1.2 Felhasználói Szerepkörök
- [ ] Admin (rendszergazda)
- [ ] Support Manager
- [ ] Support Agent
- [ ] Ügyfél (Customer)
- [ ] Fejlesztő (Developer)
- [ ] Szerepkör alapú engedélyek (RBAC)

## 2. Ticket Kezelés (Support Feladatok)

**Megjegyzés:** Opció 2 alapján a ticketek csak support feladatokhoz használatosak. Fejlesztési projektek külön kezelés (lásd: Projekt Kezelés).

### 2.1 Ticket Létrehozás
- [ ] Új ticket felvétele (support, bug, question, feature_request típusok)
- [ ] Ticket kategóriák (Support, Bug Report, Question, Feature Request, stb.)
- [ ] Prioritás beállítása (Low, Medium, High, Critical)
- [ ] Fájl csatolmányok feltöltése
- [ ] Ticket leírás formázott szöveg (rich text)
- [ ] Screenshot feltöltés
- [ ] Ticket címkék (tags)

### 2.2 Ticket Műveletek
- [ ] Ticket megnyitása
- [ ] Ticket lezárása
- [ ] Ticket újranyitása
- [ ] Ticket státusz változtatása (New, In Progress, Resolved, Closed, stb.)
- [ ] Ticket hozzárendelése fejlesztőhöz/agenthoz
- [ ] Ticket prioritás módosítása
- [ ] Ticket kategória módosítása
- [ ] Ticket törlése (soft delete)

### 2.3 Ticket Megjelenítés és Listázás
- [ ] Ticket lista nézet
- [ ] Ticket részletes nézet
- [ ] Szűrés (státusz, prioritás, kategória, hozzárendelt, dátum)
- [ ] Keresés (cím, leírás, kommentek)
- [ ] Rendezés (dátum, prioritás, státusz)
- [ ] Pagináció
- [ ] Dashboard nézet (statisztikák)

## 3. Kommentárok és Kommunikáció

### 3.1 Kommentárok
- [ ] Komment hozzáadása tickethez
- [ ] Komment szerkesztése
- [ ] Komment törlése
- [ ] Formázott szöveg kommentekben
- [ ] Fájl csatolmányok kommentekben
- [ ] @mention más felhasználókat
- [ ] Komment időbélyegzők

### 3.2 Értesítések
- [ ] Email értesítések (új ticket, hozzárendelés, komment, stb.)
- [ ] In-app értesítések
- [ ] Értesítési beállítások (preferenciák)
- [ ] Email sablonok testreszabása

## 4. Felhasználó és Csapat Kezelés

### 4.1 Felhasználók
- [ ] Felhasználó regisztráció (multi-tenant)
- [ ] Felhasználó profil kezelés
- [ ] Felhasználó szerkesztése
- [ ] Felhasználó törlése/deaktiválása
- [ ] Felhasználó lista
- [ ] Felhasználó szerepkörök kezelése

### 4.2 Csapatok
- [ ] Csapat létrehozása
- [ ] Csapat tagok hozzáadása/eltávolítása
- [ ] Csapat alapú ticket hozzárendelés
- [ ] Csapat statisztikák

## 5. Kategóriák és Konfiguráció

### 5.1 Kategóriák
- [ ] Ticket kategóriák kezelése
- [ ] Kategória hierarchia (opcionális)
- [ ] Kategória alapú routing (automatikus hozzárendelés)

### 5.2 Prioritások
- [ ] Prioritás szintek kezelése
- [ ] Prioritás alapú SLA (Service Level Agreement)

### 5.3 Státuszok
- [ ] Ticket státuszok kezelése
- [ ] Egyedi státuszok létrehozása
- [ ] Státusz workflow konfigurálása

### 5.4 Címkék (Tags)
- [ ] Címkék kezelése
- [ ] Címke alapú szűrés és csoportosítás

## 6. Fájlkezelés

### 6.1 Csatolmányok
- [ ] Fájl feltöltés (tickethez, kommenthez)
- [ ] Fájl letöltés
- [ ] Fájl törlés
- [ ] Fájl méret korlátok
- [ ] Engedélyezett fájltípusok
- [ ] Fájl preview (képek, PDF)

## 7. Jelentések és Statisztikák

### 7.1 Dashboard
- [ ] Nyitott ticketek száma
- [ ] Ticketek státusz szerint
- [ ] Ticketek prioritás szerint
- [ ] Átlagos válaszidő
- [ ] Átlagos megoldási idő
- [ ] Felhasználó/fejlesztő teljesítmény statisztikák

### 7.2 Jelentések
- [ ] Ticket jelentések generálása
- [ ] Időszak alapú statisztikák
- [ ] Exportálás (CSV, PDF)
- [ ] Egyedi jelentések létrehozása

## 8. Keresés és Szűrés

### 8.1 Keresés
- [ ] Teljes szöveges keresés
- [ ] Speciális keresési operátorok
- [ ] Keresési előzmények
- [ ] Mentett keresések

### 8.2 Szűrés
- [ ] Többkritériumos szűrés
- [ ] Mentett szűrők
- [ ] Gyors szűrők (saját ticketek, hozzárendelt ticketek, stb.)

## 9. Multi-Tenant Funkciók

### 9.1 Tenant Kezelés
- [ ] Tenant regisztráció
- [ ] Tenant beállítások
- [ ] Tenant branding (logo, színek)
- [ ] Tenant adatok izolálása
- [ ] Tenant adminisztráció

### 9.2 Tenant Konfiguráció
- [ ] Egyedi kategóriák per tenant
- [ ] Egyedi státuszok per tenant
- [ ] Egyedi munkafolyamatok per tenant
- [ ] Tenant specifikus beállítások

## 10. Projekt Kezelés (Fejlesztési Projektek)

**Megjegyzés:** Opció 2 alapján a projektek külön entitások, nem kapcsolódnak ticketekhez. A projektekhez project_tasks kapcsolódnak.

### 10.1 Projekt Létrehozás és Műveletek
- [ ] Új projekt létrehozása
- [ ] Projekt szerkesztése
- [ ] Projekt törlése (soft delete)
- [ ] Projekt státusz követés (planning, in_progress, on_hold, completed, cancelled)
- [ ] Projekt részletes nézet
- [ ] Projekt lista nézet
- [ ] Projekt szűrés (státusz, customer, manager, dátum)
- [ ] Projekt keresés

### 10.2 Projekt Feladatok (Project Tasks)
- [ ] Projekt feladat létrehozása
- [ ] Projekt feladat szerkesztése
- [ ] Projekt feladat törlése
- [ ] Projekt feladat státusz változtatása (todo, in_progress, review, done, blocked)
- [ ] Projekt feladat hozzárendelése fejlesztőhöz
- [ ] Projekt feladat prioritás beállítása
- [ ] Projekt feladat sorrendjének módosítása (drag & drop)
- [ ] Projekt feladat határidő beállítása
- [ ] Projekt feladatok listázása (projektenként)
- [ ] Projekt feladat részletes nézet

### 10.3 Projekt Menedzsment
- [ ] Projekt költségkövetés (budget, actual)
- [ ] Projekt időkövetés (estimated hours, actual hours)
- [ ] Projekt határidők kezelése (start_date, end_date, due_date)
- [ ] Projekt ügyfél hozzárendelése
- [ ] Projekt menedzser hozzárendelése
- [ ] Projekt csapat tagok hozzáadása
- [ ] Projekt milestone-ok (opcionális)
- [ ] Projekt Gantt diagram (opcionális)

### 10.4 Projekt Statisztikák és Jelentések
- [ ] Projekt státusz szerinti statisztikák
- [ ] Projekt feladatok teljesítési aránya
- [ ] Projekt időkövetés statisztikák
- [ ] Projekt költség statisztikák
- [ ] Projekt jelentések generálása
- [ ] Projekt exportálás (CSV, PDF)

### 10.5 Árajánlat Kezelés
- [ ] Árajánlat létrehozása
- [ ] Árajánlat szerkesztése
- [ ] Árajánlat státusz követés (draft, sent, accepted, rejected, expired)
- [ ] Árajánlat projekthez kapcsolása
- [ ] Árajánlat érvényességi dátum beállítása
- [ ] Árajánlat PDF generálása
- [ ] Árajánlat küldése ügyfélnek

### 10.6 Rendelés Kezelés
- [ ] Rendelés létrehozása (árajánlatból vagy közvetlenül)
- [ ] Rendelés szerkesztése
- [ ] Rendelés státusz követés (pending, confirmed, in_progress, completed, cancelled)
- [ ] Fizetési státusz követés (unpaid, partial, paid)
- [ ] Fizetés rögzítése
- [ ] Rendelés projekthez kapcsolása
- [ ] Rendelés PDF generálása

### 10.7 Időkövetés
- [ ] Idő rögzítése projekthez
- [ ] Idő rögzítése projekt feladathoz
- [ ] Idő szerkesztése
- [ ] Idő törlése
- [ ] Billable/non-billable idő jelölése
- [ ] Óradíj beállítása
- [ ] Idő jelentések (projekt, felhasználó, dátum szerint)
- [ ] Idő exportálás

## 11. API és Integrációk

### 11.1 REST API
- [ ] RESTful API végpontok
- [ ] API autentikáció (API kulcs, OAuth)
- [ ] API dokumentáció
- [ ] Rate limiting

### 11.2 Webhook-ok
- [ ] Webhook események (ticket létrehozva, státusz változás, stb.)
- [ ] Webhook konfiguráció

## 12. Adminisztráció

### 12.1 Rendszerbeállítások
- [ ] Általános beállítások
- [ ] Email konfiguráció
- [ ] Fájl tárolás beállítások
- [ ] Biztonsági beállítások

### 12.2 Naplózás
- [ ] Audit log (műveletek naplózása)
- [ ] Felhasználói aktivitás napló
- [ ] Rendszer események napló

## 13. További Funkciók (Opcionális)

### 13.1 Automatizálás
- [ ] Automatikus ticket routing
- [ ] Automatikus válaszok (canned responses)
- [ ] Automatikus státusz változtatás
- [ ] Workflow automatizálás

### 13.2 SLA Kezelés
- [ ] SLA szabályok definiálása
- [ ] SLA követés
- [ ] SLA riasztások

### 13.3 Knowledge Base
- [ ] Cikkek létrehozása
- [ ] Kategóriák
- [ ] Keresés
- [ ] Publikálás/archiválás

### 13.4 További Projekt Funkciók (Opcionális)
- [ ] Projekt sablonok
- [ ] Projekt duplikálás
- [ ] Projekt archíválás
- [ ] Projekt kapcsolatok (függőségek)
- [ ] Projekt fájlkezelés (dokumentumok)
- [ ] Projekt kommentek (projekt szintű)

## 10. AI Chat és Agent Funkciók

**Megjegyzés:** AgentInSec AI Library integrációval (https://github.com/Kesmarki-Dev/agentinsec)

### 10.1 AI Chat Alapfunkciók
- [ ] AI chat felület
- [ ] Chat üzenetek küldése és fogadása
- [ ] Chat előzmények (conversation history)
- [ ] Session kezelés (chat session-ök)
- [ ] Multi-turn conversation támogatás
- [ ] Streaming válaszok (opcionális)

### 10.2 AI Agent Képességek
- [ ] Autonomous execution - Önálló feladat végrehajtás
- [ ] Self-learning - Tanulás interakciókból
- [ ] Tool discovery - Automatikus tool felfedezés
- [ ] Persistent memory - Perzisztens memória
- [ ] Reflection - Önértékelés és javítás
- [ ] Function calling - Dinamikus funkciókiválasztás

### 10.3 AI Agent Integrációk
- [ ] Ticket kezelés funkciók (ticket létrehozás, státusz változtatás, stb.)
- [ ] Projekt kezelés funkciók (projekt létrehozás, feladat hozzáadás, stb.)
- [ ] Felhasználó keresés funkciók
- [ ] Jelentés generálás funkciók
- [ ] Egyedi tool-ok regisztrálása
- [ ] Info blocks kezelés (kontextus információk)

### 10.4 AI Agent Memória és Tanulás
- [ ] Episodic memory - Események tárolása
- [ ] Procedural memory - Műveletek és eljárások
- [ ] Semantic knowledge - Tudásbázis
- [ ] Learning history - Tanulási előzmények
- [ ] Tool usage tracking - Tool használat követése
- [ ] Reflection sessions - Önértékelési munkamenetek

### 10.5 AI Agent Biztonság
- [ ] Jogosultsági ellenőrzés funkciók végrehajtása előtt
- [ ] Veszélyes műveletek megerősítése
- [ ] Execution plan confirmation - Végrehajtási tervek megerősítése
- [ ] Tenant izoláció AI agent memóriában
- [ ] User-specifikus AI tanulás

## Migrációból Átvenni Kívánt Funkciók

A régi Delphi rendszerből átvenni kívánt konkrét funkciók azonosítása szükséges:
- [ ] Funkció lista bővítése a régi rendszer elemzése alapján
- [ ] Adatstruktúrák áttekintése
- [ ] Munkafolyamatok dokumentálása

