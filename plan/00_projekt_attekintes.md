# InTicky - Projekt Áttekintés

## Projekt Célja

Az InTicky egy modern, webes ticketing rendszer, amely egy régi Delphi 2009-es asztali alkalmazás modernizált változata. A rendszer célja, hogy egyrészt kiszolgálja a jelenlegi működést (átállás), másrészt multi-tenant képességgel bárki számára elérhető legyen.

## Régi Rendszer Jellemzői

- **Technológia**: Delphi 2009
- **Típus**: Asztali alkalmazás
- **Adatbázis**: Sybase SQL Anywhere
- **Célcsoport**: Szoftverfejlesztő cég ügyfelei
- **Funkciók**: 
  - Hibajegyek felvétele
  - Support feladatok kezelése
  - Fizetős fejlesztések nyomon követése

## Új Rendszer Jellemzői

- **Technológia**: Modern webes stack (backend + frontend)
- **Típus**: Webes alkalmazás
- **Adatbázis**: Új adatbázis motor és struktúra
- **Célcsoport**: 
  - Elsődleges: Jelenlegi fejlesztő cég (átállás)
  - Másodlagos: Bármilyen cég/szervezet, aki ticketing rendszerre van szüksége (multi-tenant)

## Fő Kihívások

1. **Funkciók átvétele**: A régi rendszer hasznos funkcióinak modernizált átvétele
2. **Adatbázis migráció**: Sybase SQL Anywhere-ről új adatbázisra
3. **Multi-tenant architektúra**: Több ügyfél/kliens független kezelése
4. **Átállás biztosítása**: Zavartalan átmenet a régi rendszerről az újra

## Projekt Fázisok

1. **Tervezés** (jelenlegi fázis)
   - Funkció lista összeállítása
   - Technikai architektúra tervezése
   - Adatbázis struktúra tervezése
   - Multi-tenant modell tervezése

2. **Fejlesztés** (következő fázisok)
   - Backend fejlesztés
   - Frontend fejlesztés
   - Adatbázis implementáció
   - Integrációk

3. **Migráció és Tesztelés**
   - Adatok migrálása
   - Tesztelés
   - Átállás

## Dokumentumok

- `00_projekt_attekintes.md` - Ez a dokumentum
- `01_funkcio_lista.md` - Részletes funkció lista
- `02_technikai_kovetelmenyek.md` - Technikai specifikációk
- `03_adatbazis_tervezes.md` - Adatbázis struktúra tervezés
- `04_multi_tenant_architektura.md` - Multi-tenant modell tervezése
- `05_migracios_terv.md` - Migrációs stratégia

