# Playboy Archiv -- Projektstatus

Stand: 2026-09-08  
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

## Aktueller stabiler Code-Stand

- App-Paket: `de.playboy.archiv`
- Daten-Schema-Version: `6`
- `www/index.html` auf dem fachlich berücksichtigten Code-Stand: Blob `92c4c26f64fac77338411302b44a1d8abd363184`
- Fachlich berücksichtigter Code-Stand: Commit `528d84c9de65fe173272cff30b5e9e3f6e939278`
- `main` stand beim Sitzungsstart am 2026-09-08 auf Commit `40a4fffcc12dd4658673bc8a76a81b8664536f81`.
- Der einzige Commit nach dem Referenz-Commit änderte nur `STATUS.md`; der Code blieb unverändert.
- Schema-6-Grundlage wurde als Update auf dem Gerät erfolgreich getestet.
- Der erste echte Werktyp `gallery` ist in der Verwaltung verfügbar und auf dem Gerät erfolgreich getestet.
- Bestehende Models, Shootings und Medien blieben nutzbar.
- Es findet weiterhin keine automatische Migration vorhandener Medien in Galerie-/Video-Objekte statt.

## Stabile Funktionen

- Archiv-Restore über SAF.
- Permanente Android-Signierung und Update-Installation ohne Deinstallation.
- Fotoanzeige mit Pinch-Zoom.
- Video-Wiedergabe; nativer WebView-Fullscreen bleibt deaktiviert.
- Models, Titel, Serien, Individuals, Shootings und Medienverwaltung funktionieren im bestehenden stabilen Stand.
- Archivfilter, freie Suche, Bewertungs- und Profilbildfilter funktionieren.
- Research mit Status, strukturierten Belegen und kontrollierter Übernahme.
- `careerFacts`, `bioFacts`, `archiveFacts`.
- Korrektur-/Rückzugslogik für bestätigte Fakten.
- Bestätigte Fakten im Model-Profil.
- Bio-Entwurfsgenerator.
- Research-Import.
- Research-Löschschutz ist implementiert; separater Gerätetest dafür weiterhin nicht dokumentiert.
- Erschließungsgrad ist vorhanden und auf dem Gerät getestet.

## Wichtig: bestehender Code versus neues Soll-Modell

Der aktuelle stabile Code verwendet teilweise noch die alten Begriffe und Beziehungen:
- `Fotogalerie`
- `Serie`
- `Individual`
- manuelle Galerie-Bezeichnung
- Galerie mit optionaler Shooting-Verknüpfung

Diese Struktur bleibt bis zu den einzelnen kontrollierten Umbau-Schritten funktionsfähig.

Die folgenden Abschnitte beschreiben das am 2026-09-08 gemeinsam festgelegte fachliche **Soll-Modell**. Es ist noch nicht vollständig im Code umgesetzt.

## Neues fachliches Soll-Modell

### Grundsatz

- `Shooting = Entstehung`
- `Galerie/Video = Werk`
- `Rubrik/Titel/Printausgabe = Veröffentlichungskontext`
- `Seiten = konkrete Fundstelle innerhalb einer Printausgabe`
- Unklare Beziehungen dürfen unklar bleiben.
- Keine künstlichen Shootings, Medien oder Veröffentlichungszuordnungen.

### Archivbereiche

Über der Archivverwaltung soll ein kompakter Archivbereich gewählt werden:

- Playboy
- Special Editions
- Cyber Club
- Playboy Plus

Darunter bleiben die Hauptbereiche:

- Models
- Titel
- Rubriken
- Printausgaben

Regeln:
- Ein Model kann in mehreren Archivbereichen vorkommen.
- Archivbereiche werden nicht automatisch allein anhand eines Datums bestimmt.
- Cyber Club und Playboy Plus bleiben getrennte Archivbereiche.
- Titelidentität kann bereichsübergreifend sein, insbesondere `Cyber Girl`.

### Shooting

Rolle: Entstehungskontext.

Eigene Daten:
- Datum
- Ort

Direkte Beziehungen:
- Model(s)
- Fotograf(en)

Nicht vorgesehen:
- eigener fachlicher Titel
- eigene Notiz ohne konkreten Bedarf
- künstliche Platzhalter nur zur Darstellung bekannten Research-Wissens

Galerie und Video können aus einem Shooting hervorgehen und übernehmen daraus Entstehungsdaten.

### Galerie

Sichtbarer Begriff: **Galerie** statt `Fotogalerie`.

Rolle:
- Foto-Werk/Container
- Fotos werden direkt der Galerie zugeordnet.

Beziehungen:
- Verweis auf das zugehörige Shooting
- direkte Zuordnung zu Veröffentlichungskontexten

Geerbte Angaben aus dem Shooting:
- Models
- Fotograf(en)
- Datum
- Ort

Model-Abweichungen:
- Standardmäßig alle Models des Shootings übernehmen.
- Bei Bedarf einzelne Models für eine Galerie ausschließen.
- Zielmodell eher über Ausschlüsse als über vollständige `custom`-Kopien.

Eigene fachliche Angaben:
- zunächst keine manuell erforderliche Bezeichnung
- zunächst keine eigene Notiz
- kein eigenes Veröffentlichungsdatum erforderlich

### Video

Video ist fachlich parallel zur Galerie.

Rolle:
- Video-Werk/Container
- Videodatei(en) werden direkt dem Video-Werk zugeordnet.

Beziehungen und Vererbung:
- Verweis auf Shooting
- Models, Fotograf(en), Datum und Ort aus Shooting
- bei Bedarf einzelne Models ausschließen
- direkte Zuordnung zu Veröffentlichungskontexten

Eigene fachliche Angaben:
- zunächst keine manuell erforderliche Bezeichnung
- zunächst keine eigene Notiz

Galerie und Video bleiben technisch getrennte Werke, auch wenn sie zur selben Veröffentlichung gehören.

### Konkrete Veröffentlichung

Der zentrale sichtbare Orientierungspunkt ist der **kleinste fachlich sinnvolle Veröffentlichungszusammenhang**.

Beispiele:
- `Busty Babes • Tiffany Ryan`
- `Cyber Girl of the Year 2007 • Breann McGregor`

Galerie und Video werden innerhalb dieses Zusammenhangs zusammengeführt.

Der Veröffentlichungszusammenhang gibt die Identität; Galerie-/Video-Nummern dienen nur zur Unterscheidung mehrerer gleichartiger Werke darin.

### Nummerierungslogik

Nummern laufen **pro konkreter Veröffentlichung** und getrennt nach Werktyp.

Regeln:
- Bei genau einer Galerie sichtbar nur `Galerie`.
- Ab der zweiten Galerie sichtbar `Galerie • Nr. 1`, `Galerie • Nr. 2`, ...
- Bei genau einem Video sichtbar nur `Video`.
- Ab dem zweiten Video sichtbar `Video • Nr. 1`, `Video • Nr. 2`, ...
- Interne Nummern werden bei Erstellung dauerhaft vergeben.
- Gelöschte Nummern werden nicht wiederverwendet.
- Nach Löschung kann die sichtbare Nummerierung wieder ausgeblendet werden, wenn nur noch ein Werk dieses Typs vorhanden ist.
- Die Nummer ist nicht die primäre Identifikation; Vorschau und Kontext müssen eine eindeutige Zuordnung ermöglichen.

### Vorschau einer Veröffentlichung

Bewährte Darstellungslogik soll erhalten bleiben:

- Wenn mindestens eine Galerie vorhanden ist, führt die Galerie die Vorschau.
- Als Vorschau wird nach Möglichkeit das erste geeignete hochformatige Foto der führenden Galerie verwendet.
- Videos werden nicht innerhalb der Galerie angezeigt.
- Gehören Videos zur selben Veröffentlichung, erscheinen anklickbare Videosymbole in der Veröffentlichungsvorschau.
- Bei mehreren Videos erscheinen mehrere anklickbare Videosymbole.
- Enthält die Veröffentlichung nur Video und keine Galerie, dient die Video-Darstellung als Vorschau.
- Bei mehreren Galerien führt zunächst die erste/führende Galerie die Veröffentlichungsvorschau.

### Rubriken

Sichtbarer Begriff: **Rubriken** statt `Serien`.

Beispiele:
- Busty Babes
- Women of Playboy
- Girls with Girls
- College Girls
- Lingerie
- Nudes
- Amateur
- Cyber Girl

Es gibt fachlich zwei Ebenen:
1. Rubrikdefinition, z. B. `Girls with Girls`
2. konkrete Rubrik-Veröffentlichung, z. B. `Girls with Girls • Brandie Moses, Breann McGregor`

Galerie(n) und Video(s) hängen an der konkreten Veröffentlichung.

Models, Fotograf(en), Datum und Ort werden soweit möglich über Galerie/Video → Shooting abgeleitet.

### Playboy Plus und individuelle Veröffentlichungsnamen

Bei Playboy Plus gibt es häufig individuelle Veröffentlichungsnamen, z. B.:
- `Divine Morning`
- `Rustic Charmer`

Diese Namen sind wichtige fachliche Bezeichnungen und haben in der Oberfläche Vorrang vor technischen Werkbezeichnungen wie `Galerie • Nr. 10`.

Regeln:
- Der individuelle Name gehört zur konkreten Veröffentlichung, nicht zur Galerie oder zum Video.
- Wenn Galerie und Video zusammengehören, führt die Galerie die Darstellung.
- Der frühere Begriff `Individual` wird nicht als eigener fachlicher Haupttyp fortgeführt.

### Nicht zugeordnet

`Nicht zugeordnet` soll innerhalb **Rubriken** erscheinen, nicht als eigener Haupttab.

Zweck:
- neutrale Ablage für bekannte Veröffentlichungen, deren korrekte Rubrik noch nicht sicher bestimmt ist
- kein behaupteter historischer Playboy-Rubrikname

Wichtig:
- Ein bekannter individueller Veröffentlichungsname muss gespeichert werden können.
- Wird die richtige Rubrik später bekannt, wird nur die Zuordnung ergänzt/geändert; die Veröffentlichung bleibt bestehen.

Bestehende Legacy-Individuals werden erst in einem späteren kontrollierten Migrationsschritt behandelt.

### Titel

Titel sind Rubriken strukturell ähnlich, besitzen aber Stufen/Hierarchien.

Bekannte Zielstruktur:

- Coed: `of the Week → of the Month`
- Cyber Girl: `of the Week → of the Month → of the Year`
- Special Editions Model: `of the Year`

Beispiel:
- `Cyber Girl of the Year 2007 • Breann McGregor`

Bereichslogik:
- Cyber Club: Cyber Girl Week / Month / Year
- Playboy Plus: Cyber Girl Month / Year
- Die Titelfamilie `Cyber Girl` bleibt bereichsübergreifend dieselbe.
- Archivbereich und Titel dürfen nicht gleichgesetzt werden.
- Kein automatisches `Cyber Girl = Cyber Club`.
- Keine automatische Bereichszuordnung ausschließlich anhand eines Datums.

### Printausgaben

Printausgabe = konkrete physische/digitale Ausgabe als Veröffentlichungskontext.

Eigene Ausgabeangaben:
- Ausgabe/Identität
- Datum
- Cover-Vorschau
- optional PDF
- `im Bestand`

Regeln:
- PDF und physischer Bestand sind unabhängig.
- Eine Ausgabe kann dokumentiert werden, auch wenn weder PDF noch physisches Exemplar vorhanden sind.
- Bereits vorhandene Galerie-/Video-Werke werden bei Print-Wiederverwendung nicht dupliziert.

### Seiten

Unterhalb einer Printausgabe gibt es **Seiten** als konkrete Fundstellen.

Beispiele:
- `Cover`
- `Seite 20`
- `Seiten 20–27`

An einer Seiten-Fundstelle können hängen:
- Model(s)
- Fotograf(en)
- Rubrik
- ggf. Titel
- ggf. Galerie
- ggf. Video

Dadurch können Print-Credits erfasst werden, auch wenn das entsprechende Werk/Medium noch nicht digital im Archiv vorhanden ist.

Das Cover kann als besondere Seiten-Fundstelle behandelt werden und zugleich als Vorschau der Printausgabe dienen.

## Historische/taxonomische Arbeitsannahmen aus der aktuellen Erschließung

Diese Punkte sind fachliche Beobachtungen aus dem vorhandenen Material und dürfen nicht ungeprüft zu automatischen Regeln werden:

### Cyber Club
Beobachtete Titelfamilien:
- Coed: Week → Month
- Cyber Girl: Week → Month → Year

Beobachtete Rubriken u. a.:
- Busty Babes
- Women of Playboy
- Sexy Wives
- Student Bodies

### Playboy Plus
Beobachtete Rubriken:
- Amateur
- Cyber Girl

Beobachtete Cyber-Girl-Titel:
- Month
- Year

Playboy Plus verwendet häufig individuelle Veröffentlichungsnamen.

Die mögliche Entwicklung `Amateur → Cyber Girl` ist noch keine gesicherte automatische Regel.

### Special Editions
Bekannter Titel:
- Special Editions Model of the Year

Beobachtete Rubriken u. a.:
- College Girls
- Lingerie
- Nudes
- Girls with Girls

### Übergang Cyber Club → Playboy Plus
- Coeds und Cyber Girls sind im vorhandenen Material noch bis Februar 2012 dokumentiert.
- Der genaue Plattform-/Strukturwechsel bleibt trotzdem nicht vollständig gesichert.
- Cyber-Girl-Titel existieren auch deutlich später.
- Daher keine automatische Bereichsumschaltung anhand eines festen Datums.

## Reale Referenzfälle

### Tiffany Ryan

Bekannter Bestand:
- `Busty Babes`: 3 Shootings, 2 Pictorials/Galerien, 1 Video
- `Women of Playboy`: 3 Shootings, 2 Pictorials/Galerien, 1 Video
- 4 benannte Pictorials bereits im Archiv
- 2 Videos bestätigt, je eines für Busty Babes und Women of Playboy
- konkrete Shooting-Zuordnung der beiden Videos derzeit nicht sicher

Regel:
- kein Platzhalter-Shooting und kein künstliches Videoobjekt nur zur Darstellung bestätigten Wissens

### Tahlia Paris

Bestätigte Titel:
- Cyber Girl of the Month — Januar 2016
- Cyber Girl of the Year — 2017

Beobachteter Archivbestand:
- 18 Galerien
- 18 Videos
- 18 Shootings
- in den beobachteten Fällen jeweils `1 Galerie = 1 Video = 1 Shooting`

Beobachtete Video-Kennzeichnungen:
- 1× Amateur
- 2× Cyber Girl
- 3× Cyber Girl of the Month
- 12× Cyber Girl of the Year

Diese Beobachtung darf nicht als globale 1:1-Regel erzwungen werden.

## Erschließungsgrad

Im Model-Profil existiert die Karte `Erschließungsgrad`.

Aktueller stabiler Stand:
- keine subjektive Gesamtpunktzahl
- Titel, Serien, Pictorials und Videos werden getrennt betrachtet
- `unbekannt` wird nicht als `0` behandelt
- reale `linkedArchiveIds` reduzieren offene Restmengen und vermeiden Doppelzählungen

Bekanntes Übergangsproblem:
- der Erschließungsgrad verwendet derzeit noch nicht die neuen Galerie-/Video-Werke als endgültige Zählgrundlage
- ein Shooting mit Fotos darf langfristig nicht pauschal mit einer Galerie/Pictorial gleichgesetzt werden

Erst nach stabiler Werk-/Veröffentlichungsmigration umstellen.

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
- `DATA_SCHEMA_VERSION` niemals zurücksetzen.
- Keine automatische Erzeugung von `gallery`/`video`.
- Keine automatische Änderung bestehender Shooting-/Medienbeziehungen.
- Keine künstlichen Daten.
- Verworfene 6.3.2-Oberfläche „Vorhandenes Archivobjekt“ bleibt entfernt.

## Nicht wieder einführen

- kein nativer Video-Fullscreen
- kein globaler Ein-Finger-`touchmove` mit `preventDefault()`
- kein `main`-Scrollcontainer mit `height:100vh`
- `DATA_SCHEMA_VERSION` niemals zurücksetzen
- keine künstlichen Shootings/Medien zur Darstellung bestätigten Wissens
- keine dauerhafte Gleichsetzung Shooting mit Pictorial/Galerie
- kein allgemeines 6.3.2-Feld „Vorhandenes Archivobjekt“ für Video/Pictorial-Fakten
- keine automatische Massenmigration ohne separaten Test
- `Individual` nicht als neuen fachlichen Haupttyp verfestigen
- Archivbereich nicht allein aus Datum oder Titel ableiten

## Offene Umsetzungsblöcke

Die Migration soll klein, kontrolliert und gerätetestbar erfolgen.

Geplante Reihenfolge:

1. **Begriffe und Grundnavigation**
   - sichtbares `Fotogalerie` → `Galerie`
   - sichtbares `Serien` → `Rubriken`
   - Archivbereich-Auswahl vorbereiten
   - keine Bestandsmigration

2. **Shooting / Galerie / Video**
   - neues schlankes Werkmodell
   - automatische Werkbezeichnung
   - Vererbung aus Shooting
   - Model-Ausschlüsse statt unnötiger Vollkopien
   - Medien später kontrolliert zuordnen

3. **Konkrete Veröffentlichung**
   - Rubrik/Titel/individueller Name als gemeinsamer Veröffentlichungszusammenhang
   - Galerie/Video gemeinsam darstellen
   - Nummerierung pro Veröffentlichung
   - Vorschau- und Video-Symbol-Logik

4. **Titel**
   - Titelfamilien und Stufen
   - bereichsabhängig erlaubte Stufen
   - Cyber Girl bereichsübergreifend

5. **Printausgaben**
   - Ausgabe
   - Cover
   - PDF
   - `im Bestand`
   - Seiten-Fundstellen

6. **Kontrollierte Legacy-Migration**
   - Serien → Rubriken
   - Individuals → konkrete Veröffentlichungen / Nicht zugeordnet
   - bestehende Medienbeziehungen
   - Erschließungsgrad auf echte Werke umstellen

## Noch nicht endgültig entschieden

Vor den jeweiligen Umsetzungsblöcken bei Bedarf konkret festlegen:
- genaue technische IDs/Typnamen für Rubrikdefinition versus konkrete Veröffentlichung
- genaue Printausgaben-Identitätsfelder
- technische Darstellung/Sortierung von Seitenbereichen
- Umgang mit Werk ohne bekanntes Shooting (`shootingId = null` zulassen oder später entscheiden)
- endgültige technische Ablage der permanenten Werknummern

## Gerätetests

Erfolgreich dokumentiert:
- Schema-6-Grundlage
- Erschließungsgrad
- erster Galerie-Schritt
- Benutzerbestätigung für die damalige Fotogalerie: „Funktioniert 👍🏻“

Noch nicht separat dokumentiert:
- Research-Löschschutz

Alle kommenden Umbau-Schritte müssen einzeln als App-Update getestet werden. App nicht deinstallieren.

## Zuletzt abgeschlossener Arbeitsblock

**Fachliche Neumodellierung des Archivs**

Am 2026-09-08 wurde das neue Soll-Modell gemeinsam festgelegt:
- Archivbereiche
- Shooting als Entstehung
- Galerie/Video als Werke
- Rubriken statt Serien
- konkrete Veröffentlichung als gemeinsamer fachlicher Zusammenhang
- individuelle Playboy-Plus-Veröffentlichungsnamen
- `Nicht zugeordnet` innerhalb Rubriken
- Titelhierarchien
- Printausgaben mit Seiten
- Nummerierung pro Veröffentlichung
- Galerie-geführte Vorschau mit anklickbaren Videosymbolen

Dieser Arbeitsblock war konzeptionell; es wurde dabei noch kein Code geändert.

Referenz-Commit:
`528d84c9de65fe173272cff30b5e9e3f6e939278`

`www/index.html` Blob:
`92c4c26f64fac77338411302b44a1d8abd363184`

## Nächster sinnvoller Schritt

Etappe 1 klein beginnen:

1. `STATUS.md` mit dem neuen Soll-Modell auf `main` festhalten.
2. Danach `main` erneut prüfen.
3. Anschließend die aktuelle `www/index.html` neu einlesen.
4. Als ersten Code-Schritt nur sichtbare Begriffe/Grundnavigation vorbereiten, ohne Schema- oder Bestandsmigration.
5. Gerätetest als Update.
6. Erst nach erfolgreichem Test den nächsten Funktionsblock beginnen.
