# Playboy Archiv -- Projektstatus

Stand: 2026-09-06  
Referenz-Commit: `a6444aaf1702f6ab2be69359afbf342cd1726442`

> Verbindliche Übergabedatei. Vor neuer Arbeit `AGENTS.md` lesen und prüfen, ob `main` seit dem Referenz-Commit weitergelaufen ist.

## Verbindlicher Arbeitsablauf

- Vor jeder Bearbeitung die aktuelle Datei aus `main` lesen.
- Nur auf Basis dieser Version arbeiten.
- Änderungen klein und gezielt halten.
- Fertige Dateien als vollständige Ersatzdateien mit exakt dem Repository-Dateinamen bereitstellen.
- Keine Patches oder umbenannten Ersatzdateien, sofern nicht ausdrücklich gewünscht.
- Der Benutzer ersetzt die Datei selbst in GitHub.
- Vor dem nächsten Schritt `main` erneut prüfen.
- APKs immer als UPDATE installieren; App nicht deinstallieren.

## Aktueller stabiler Stand

- App-Paket: `de.playboy.archiv`
- Daten-Schema-Version: `6`
- `www/index.html` auf `main`: Blob `14a0702dbaa62553efd077feb9f6226136853e28`
- Fachlich berücksichtigter Code-Stand: Commit `a6444aaf1702f6ab2be69359afbf342cd1726442`
- Schema-6-Grundlage wurde als Update auf dem Gerät erfolgreich getestet.
- Bestehende Models, Shootings und Medien blieben nutzbar.
- Es findet keine automatische Migration vorhandener Medien in Fotogalerie-/Video-Objekte statt.

## Stabile Funktionen

- Archiv-Restore über SAF.
- Permanente Android-Signierung und Update-Installation ohne Deinstallation.
- Fotoanzeige mit Pinch-Zoom.
- Video-Wiedergabe; nativer WebView-Fullscreen bleibt deaktiviert.
- Models, Titel, Serien, Individuals, Shootings und Medienverwaltung funktionieren.
- Archivfilter, freie Suche, Bewertungs- und Profilbildfilter funktionieren.
- Research mit Status, strukturierten Belegen und kontrollierter Übernahme.
- `careerFacts`, `bioFacts`, `archiveFacts`.
- Korrektur-/Rückzugslogik für bestätigte Fakten.
- Bestätigte Fakten im Model-Profil.
- Bio-Entwurfsgenerator.
- Research-Import.
- Research-Löschschutz ist implementiert; separater Gerätetest dafür weiterhin nicht dokumentiert.

## Erschließungsgrad

Im Model-Profil existiert die Karte `Erschließungsgrad`.

- Keine subjektive Gesamtpunktzahl.
- Titel, Serien, Pictorials und Videos werden getrennt betrachtet.
- `unbekannt` wird nicht als `0` behandelt.
- Reale `linkedArchiveIds` reduzieren offene Restmengen und vermeiden Doppelzählungen.
- Anzeige und Härtung wurden auf dem Gerät erfolgreich geprüft.

Bekanntes fachliches Problem der alten Struktur:
- Ein Shooting mit Fotos darf langfristig nicht pauschal mit einem Pictorial/Fotogalerie gleichgesetzt werden.

## Neue fachliche Zielstruktur

### Shooting

Rolle: Entstehungskontext.

Eigene Daten:
- Datum
- Ort
- Notizen

Direkte Beziehungen:
- Model(s)
- Fotograf(en)

### Fotogalerie

Neuer Werktyp `gallery`.

Eigene Daten künftig:
- Bezeichnung/Titel
- optional Veröffentlichungsdatum
- Notizen

Mögliche Beziehungen:
- Shooting
- Serie
- Titel
- Printausgabe
- Individual

Model-/Fotografenlogik:
- Standard: vom verknüpften Shooting erben.
- `modelsMode = inherit | custom`
- `creditsMode = inherit | custom`
- `custom` nur für echte Abweichungen oder Werke ohne bekannten Shooting-Bezug.

### Video

Neuer Werktyp `video`.

- eigenständiges Werkobjekt
- Beziehungen zu Shooting, Serie, Titel, Printausgabe und Individual möglich
- Models/Fotografen standardmäßig vom Shooting erben
- gezielte Abweichungen über `custom`

### Printausgabe

Die vorhandene Objektart `publication` soll wieder sichtbar als `Printausgabe` integriert werden.

Geplantes Modell:
- Ausgabe/Bezeichnung
- Jahr
- Monat/Heftnummer
- Land/Edition
- Notizen
- `im Bestand`
- optional PDF
- Coverbild

Beziehungen:
- viele Fotogalerien
- ggf. Videos
- keine Duplikation der Inhalte

Cover:
- technisch ein Medium mit besonderer Rolle
- darf eigene Model-/Fotografen- und optional Shooting-Zuordnungen besitzen

## Schema 6 -- bereits umgesetzt

- `shoot.modelIds`
- `shoot.credits`
- `shoot.location`
- Werktypen `gallery` und `video`
- `modelsMode = inherit | custom`
- `creditsMode = inherit | custom`
- `publication.owned`
- `publication.hasPdf`

Wichtig:
- keine automatische Erzeugung von `gallery`/`video`
- keine automatische Änderung bestehender Shooting-/Medienbeziehungen
- keine künstlichen Daten
- verworfene 6.3.2-Oberfläche „Vorhandenes Archivobjekt“ wurde wieder entfernt

Gerätetest:
- App startet als Update.
- vorhandener Datenbestand bleibt nutzbar.
- Benutzer bestätigte am 2026-09-06: „Läuft.“

## Fachliche Grundregeln

- `Shooting = Entstehung`.
- `Fotogalerie/Video = veröffentlichtes bzw. archiviertes Werk`.
- `Serie/Titel/Printausgabe/Individual = Veröffentlichungskontext`.
- Beziehungen dürfen unvollständig sein.
- Ein bestätigtes Werk darf bekannt sein, obwohl sein Shooting noch unbekannt ist.
- Keine leeren Shootings nur wegen bestätigtem Research-Wissen.
- Keine künstlichen Medienobjekte.
- Externe Magazine/Kampagnen/TV bleiben Karrierefakten.
- Playboy-bezogenes bestätigtes, noch nicht vollständig erschlossenes Wissen bleibt `archiveFact`.

## Reale Referenzfälle

### Tiffany Ryan

- `Busty Babes`: ein Video bestätigt; Serie bekannt; konkretes Shooting/Medium unbekannt.
- `Women of Playboy`: ein Video bestätigt; Serie bekannt; konkretes Shooting/Medium unbekannt.
- Kein Platzhalter-Shooting und kein künstliches Videoobjekt nötig.

### Tahlia Paris

- Cyber Girl of the Month — Januar 2016
- Cyber Girl of the Year — 2017
- Korrektur-/Rückzugsworkflow erfolgreich getestet.

## Nicht wieder einführen

- kein nativer Video-Fullscreen
- kein globaler Ein-Finger-`touchmove` mit `preventDefault()`
- kein `main`-Scrollcontainer mit `height:100vh`
- `DATA_SCHEMA_VERSION` niemals zurücksetzen
- keine künstlichen Shootings/Medien zur Darstellung bestätigten Wissens
- keine dauerhafte Gleichsetzung Shooting mit Pictorial/Fotogalerie
- kein allgemeines 6.3.2-Feld „Vorhandenes Archivobjekt“ für Video/Pictorial-Fakten

## Offene Punkte

- Fotogalerie als erstes echtes Werkobjekt in der UI einführen.
- Danach Video als Werkobjekt.
- Vererbungslogik Models/Fotografen praktisch umsetzen.
- Beziehungen Fotogalerie/Video ↔ Shooting/Serie/Titel/Printausgabe/Individual umsetzen.
- Printausgaben wieder in der Hauptübersicht sichtbar machen.
- Cover-/PDF-/`im Bestand`-Logik testen.
- Bestehende Medien später kontrolliert migrieren.
- Erschließungsgrad anschließend auf echte Fotogalerie-/Video-Objekte umstellen.
- Research-Löschschutz separat testen.

## Zuletzt abgeschlossener Arbeitsblock

**Archivmodell – Schema-6-Grundlage**

- neue Zielstruktur festgelegt
- Schema 6 eingeführt
- `gallery` und `video` vorbereitet
- Vererbungsmodus für Models/Fotografen vorbereitet
- vorhandene `publication` als Basis für Printausgaben vorgesehen
- keine bestehende Archivstruktur automatisch migriert
- verworfene 6.3.2-Zuordnungsoberfläche entfernt
- Gerätetest erfolgreich

Referenz-Commit:
`a6444aaf1702f6ab2be69359afbf342cd1726442`

`www/index.html` Blob:
`14a0702dbaa62553efd077feb9f6226136853e28`

## Nächster sinnvoller Schritt

**Fotogalerie als erstes echtes Werkobjekt einführen.**

Erster Umfang:
- Fotogalerie manuell anlegen können
- Name/Bezeichnung und optionale Notiz
- optional vorhandenes Shooting verknüpfen
- noch keine automatische Medienmigration
- noch keine Serien-/Titel-/Print-/Individual-Massenlogik
- danach Gerätetest mit einer einzelnen Testgalerie
