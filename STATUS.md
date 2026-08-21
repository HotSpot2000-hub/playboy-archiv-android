# Playboy Archiv – Projektstatus

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
  - IndexedDB / lokale Datenlogik

- `native/MainActivity.java`
  - Capacitor BridgeActivity
  - registriert `ArchiveDirectoryPlugin`

- `native/ArchiveDirectoryPlugin.java`
  - SAF-Verzeichniszugriff für Archiv-Restore

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
- Beispiel:
  - Cyber Girl of the Year
  - SE Model of the Year
  werden beide angezeigt.
- Niedrigere Stufen werden ausgeblendet.

Serien:
- Pro Model werden nur Serien mit der höchsten Anzahl an zugehörigen Shootings angezeigt.
- Gleichstände bleiben erhalten.

## Offene Punkte

### 1. Video-Fullscreen
Hohe Priorität.

Nicht wieder den nativen WebView-Video-Fullscreen verwenden, ohne neue Strategie.

Besserer Ansatz:
Eigener Fullscreen-Modus innerhalb der App:
- Video-Overlay über gesamte App
- schwarzer Hintergrund
- App-Navigation ausblenden
- Android-Zurück sauber behandeln
- keine problematische native WebView-Fullscreen-Umschaltung

### 2. Kleiner Pull-Down-Effekt am oberen Rand
Niedrige Priorität.

Mehrere Ansätze wurden getestet und wieder verworfen:
- `overscroll-behavior-y:none`
- `WebView.setOverScrollMode(View.OVER_SCROLL_NEVER)`
- JavaScript-Abfangen von `touchmove`
- `<main>` als eigener Scrollcontainer

Der letzte Ansatz entfernte den Effekt, machte das Scrollen aber stark ruckelig und ließ Inhalte wieder oberhalb der Titelleiste erscheinen.

Deshalb aktueller stabiler Zustand beibehalten.

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
- Scrollen wurde unflüssig
- Inhalte erschienen wieder oberhalb der Titelleiste

### Native Video-Fullscreen
Aktuell deaktiviert, weil Freeze reproduzierbar war.

## Letzter sinnvoller nächster Schritt

Video-Fullscreen neu konzipieren und als eigenen App-Fullscreen umsetzen.

Vorher:
- aktuellen stabilen Stand nicht verändern
- Änderungen klein halten
- jeweils nur eine Sache testen
- neue APK immer als Update installieren
