# Playboy Archiv – Projektstatus

Stand: 2026-08-29  
Referenz-Commit: `1530b90f5e0ffbfc9d4be0fa01dbd1f4c6fc0bec`

> Diese Datei ist die verbindliche Übergabedatei zwischen Arbeitssitzungen.
> Vor neuer Arbeit zusätzlich `AGENTS.md` lesen und prüfen, ob `main` seit dem
> Referenz-Commit weitergelaufen ist.

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
- Standardmäßig alphabetisch A–Z.
- Alternativ kann nach Bewertung absteigend sortiert werden.
- Bei gleicher Bewertung bleibt die Reihenfolge alphabetisch.
- Die Sortierung wurde auf dem Gerät erfolgreich getestet.

Profilbild-Filter:
- Models können nach Profilbild gefiltert werden.
- Auswahl: `Alle`, `✓` (Profilbild vorhanden), `✕` (Profilbild nicht vorhanden).
- Profilbild-Filter und Sortierung bleiben in der mobilen Ansicht gemeinsam in einer Zeile.
- Der Profilbild-Filter kann mit der Bewertungssortierung kombiniert werden.
- Damit lassen sich insbesondere Models ohne Profilbild nach Bewertung priorisieren.
- Darstellung und Filter wurden auf dem Gerät erfolgreich getestet.

Filterwirkung auf weitere Übersichten:
- Aktive Model-/Profilbildfilter wirken auch auf `Titel`, `Serien` und `Individuals`.
- Shootings eines ausgeblendeten Models werden dort ebenfalls ausgeblendet.
- Ausnahme: Enthält ein Shooting zusätzlich mindestens ein weiterhin sichtbares Model, bleibt das Shooting sichtbar.
- Ausgeblendete Models werden in diesen gefilterten Übersichten nicht zusätzlich als Model-Gruppe geführt.
- Dieses Verhalten wurde auf dem Gerät erfolgreich getestet.

### Bewertung / Filter
- Bewertungsfilter mit Von-/Bis-Bereich ist vorhanden.
- Bewertungsdarstellung verwendet goldene Hervorhebung.
- Im Model-Profil wird die Gesamtwertung als Sternezahl plus Prozentwert angezeigt, z. B. `8/10 ★ · 83%`.
- Darunter werden die fünf Einzelwerte kompakt angezeigt:
  `Größe · Gesicht · Busen · Pussy · Eindruck`.
- Die Einzelwerte bleiben in einer kompakten Zeile (`11px`, `white-space: nowrap`).
- Ist mindestens eine der fünf Kategorien `0`, erscheint der Hinweis `Bewertung unvollständig`.
- Auch `Größe = 0` zählt ausdrücklich als unvollständige Bewertung.
- Die Bewertungsberechnung verwendet weiterhin die bestehende Funktion `modelRatingSummary()`.
- Prozentwert, Einzelwerte und Unvollständig-Hinweis wurden auf dem Gerät erfolgreich getestet.

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

Model-Übersicht und Filterzusammenhang wurden erweitert und auf dem Gerät erfolgreich getestet:

- Profilbild-Filter mit `Alle / ✓ / ✕`
- kompakte einzeilige Darstellung zusammen mit der Sortierung
- Kombination mit `A–Z` bzw. `★ ↓`
- Profilbildfilter wirkt auf Models sowie auf Titel, Serien und Individuals
- Shootings ausgeblendeter Models verschwinden aus den gefilterten Übersichten
- gemeinsame Shootings bleiben sichtbar, wenn mindestens ein sichtbares Model beteiligt ist

Der vorherige Bewertungsblock bleibt ebenfalls stabil:
- Gesamtwertung plus Prozentwert
- kompakte Aufschlüsselung aller fünf Bewertungskategorien
- Kennzeichnung unvollständiger Bewertungen
- Sortierung der Model-Übersicht nach Bewertung

## Letzter sinnvoller nächster Schritt

Der aktuelle Filter-/Sortierblock ist abgeschlossen und erfolgreich getestet.

Vor der nächsten funktionalen Erweiterung:
- `AGENTS.md` und diese Datei lesen,
- prüfen, ob `main` seit `1530b90f5e0ffbfc9d4be0fa01dbd1f4c6fc0bec` weitergelaufen ist,
- den nächsten Funktionsschwerpunkt bewusst festlegen,
- den aktuellen stabilen Stand nicht unnötig verändern.

Video-Fullscreen bleibt ein offener Punkt, ist aber nicht automatisch der nächste Arbeitsschritt.

## Pflegehinweis

Nach jeder abgeschlossenen funktionalen Änderung:
- prüfen, ob diese Datei angepasst werden muss,
- Referenz-Commit aktualisieren,
- offenen bzw. nächsten Schritt korrigieren,
- Sitzung erst danach als abgeschlossen betrachten.
