# Playboy Archiv – Projektstatus

Stand: 2026-09-03  
Referenz-Commit: `0ca344275969d63ba6c3f3bc934fa71af3e4f1a8`

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
- Beim Öffnen eines Model-Profils aus den Übersichten `Models`, `Titel`, `Serien` und `Individuals` wird die Seite gezielt auf `top: 0` gesetzt.
- Dadurch startet das Profil wieder ganz oben beim Profilbild statt auf der vorherigen Scrollhöhe der Übersicht.
- Dieses Verhalten wurde auf dem Gerät erfolgreich getestet.
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

### Kurzbio im Model-Profil
- Direkt unter den Kerndaten gibt es eine eigene klappbare `Kurzbio`-Karte.
- Die Kurzbio wird pro Model gespeichert und in der normalen Profil-Bearbeitung als Freitext gepflegt.
- Absätze und Zwischenüberschriften bleiben im Profil erhalten.
- Modelle ohne hinterlegte Kurzbio zeigen die Karte nicht an.
- Die Karte startet eingeklappt.
- Im eingeklappten Zustand bleibt der Anfang der Bio sichtbar.
- Der untere Textbereich läuft in einen deutlich sichtbaren weißen Verlauf aus.
- Unten rechts erscheint `Mehr anzeigen`; Antippen klappt die vollständige Bio auf.
- Der Pfeil in der Kartenüberschrift kann ebenfalls zum Auf- und Zuklappen verwendet werden.
- Darstellung, Bearbeitung, Speichern sowie Auf-/Zuklappen wurden auf dem Gerät erfolgreich getestet.

### Bewertung / Filter
Das Bewertungssystem verwendet fünf bewertete Stufen pro Kategorie:
- `5 = 100%`
- `4 = 90%`
- `3 = 70%`
- `2 = 40%`
- `1 = 0%`

`Nicht bewertet` bleibt davon getrennt. Eine bereits gewählte Sternstufe kann durch erneutes Antippen wieder auf `nicht bewertet` gesetzt werden.

Gewichtung:
- Größe: 20%
- Gesicht: 20%
- Busen: 20%
- Pussy: 20%
- Eindruck: 20%

Größe:
- `5★ = 5'1"–5'4"`
- `4★ = 5'0" / 5'5"`
- `3★ = 4'11" / 5'6"`
- `2★ = 4'10" / 5'7"`
- `1★ = Rest`

Entscheidungshilfen:
- Gesicht: `5★ zum verlieben`, `4★ sehr attraktiv`, `3★ attraktiv`, `2★ neutral`, `1★ Rest`
- Busen: `5★ geile Dinger`, `4★ 1 kleiner Abstrich`, `3★ 2 kleine Abstriche`, `2★ neutral`, `1★ Rest`
- Pussy: `5★ blank`, `4★ kurz, schmal`, `3★ kurz, breit`, `2★ neutral`, `1★ Rest`
- Eindruck: `5★ top`, `4★ irgendetwas fehlt`, `3★ hat was`, `2★ neutral`, `1★ Rest`

Darstellung und Verhalten:
- Die Entscheidungshilfen werden direkt beim Bewerten im Model-Profil angezeigt.
- Gesamtbewertung ist der gleichgewichtete Mittelwert der fünf Kategorien.
- Bei exakt 100% wird `❤️` angezeigt.
- Sortierung und Bewertungsfilter verwenden die gerundete angezeigte Prozentzahl.
- Bewertungsfilter arbeitet mit `0–100`.
- `Bewertung unvollständig` nur bei tatsächlich fehlender Kategorie.
- Die neue Bewertungslogik wurde im normalen Gebrauch auf dem Gerät erfolgreich bestätigt.
- Einzelwerte links kompakt, Gesamtbewertung/Herz rechts und vertikal zentriert.
- Herz im Profil größer als in der Übersicht.
- Separates Favoriten-Herz im Profil entfernt.

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

Die Kurzbio im Model-Profil wurde ergänzt und auf dem Gerät erfolgreich bestätigt:

- eigene Kurzbio-Karte direkt unter den Kerndaten
- Kurzbio pro Model frei bearbeitbar und speicherbar
- Modelle ohne Bio zeigen keine leere Karte
- eingeklappt bleibt der Anfang lesbar
- sanftes Ausblenden am unteren Rand
- sichtbare Schaltfläche `Mehr anzeigen`
- vollständiger Text nach dem Aufklappen
- Auf-/Zuklappen auch über den Pfeil in der Kartenüberschrift
- Absätze und Zwischenüberschriften werden beibehalten
- Gerätetest erfolgreich bestätigt

Der zuvor stabile Bewertungs-, Archivfilter-, Scroll- und Profilnavigationsstand bleibt unverändert.

Der aktuelle `www/index.html`-Stand auf `main` entspricht dem erfolgreich getesteten Kurzbio-Stand.

## Letzter sinnvoller nächster Schritt

Der Kurzbio-Arbeitsblock ist abgeschlossen und auf dem Gerät bestätigt.

Vor der nächsten funktionalen Änderung:
- aktuellen `main`-Stand erneut prüfen,
- aktuelle betroffene Datei aus `main` lesen,
- neuen Funktionsschwerpunkt bewusst festlegen,
- den stabilen Kurzbio-, Bewertungs-, Filter-, Scroll- und Profilnavigationsstand nicht unnötig verändern.

Video-Fullscreen und der kleine kosmetische Pull-Down-Effekt bleiben offen, sind aber nicht automatisch der nächste Arbeitsschritt.

## Pflegehinweis

Nach jeder abgeschlossenen funktionalen Änderung:
- prüfen, ob diese Datei angepasst werden muss,
- Referenz-Commit aktualisieren,
- offenen bzw. nächsten Schritt korrigieren,
- Sitzung erst danach als abgeschlossen betrachten.
