# Playboy Archiv – Projektstatus

Stand: 2026-08-30  
Referenz-Commit: `e2bae4fc31c3b122312d285533d2d1aa79bc793d`

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
  - Hauptoberfläche, Navigation, Archiv- und Model-Logik
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
- Nativer Video-Fullscreen bleibt deaktiviert (`controlsList="nofullscreen"`), weil er reproduzierbar Freezes verursachte.

### Navigation / normales Scrollen
- Rückkehr aus „Zuletzt bearbeitet“ funktioniert korrekt.
- Shooting-Detail kehrt wieder zur Archivansicht zurück.
- Inhalte erscheinen beim normalen Scrollen nicht mehr oberhalb der Titelleiste.
- Kleiner Pull-Down-Effekt am oberen Rand bleibt kosmetisch.

### Ein-Finger-Scrollen in Bearbeitungskarten
- Das zuvor stark hakelige Ein-Finger-Scrollen in Bearbeitungskarten wurde behoben.
- Ursache war ein globaler JavaScript-`touchmove`-Handler, der bei genau einem Finger und `window.scrollY <= 0` mit `preventDefault()` eingriff.
- Dieser globale Touch-Block wurde entfernt.
- Gerätetest erfolgreich bestätigt in:
  - Archivfilter
  - Model-Profil
  - Verwaltung
- Zwei-Finger-Scrollen war bereits vorher unauffällig.
- Den globalen Ein-Finger-`touchmove`-Block nicht wieder einführen.

### Model-Übersicht
- Karten sind kompakt.
- Titel: nur höchste erreichte Stufe, Gleichstände bleiben.
- Serien: nur Serien mit höchster Shooting-Anzahl, Gleichstände bleiben.
- Sortierung `A–Z` oder `% ↓`.
- `% ↓` verwendet die gerundete angezeigte Prozentzahl; bei Gleichstand alphabetisch.
- Korrigierte Sortierung auf Gerät getestet.

Profilbild-Filter:
- `Alle / ✓ / ✕`
- mit Bewertungssortierung kombinierbar
- auf Gerät getestet.

Filterwirkung:
- Aktive Model-/Profilbildfilter wirken auf Models, Titel, Serien und Individuals.
- Gemeinsame Shootings bleiben sichtbar, wenn mindestens ein sichtbares Model beteiligt ist.
- Auf Gerät getestet.

### Archivfilter / freie Suche
Der Archivfilter ist jetzt als einfacher Bottom-Sheet-Dialog stabil nutzbar.

Verhalten:
- Ein-Finger-Scrollen funktioniert nach Entfernung des globalen `touchmove`-Blocks.
- Freie Suche funktioniert.
- `Filter anwenden` filtert, ohne die Filterkarte zu schließen.
- `Filter zurücksetzen` setzt zurück, ohne die Karte zu schließen.
- `×` schließt die Filterkarte.
- Die untere App-Navigation wird bei geöffnetem Archivfilter ausgeblendet.

Performance:
- Die freie Suche wird pro Filterlauf nur einmal berechnet.
- Die ermittelte Model-ID-Menge wird anschließend für die Modelprüfung wiederverwendet.
- Dadurch friert die App beim Anwenden einer freien Suche nicht mehr ein.
- Freie Suche wurde auf dem Gerät erfolgreich bestätigt.

### Bewertung / Filter
Das Bewertungssystem verwendet fünf Sterne pro Kategorie:
- `5 = 100%`
- `4 = 90%`
- `3 = 75%`
- `2 = 55%`
- `1 = 30%`
- `0 = 0%`

Gewichtung:
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
- Prozentwert; bei exakt 100% wird `❤️` angezeigt.
- Sortierung und Bewertungsfilter verwenden die gerundete angezeigte Prozentzahl.
- Bewertungsfilter arbeitet mit `0–100`.
- `0 Sterne` ist ein gültiger gesetzter Wert.
- `Bewertung unvollständig` nur bei tatsächlich fehlender Kategorie.
- Sortierung und Bewertungsfilter auf Gerät erfolgreich getestet.

Darstellung:
- Einzelwerte links kompakt, Gesamtbewertung/Herz rechts und vertikal zentriert.
- Herz im Profil größer als in der Übersicht.
- Separates Favoriten-Herz im Profil entfernt.
- Darstellung auf Gerät getestet.

## Offene Punkte

### Video-Fullscreen
Weiterhin offen, aktuell kein Arbeitsschwerpunkt.

Bevorzugter zukünftiger Ansatz:
- eigener Fullscreen-Modus innerhalb der App
- schwarzer Hintergrund
- App-Navigation ausblenden
- Android-Zurück sauber behandeln
- keine native WebView-Fullscreen-Umschaltung

### Kleiner Pull-Down-Effekt am oberen Rand
Niedrige Priorität.

Verworfene Ansätze:
- `overscroll-behavior-y:none` als gezielter Lösungsversuch
- `WebView.setOverScrollMode(View.OVER_SCROLL_NEVER)`
- JavaScript-Abfangen von `touchmove`
- `<main>` als eigener Scrollcontainer

Wichtig:
Der zuletzt noch vorhandene globale JavaScript-`touchmove`-Block war zusätzlich die Ursache des hakeligen Ein-Finger-Scrollens in Bearbeitungskarten und wurde entfernt. Nicht wieder einführen.

## Dinge, die NICHT wiederholt werden sollten

### Immersive Fullscreen
Nicht global verwenden:
- `WindowCompat.setDecorFitsSystemWindows(false)`

Keine optionalen Plugin-Aufrufe wie
`plugin?.method?.().catch(...)`,
wenn die Methode möglicherweise nicht existiert.

### Main-Scrollcontainer
Nicht wieder:
`main{height:100vh;overflow-y:auto;...}`

Folgen:
- Scrollen wurde unflüssig.
- Inhalte erschienen wieder oberhalb der Titelleiste.

### Globales Touchmove-Abfangen
Keinen globalen Ein-Finger-`touchmove`-Handler mit `preventDefault()` wieder einführen.

Folge:
- Bearbeitungskarten scrollten mit einem Finger stark hakelig.
- Filter, Profil und Verwaltung waren betroffen.

### Native Video-Fullscreen
Aktuell deaktiviert, weil Freeze reproduzierbar war.

## Zuletzt abgeschlossener Arbeitsblock

Archivfilter und Bearbeitungskarten wurden stabilisiert:

- freie Suche im Archivfilter funktioniert
- Suchkorpus wird pro Filterlauf nur einmal berechnet
- Freeze beim Anwenden der freien Suche behoben
- `Filter anwenden` lässt die Filterkarte geöffnet
- `Filter zurücksetzen` lässt die Filterkarte geöffnet
- `×` schließt die Filterkarte
- globales Ein-Finger-`touchmove`-Abfangen entfernt
- Ein-Finger-Scrollen in Filter, Profil und Verwaltung auf dem Gerät erfolgreich getestet

Der aktuelle `www/index.html`-Stand auf `main` entspricht dem erfolgreich getesteten Stand.

## Letzter sinnvoller nächster Schritt

Der Archivfilter-/Scroll-Arbeitsblock ist abgeschlossen und auf dem Gerät bestätigt.

Vor der nächsten funktionalen Änderung:
- aktuellen `main`-Stand erneut prüfen,
- aktuelle betroffene Datei aus `main` lesen,
- neuen Funktionsschwerpunkt bewusst festlegen,
- stabilen Filter- und Scrollstand nicht unnötig verändern.

Video-Fullscreen und der kleine kosmetische Pull-Down-Effekt bleiben offen, sind aber nicht automatisch der nächste Arbeitsschritt.

## Pflegehinweis

Nach jeder abgeschlossenen funktionalen Änderung:
- prüfen, ob diese Datei angepasst werden muss,
- Referenz-Commit aktualisieren,
- offenen bzw. nächsten Schritt korrigieren,
- Sitzung erst danach als abgeschlossen betrachten.
