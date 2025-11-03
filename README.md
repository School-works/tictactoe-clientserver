
# 🧩 Protocollo di Comunicazione — Tic Tac Toe (Tris) TCP

## 🎯 Scopo
Questo protocollo definisce le regole di comunicazione tra **server** e **client** per realizzare il gioco del **Tris (Tic Tac Toe)** tramite connessione TCP in Java.  
Il server amministra la partita tra due giocatori remoti (G1 e G2), controlla le mosse, gestisce i turni e comunica gli esiti.

---

## ⚙️ Struttura generale

- La comunicazione è **sincrona**: ogni client attende una risposta prima di inviare nuovi dati.
- La griglia di gioco è composta da **9 caselle**, indicizzate da 0 a 8:

| Indice | Posizione |
|:--:|:--|
| 0 | in alto a sinistra |
| 1 | in alto al centro |
| 2 | in alto a destra |
| 3 | centro sinistra |
| 4 | centro |
| 5 | centro destra |
| 6 | basso sinistra |
| 7 | basso centro |
| 8 | basso destra |

---

## 🔄 Fasi del protocollo

### 1️⃣ Connessione e avvio partita
1. Il primo client che si connette diventa **Giocatore 1 (G1)**  
   → riceve il messaggio:
   ```
   WAIT
   ```
   e resta in attesa.

2. Quando si connette il secondo client (**Giocatore 2 – G2**), il server invia a **entrambi**:
   ```
   READY
   ```
   indicando che la partita può iniziare.

3. **G1** inizia sempre per primo.

---

### 2️⃣ Mossa del giocatore
Durante il proprio turno, il giocatore invia al server un numero intero da `0` a `8`, che rappresenta la casella scelta nella griglia.

Esempio:
```
4
```
→ Il giocatore vuole occupare la casella centrale.

---

### 3️⃣ Risposta del server al giocatore che ha mosso

| Codice | Significato | Azione successiva |
|:--:|:--|:--|
| `OK` | Mossa valida, turno dell’avversario | Attendere l’aggiornamento |
| `KO` | Mossa non valida (casella già occupata o indice non valido) | Riprovare una nuova mossa |
| `W` | Il giocatore ha vinto | La partita termina |
| `P` | La partita è finita in pareggio | La partita termina |

---

### 4️⃣ Messaggio di aggiornamento all’avversario
Ogni volta che un giocatore effettua una mossa valida (`OK`, `W` o `P`), il server invia **all’altro giocatore** una stringa di **10 campi separati da virgola**, nel formato:

```
<stato_0>,<stato_1>,<stato_2>,<stato_3>,<stato_4>,<stato_5>,<stato_6>,<stato_7>,<stato_8>,<esito>
```

#### Significato dei campi
- `<stato_0>` … `<stato_8>` → valori:
  - `0` → casella vuota  
  - `1` → casella occupata da G1  
  - `2` → casella occupata da G2  
- `<esito>` → può essere:
  - `""` (vuoto) → la partita continua, ora tocca a te  
  - `L` → hai perso  
  - `P` → partita terminata in pareggio  

#### Esempio
```
1,0,2,0,1,0,0,0,0,
```
→ G1 ha occupato le celle 0 e 4, G2 la cella 2. La partita continua.

> ⚠️ **Nota:**  
> Il messaggio di aggiornamento viene inviato **solo dopo una mossa valida** (`OK`, `W` o `P`).  
> Dopo un `KO` non viene inviato nulla all’altro giocatore.

---

### 5️⃣ Fine partita
Quando si verifica una condizione di vittoria o pareggio:
- Il server invia:
  - al vincitore → `W`
  - allo sconfitto → la matrice con esito `L`
- In caso di pareggio:
  - al giocatore che ha appena effettuato la mossa → `P`
  - all'altro giocatore → la matrice con esito `P`
- Dopo aver inviato i messaggi finali, il server **chiude entrambe le connessioni**.

---

### 6️⃣ Disconnessioni o errori
Se un giocatore si disconnette prima della fine della partita:
```
DISCONNECTED
```
viene inviato al giocatore rimasto connesso.  
Il server chiude poi la connessione.



---

## 🧠 Regole di implementazione consigliate
- La griglia può essere rappresentata come:
  ```java
  ArrayList<Integer> board = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0,0));
  ```
- Per la comunicazione testuale:
  ```java
  BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
  ```
- Per costruire l'array a partire dalla stringa ricevuta:
  ```java
  String received = "1,0,2,0,1,0,0,0,0,"; // esempio dal server
  String[] cells = received.split(",");   // splitta la stringa in array di stringhe
  ```

---

## 💬 Esempio di sequenza semplificata
```
G1 → SERVER : 4
SERVER → G1 : OK
SERVER → G2 : 0,0,0,0,1,0,0,0,0,
G2 → SERVER : 0
SERVER → G2 : OK
SERVER → G1 : 2,0,0,0,1,0,0,0,0,
G1 → SERVER : 8
SERVER → G1 : W
SERVER → G2 : 2,0,0,0,1,0,0,0,1,L
SERVER chiude entrambe le connessioni
```
