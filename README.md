# SAB Projekat

Domaći zadatak iz predmeta Softverski alati baza podataka (ETF Beograd, 2025/2026).
Sistem za filmove i korisnike: ocenjivanje, žanrovi, tagovi, liste za gledanje,
preporuke, praćenje trendova popularnosti i sistem nagrada.

## Struktura

- `StudentMain.java` — ulazna tačka, registruje implementacije i pokreće testove
- `student/` — implementacije interfejsa (čist JDBC)
  - `DBConnection.java` — konekcija ka bazi (jedna deljena konekcija za ceo život programa)
  - `rd210107_*Operations.java` — implementacija po interfejsu
- `rd210107.sql` — kompletna kreacija baze (tabele, ograničenja, okidači, procedure, funkcije)
- `rd210107-tsql.sql` — samo okidači/procedure/funkcije
- `rd210107.png` — ER dijagram

## Implementirana pravila

- `TR_BLOCK_EXTREME` — blokada ekstremnih ocena (1/10) po žanru posle 3 takve i manje od 3 neutralne
- `TR_UPDATE_MOVIE_TREND` — status filma (Trending / Rising / Falling / Classic)
- `SP_REWARD_USER_Evaluate` — nagrađivanje korisnika za ocenjivanje slabije ocenjenih filmova iz omiljenog žanra
- Preporuke iz omiljenih žanrova, tematske specijalizacije po tagovima, opis korisnika (curious/focused/undefined)

Pretpostavke tamo gde tekst zadatka nije precizan (npr. definicija "Trending",
prioritet statusa) dokumentovane su kao komentari direktno u `rd210107.sql`.

## Pokretanje

1. Pokreni `rd210107.sql` na MS SQL Server instanci.
2. U `student/DBConnection.java` podesi `DEFAULT_URL`/`DEFAULT_USER`/`DEFAULT_PASSWORD`
   (podrazumevano: `localhost:1433`, baza `MovieDB`, `sa`/`sa123`).
3. Dodaj u classpath:
   - JDBC drajver: `mssql-jdbc-<verzija>.jar`
   - Interfejsi + javni testovi: `SAB_projekat_2026_javni_test.jar` (materijal predmeta)
4. Kompajliraj i pokreni `StudentMain.main()`.

## Testovi

Javni testovi (iz `SAB_projekat_2026_javni_test.jar`) prolaze 15/15.
