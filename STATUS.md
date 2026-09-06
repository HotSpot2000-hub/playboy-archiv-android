# Playboy Archiv -- Projektstatus

Stand: 2026-09-06  
Referenz-Commit: `528d84c9de65fe173272cff30b5e9e3f6e939278`

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
- `www/index.html` auf `main`: Blob `92c4c26f64fac77338411302b44a1d8abd363184`
- Fachlich berücksichtigter Code-Stand: Commit `528d84c9de65fe173272cff30b5e9e3f6e939278`
- Schema-6-Grundlage wurde als Update auf dem Gerät erfolgreich getestet.
- Der erste echte Werktyp `gallery` ist in der Verwaltung verfügbar und auf dem Gerät erfolgreich getestet.
- Fotogalerien können mit Bezeichnung und optionaler Notiz angelegt werden.
- Eine Fotogalerie kann optional mit einem vorhandenen Shooting verknüpft werden.
- Verknüpfte Models werden aus dem Shooting geerbt und in der Galerieansicht angezeigt.
- Bestehende Models, Shootings und Medien blieben nutzbar.
- Es findet weiterhin keine automatische Migration vorhandener Medien in Fotogalerie-/Video-Objekte statt.

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
- Der Erschließungsgrad verwendet derzeit noch nicht die neuen `gallery`-Objekte als endgültige Zählgrundlage.

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

Werktyp `gallery`.

Aktuell umgesetzt:
- Bezeichnung/Titel
- optionale Notiz
- optionale Verknüpfung zu einem vorhandenen Shooting
- Models werden bei `modelsMode = inherit` dynamisch aus dem verknüpften Shooting abgeleitet
- keine automatische Erzeugung oder Veränderung eines Shootings

Künftig:
- optional Veröffentlichungsdatum
- Beziehungen zu Serie, Titel, Printausgabe und Individual
- Medienzuordnung
- `custom`-Modus für echte Model-/Fotografen-Abweichungen

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
- UI noch nicht umgesetzt

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

Gerätetests:
- Schema-6-Grundlage: erfolgreich.
- Erster Fotogalerie-Schritt: erfolgreich.
- Benutzer bestätigte am 2026-09-06 für die Fotogalerie: „Funktioniert 👍🏻“.

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

- Fotogalerie um weitere Beziehungen erweitern: Serie, Titel, Printausgabe, Individual.
- Medienzuordnung zur Fotogalerie konzipieren und umsetzen.
- Fotografen-Vererbung praktisch in der Galerieoberfläche umsetzen.
- `custom`-Modus für Model-/Fotografen-Abweichungen umsetzen.
- Video als echtes Werkobjekt in der UI einführen.
- Printausgaben wieder in der Hauptübersicht sichtbar machen.
- Cover-/PDF-/`im Bestand`-Logik testen.
- Bestehende Medien später kontrolliert migrieren.
- Erschließungsgrad anschließend auf echte Fotogalerie-/Video-Objekte umstellen.
- Research-Löschschutz separat testen.

## Zuletzt abgeschlossener Arbeitsblock

**Archivmodell – erste echte Fotogalerie**

- eigener Verwaltungsreiter `Fotogalerien`
- Fotogalerie kann manuell angelegt werden
- Bezeichnung und optionale Notiz
- optional vorhandenes Shooting verknüpfen
- kein Shooting wird dadurch erzeugt oder verändert
- Models werden aus verknüpften Shootings geerbt und angezeigt
- Schema bleibt 6
- keine automatische Medienmigration
- Gerätetest erfolgreich

Referenz-Commit:
`528d84c9de65fe173272cff30b5e9e3f6e939278`

`www/index.html` Blob:
`92c4c26f64fac77338411302b44a1d8abd363184`

## Nächster sinnvoller Schritt

Als nächster kleiner Schritt die Fotogalerie weiter ausbauen, ohne bestehende Daten umzubauen.

Empfohlene Reihenfolge:
1. Fotografen-Vererbung aus dem verknüpften Shooting sichtbar machen.
2. Danach weitere fachliche Beziehungen einzeln ergänzen.
3. Erst später Medienzuordnung und kontrollierte Migration bestehender Fotos.

Keine automatische Massenmigration ohne separaten Test.
