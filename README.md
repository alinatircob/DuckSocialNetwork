# 🦆 Duck Social Network

> O rețea de socializare interactivă, construită în Java și JavaFX, dedicată interacțiunii dintre Persoane și... Rațe! Proiectul demonstrează aplicarea conceptelor avansate de Programare Orientată pe Obiecte (OOP), Design Patterns, Algoritmică a Grafurilor și baze de date relaționale.

---

## 🌟 Funcționalități Principale

* **Sistem Dual de Utilizatori:** Suportă două tipuri de entități - `Persoane` și `Rațe` (cu abilități specifice precum `FLYING`, `SWIMMING` sau ambele).
* **Gestionarea Prieteniilor:** Sistem complet de trimitere, acceptare sau respingere a cererilor de prietenie.
* **Mesagerie Privată (Chat):** Sistem de chat în timp real (actualizat via *Observer Pattern*), care suportă funcționalitatea de *Reply* și ține evidența mesajelor citite/necitite.
* **Evenimente și Competiții:** Utilizatorii pot crea evenimente (ex. curse pentru rațe), se pot înscrie ca spectatori sau participanți, iar sistemul generează automat timpii și clasamentele curselor.
* **Algoritmică Avansată:** Implementare algoritmi pe grafuri (DFS/BFS) pentru identificarea numărului de comunități (componente conexe) și determinarea celei mai sociabile comunități.
* **Paginare Custom:** Sistem de paginare dezvoltat de la zero, optimizat pentru baze de date mari, cu interfață grafică responsivă.
* **Interfață Grafică (GUI):** UI modern construit cu JavaFX, FXML și stilizat prin CSS.

---

## 🛠️ Tehnologii și Arhitectură

Acest proiect a fost dezvoltat cu un accent puternic pe *Clean Code* și arhitectură stratificată.

* **Backend:** Java 17+
* **Frontend (GUI):** JavaFX (FXML, CSS)
* **Bază de Date:** PostgreSQL
* **Comunicare DB:** JDBC (cu PreparedStatement pentru prevenirea SQL Injections)
* **Securitate:** Criptare parole (AES/BCrypt)
* **Design Patterns folosite:**
    * **Factory Pattern:** Pentru instanțierea dinamică a diferitelor tipuri de rațe.
    * **Decorator Pattern:** Pentru adăugarea comportamentelor specifice (`FlyingDuck`, `SwimmingDuck`) la runtime.
    * **Observer Pattern:** Pentru a asigura arhitectura reactivă a interfeței grafice (UI-ul se actualizează automat la primirea unui mesaj nou).
    * **Repository / DAO Pattern:** Pentru decuplarea logicii de business de accesul la date.

---

## ⚙️ Cum să rulezi proiectul local

Dacă dorești să testezi aplicația pe propriul computer, urmează pașii de mai jos.

### 1. Cerințe preliminare
* **Java Development Kit (JDK):** Versiunea 17 sau mai nouă.
* **PostgreSQL:** Instalat și rulând pe portul implicit `5432`.
* **IDE:** IntelliJ IDEA (recomandat) sau Eclipse, configurat pentru proiecte JavaFX/Maven.

### 2. Configurarea Bazei de Date
Aplicația necesită o bază de date PostgreSQL pentru a rula. Am pregătit un script de inițializare care creează tabelele, tipurile ENUM și inserează câteva date de test.

1. Deschide pgAdmin (sau terminalul psql).
2. Creează o bază de date goală numită `useri`.
3. Rulează scriptul `init_db.sql` aflat în rădăcina acestui repository.

### 3. Variabile de Mediu (Opțional, dar recomandat)
Pentru motive de securitate, aplicația citește credențialele bazei de date din variabilele de mediu. Dacă acestea nu sunt setate, va face "fallback" la valorile implicite (`postgres` / `postgres`).

Dacă baza ta de date locală are o altă parolă, setează următoarele variabile de mediu în sistemul tău sau în configurația de rulare (Run Configuration) din IDE:
* `DB_URL` (ex: `jdbc:postgresql://localhost:5432/useri`)
* `DB_USER` (ex: `postgres`)
* `DB_PASSWORD` (parola ta de PostgreSQL)

### 4. Lansarea Aplicației
Pentru a porni aplicația cu interfața grafică, rulează clasa principală:
`src/main/java/org/example/ducksocialnetworkm/Main.java`

*Notă pentru testare:* Poți folosi următoarele conturi de test incluse în scriptul SQL:
* **Username:** `MARIAC` | **Parola:** `MYPASS`
* **Username:** `DANV` | **Parola:** `DANPASS`
  *(Notă: Asigură-te că parolele din baza de date locală corespund algoritmului de criptare din codul sursă).*

---

## 📂 Structura Proiectului

Proiectul este organizat pe pachete respectând principiul de Separare a Responsabilităților (Separation of Concerns):

* `controller/` - Conține clasele JavaFX care gestionează evenimentele din interfață.
* `depozit/` - Implementarea pattern-ului Repository pentru accesul la date (DB). Conține și subpachetul `paging` pentru logica de paginare.
* `domeniu/` - Entitățile de bază ale aplicației (`User`, `Rata`, `Mesaj`, `Event`), organizate logic.
* `factory/` - Logica de creare a obiectelor complexe.
* `interfata/` - Componente UI (în cazul tranzițiilor dinspre consolă spre GUI).
* `serviciu/` - Stratul de Business Logic, unde se regăsesc algoritmii de validare, gestionarea prieteniilor și sistemul de comunități (Grafuri).
* `utils/` - Utilitare globale (criptarea parolelor, tipurile de evenimente pentru Observer).
* `resources/` - Fișierele `.fxml` și `style.css` pentru aspectul aplicației.

---

## 🚀 Planuri de Viitor (Roadmap)
* [ ] Extragerea textelor hardcodate într-un sistem de internaționalizare (i18n).
* [ ] Trecerea la un Connection Pool (ex. HikariCP) pentru optimizarea conexiunilor la baza de date.
* [ ] Implementarea unui "Dark Mode" prin manipularea fișierului CSS.
