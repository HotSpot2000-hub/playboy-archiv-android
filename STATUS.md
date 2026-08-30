# Playboy Archiv – Projektstatus

Stand: 2026-08-30  
Referenz-Commit: `6476f5b799c8f557b6436c990a695b2ed5de3cd6`

> Diese Datei ist die verbindliche Übergabedatei zwischen Arbeitssitzungen.
> Vor neuer Arbeit zusätzlich `AGENTS.md` lesen und prüfen, ob `main` seit dem
> Referenz-Commit weitergelaufen ist.

## Verbindlicher Arbeitsablauf

Für Änderungen gilt der in `AGENTS.md` festgelegte Datei-Workflow:

- Vor jeder Bearbeitung zuerst die aktuelle Version der betroffenen Datei aus `main` lesen.
- Nur diese aktuelle Datei bearbeiten.
- Änderungen möglichst klein und gezielt halten.
- Fertige Dateien als vollständige Ersatzdateien mit exakt dem Repository-Dateinamen bereitstellen, z. B. `index.html` oder `STATUS.md`.
- Keine Patches oder umbenannten Ersatzdateien, sofern nicht ausdrücklich gewünscht.
- Der Benutzer lädt die fertige Datei selbst in GitHub hoch bzw. ersetzt dort die bestehende Datei.
- Vor dem nächsten Arbeitsschritt den neuen Stand von `main` erneut prüfen.

Dieser Ablauf ist der bevorzugte Standard und soll auch nach einem Chat- oder Sitzungswechsel beibehalten werden.

## Aktueller stabiler Stand

Die Android-App läuft stabil und wird per GitHub Actions als signierte APK gebaut.

Wichtige Hinweise:
- APKs immer als UPDATE installieren.
- App nicht deinstallieren, damit lokale Archivdaten erhalten bleiben.
- GitHub Actions Workflow: `.github/workflows/android-debug-apk.yml`
- App-Paket: `de.playboy.archiv`

## Wichtige Dateien

- `www/index.html`
  - Hauptoberfläche
  - Navigation
  - Archiv- und Model-Logik
  - Foto- und Videoanzeige
  - Filter und Bewertungsdarstellung
  - IndexedDB / lokale Datenlogik

- `native/MainActivity.java`
  - Capacitor BridgeActivity
  - registriert `ArchiveDirectoryPlugin`

- `native/ArchiveDirectoryPlugin.java`
  - SAF-Verzeichniszugriff für Archiv-Restore

- `AGENTS.md`
  - verbindliche Arbeits- und Übergaberegeln
  - enthält den verbindlichen Datei-Workflow
  - legt fest, dass `STATUS.md` nach funktionalen Änderungen gepflegt werden muss

## Was bereits funktioniert

### Archiv
- Archiv-Restore über SAF funktioniert.
- Vollständiger Restore wurde erfolgreich getestet.
- Medien, Shootings und Models werden korrekt wiederhergestellt.
- Keine bekannten beschädigten Datensätze nach Restore.

### Signierung
- Permanente Android-Signierung ist eingerichtet.
- Neue Builds lassen sich als Update über bestehende Installation installieren.

### Fotoanzeige
- Pinch-to-Zoom funktioniert.
- Verschieben im gezoomten Bild funktioniert.
- Zoom wird beim Bildwechsel sauber zurückgesetzt.

### Video
- Video-Wiedergabe funktioniert.
- Hochkant und Querformat funktionieren.
- Android-Zurück beendet die Videoansicht sauber.

Aktuell ist nativer Video-Fullscreen deaktiviert:
`controlsList="nofullscreen"`

Grund:
Der native WebView-Video-Fullscreen führte reproduzierbar zu Freeze-Problemen.

### Navigation
- Rückkehr aus „Zuletzt bearbeitet“ funktioniert korrekt.
- Shooting-Detail kehrt wieder zur Archivansicht zurück.

### Titelleiste / Scroll
- Inhalte erscheinen beim Scrollen nicht mehr oberhalb der Titelleiste.
- Scrollen ist aktuell flüssig.
- Kleiner Pull-Down-Effekt am oberen Rand ist noch vorhanden, aber nur kosmetisch.

### Model-Übersicht
Die Karten wurden kompakter gemacht.

Titel:
- Es werden nur Titel der höchsten erreichten Stufe angezeigt.
- Gleichstände bleiben erhalten.
- Niedrigere Stufen werden ausgeblendet.

Serien:
- Pro Model werden nur Serien mit der höchsten Anzahl an zugehörigen Shootings angezeigt.
- Gleichstände bleiben erhalten.

Sortierung:
- Standardmäßig alphabetisch `A–Z`.
- Alternativ nach Gesamtbewertung absteigend mit `% ↓`.
- Sortiert wird nach der gerundeten, angezeigten Prozentzahl.
- Bei gleicher angezeigter Prozentzahl wird alphabetisch nach Modelname sortiert.
- Die korrigierte Sortierung wurde auf dem Gerät erfolgreich getestet.

Profilbild-Filter:
- Models können nach Profilbild gefiltert werden.
- Auswahl: `Alle`, `✓` (Profilbild vorhanden), `✕` (Profilbild nicht vorhanden).
- Profilbild-Filter und Sortierung bleiben in der mobilen Ansicht gemeinsam in einer Zeile.
- Der Profilbild-Filter kann mit der Bewertungssortierung kombiniert werden.
- Darstellung und Filter wurden auf dem Gerät erfolgreich getestet.

Filterwirkung auf weitere Übersichten:
- Aktive Model-/Profilbildfilter wirken auch auf `Titel`, `Serien` und `Individuals`.
- Shootings eines ausgeblendeten Models werden dort ebenfalls ausgeblendet.
- Ausnahme: Enthält ein Shooting zusätzlich mindestens ein weiterhin sichtbares Model, bleibt das Shooting sichtbar.
- Ausgeblendete Models werden in diesen gefilterten Übersichten nicht zusätzlich als Model-Gruppe geführt.
- Dieses Verhalten wurde auf dem Gerät erfolgreich getestet.

### Bewertung / Filter

Das Bewertungssystem wurde auf fünf Sterne pro Kategorie erweitert und erfolgreich auf dem Gerät getestet.

Kategorien:
- Größe
- Gesicht
- Busen
- Pussy
- Eindruck

Sternebewertung innerhalb jeder Kategorie:
- `5 = 100%`
- `4 = 90%`
- `3 = 75%`
- `2 = 55%`
- `1 = 30%`
- `0 = 0%`

Die bestehende Gewichtung der Kategorien untereinander bleibt unverändert:
- Größe: 30%
- Gesicht: 25%
- Busen: 20%
- Pussy: 15%
- Eindruck: 10%

Größe:
- `5★ = 5'1"–5'4"`
- `4★ = 5'0" / 5'5"`
- `3★ = 4'11" / 5'6"`
- `2★ = 4'10" / 5'7"`
- `1★ = 4'9" / 5'8"`
- `0★ = Rest`

Gesamtbewertung:
- Wird grundsätzlich als Prozentwert dargestellt.
- Bei exakt `100%` wird statt `100%` ein Favoriten-Herz `❤️` angezeigt.
- Das gilt sowohl im Model-Profil als auch in der Model-Übersicht.
- Die Sortierung verwendet `% ↓`.
- Sortierung und Bewertungsfilter verwenden die gerundete, angezeigte Prozentzahl.
- Der Bewertungsfilter arbeitet korrekt mit einem Prozentbereich von `0–100`.
- Min-/Max-Eingaben wie `80` bleiben `80` und werden nicht mehr auf `10` begrenzt.
- `0 Sterne` ist ein gültiger Bewertungswert.
- `Bewertung unvollständig` erscheint nur, wenn eine Kategorie tatsächlich noch nicht bewertet wurde.
- Automatische Favoritenmarkierung erfolgt bei `100%`.
- Die Bewertungsberechnung verwendet weiterhin `modelRatingSummary()`.
- Korrigierte Sortierung und korrigierter Bewertungsfilter wurden auf dem Gerät erfolgreich getestet.

Darstellung:
- Die fünf Einzelwerte werden weiterhin kompakt angezeigt:
  `Größe · Gesicht · Busen · Pussy · Eindruck`.
- Die Einzelwerte stehen links in einer kompakten Zeile (`11px`, `white-space: nowrap`).
- Gesamtbewertung bzw. Favoriten-Herz steht rechts daneben.
- Die Einzelwerte sind vertikal mittig zur Gesamtbewertung bzw. zum Favoriten-Herz ausgerichtet.
- Das Favoriten-Herz ist im Profil größer und in der Model-Übersicht kleiner/dezenter.
- Ein separates Favoriten-Herz im Profil gibt es nicht mehr.
- Normale Prozentbewertungen bleiben golden hervorgehoben.
- Diese Darstellung wurde auf dem Gerät erfolgreich getestet.

## Offene Punkte

### Video-Fullscreen
Weiterhin offen, aktuell aber nicht der laufende Arbeitsschwerpunkt.

Nicht wieder den nativen WebView-Video-Fullscreen verwenden, ohne neue Strategie.

Falls das Thema wieder aufgenommen wird, ist der bevorzugte Ansatz:
Eigener Fullscreen-Modus innerhalb der App:
- Video-Overlay über gesamte App
- schwarzer Hintergrund
- App-Navigation ausblenden
- Android-Zurück sauber behandeln
- keine problematische native WebView-Fullscreen-Umschaltung

### Kleiner Pull-Down-Effekt am oberen Rand
Niedrige Priorität.

Mehrere Ansätze wurden getestet und wieder verworfen:
- `overscroll-behavior-y:none`
- `WebView.setOverScrollMode(View.OVER_SCROLL_NEVER)`
- JavaScript-Abfangen von `touchmove`
- `<main>` als eigener Scrollcontainer

Der letzte Ansatz entfernte den Effekt, machte das Scrollen aber stark ruckelig und ließ Inhalte wieder oberhalb der Titelleiste erscheinen.

Deshalb aktuellen stabilen Zustand beibehalten.

## Dinge, die NICHT wiederholt werden sollten

### Immersive Fullscreen
Frühere native Immersive-Versuche haben die App stark beschädigt bzw. unbenutzbar gemacht.

Nicht global verwenden:
- `WindowCompat.setDecorFitsSystemWindows(false)`

Keine optionalen Plugin-Aufrufe wie:
`plugin?.method?.().catch(...)`
wenn die Methode möglicherweise nicht existiert.

### Main-Scrollcontainer
Nicht wieder:
`main{height:100vh;overflow-y:auto;...}`

Folgen:
- Scrollen wurde unflüssig.
- Inhalte erschienen wieder oberhalb der Titelleiste.

### Native Video-Fullscreen
Aktuell deaktiviert, weil Freeze reproduzierbar war.

## Zuletzt abgeschlossener Arbeitsblock

Bewertungssystem und Bewertungsdarstellung wurden verfeinert und auf dem Gerät erfolgreich getestet:

- fünf Sterne pro Kategorie
- prozentuale Abstufung je Stern
- Größenstaffel mit fünf Sternen
- bestehende Kategoriegewichtung beibehalten
- Gesamtbewertung als Prozentwert
- bei 100% Herz statt Prozentzahl in Profil und Übersicht
- separates Favoriten-Herz im Profil entfernt
- Gesamtbewertung rechts, Einzelwerte links und vertikal zentriert
- Profil-Herz größer, Übersichts-Herz kleiner
- Sortierung `A–Z / % ↓`
- bei gleicher angezeigter Prozentzahl alphabetische Zweitsortierung
- Bewertungsfilter korrekt auf `0–100%`
- Sortierung und Filter vergleichen die gerundete, angezeigte Prozentzahl
- `0 Sterne` als gültige Bewertung
- `Bewertung unvollständig` nur bei tatsächlich fehlender Bewertung
- automatische Favoritenmarkierung bei 100%

Alle zuletzt korrigierten Punkte wurden auf dem Gerät erfolgreich bestätigt.

Der vorherige Filterblock bleibt stabil:
- Profilbild-Filter mit `Alle / ✓ / ✕`
- Filter wirkt auf Models sowie auf Titel, Serien und Individuals
- gemeinsame Shootings bleiben sichtbar, wenn mindestens ein sichtbares Model beteiligt ist

## Letzter sinnvoller nächster Schritt

Der aktuelle Bewertungsblock ist abgeschlossen und erfolgreich getestet.

Vor der nächsten funktionalen Erweiterung:
- `AGENTS.md` und diese Datei lesen,
- prüfen, ob `main` seit `6476f5b799c8f557b6436c990a695b2ed5de3cd6` weitergelaufen ist,
- die aktuelle Version der zu ändernden Datei aus `main` laden,
- den nächsten Funktionsschwerpunkt bewusst festlegen,
- den aktuellen stabilen Stand nicht unnötig verändern.

Video-Fullscreen bleibt ein offener Punkt, ist aber nicht automatisch der nächste Arbeitsschritt.

## Pflegehinweis

Nach jeder abgeschlossenen funktionalen Änderung:
- prüfen, ob diese Datei angepasst werden muss,
- Referenz-Commit aktualisieren,
- offenen bzw. nächsten Schritt korrigieren,
- Sitzung erst danach als abgeschlossen betrachten.
