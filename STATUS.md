# Playboy Archiv -- Projektstatus

Stand: 2026-09-11  
Referenz-Commit: `52b0c292ffb56d6f0fc3926ea232d1c442a88e46`

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
- Fachlich berücksichtigter Code-Stand: Commit `52b0c292ffb56d6f0fc3926ea232d1c442a88e46`
- `www/index.html` auf diesem Stand: Blob `4cf24084943878d37ec1f63520bfe9312ce32e7b`
- Sichtbare Begriffe wurden in Etappe 1 auf `Rubriken` und `Galerien` umgestellt; technische Legacy-IDs bleiben vorerst unverändert.
- Etappe 2a: Shooting-Erfassung ist auf Datum, Ort, Models und Fotograf(en) ausgerichtet; neue Shootings benötigen keinen manuellen fachlichen Titel und keine Notiz.
- Etappe 2b: Galerien können beim Anlegen mit einem Shooting verknüpft werden und leiten Models, Fotograf(en), Datum und Ort daraus ab.
- Korrektur zu Etappe 2b: Die Shooting-Auswahl einer Galerie zeigt alle vorhandenen Shootings; Datum und Models helfen bei der Unterscheidung.
- Galerie-Grundlogik funktioniert auf dem Gerät: Shooting setzen/ändern/entfernen, Models/Fotograf(en) erben und einzeln ausschließen, Fotos direkt zuordnen und öffnen.
- Video-Grundmodell funktioniert auf dem Gerät: eigener Verwaltungs-Tab `Videos`, optionales Shooting bei Erstellung/Bearbeitung, geerbte Models/Fotograf(en)/Datum/Ort, Ausschlüsse, direkte Videodatei-Zuordnung und Wiedergabe.
- Etappe 3a ist umgesetzt und auf dem Gerät erfolgreich getestet: neuer separater technischer Typ `release` für die kleinste konkrete Veröffentlichung.
- Etappe 3b ist umgesetzt und auf dem Gerät erfolgreich getestet: `release` kann ohne Rubrik/Titel neutral als `Nicht zugeordnet` bestehen und später derselben Veröffentlichung real zugeordnet werden.
- Etappe 3c ist umgesetzt und auf dem Gerät erfolgreich getestet: Galerien und Videos können optional genau einer konkreten Veröffentlichung (`release`) zugeordnet, umgehängt oder wieder gelöst werden; die Shooting-Beziehung bleibt unabhängig bestehen.
- `release` wird sichtbar als `Veröffentlichung` geführt und kann angelegt, bearbeitet und gelöscht werden.
- Eine konkrete Veröffentlichung kann einer Rubrik (`series`), einem Titel (`title`) oder beiden zugeordnet werden.
- Der bestehende technische Typ `publication` bleibt unverändert und steht weiterhin für die bisherige Printpublikations-/Printausgabenlogik; er wurde nicht umgedeutet.
- Es gibt weiterhin keine automatische Legacy-Migration und keinen Schema-Bump.

## Stabile Funktionen

- Archiv-Restore über SAF.
- Permanente Android-Signierung und Update-Installation ohne Deinstallation.
- Fotoanzeige mit Pinch-Zoom.
- Video-Wiedergabe; nativer WebView-Fullscreen bleibt deaktiviert.
- Models, Titel, Rubriken (technisch weiterhin Legacy-`series`), Individuals, Shootings, Galerien, Videos, Veröffentlichungen (`release`) und Medienverwaltung funktionieren im bestehenden stabilen Stand.
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

Der aktuelle stabile Code verwendet teilweise noch alte technische Typen und Beziehungen:
- technischer Typ `series` bleibt vorerst bestehen, sichtbar heißt er `Rubrik`
- technischer Typ `gallery` ist sichtbar `Galerie`
- technischer Typ `video` ist das eigenständige Video-Werk
- technischer Typ `release` ist seit Etappe 3a die konkrete Veröffentlichung
- technischer Typ `publication` bleibt die bisherige Printpublikations-/Printausgabenstruktur und darf nicht mit `release` gleichgesetzt werden
- `Individual` ist strukturell noch vorhanden
- neue Galerien benötigen keine manuelle Bezeichnung und keine Notiz; bestehende Legacy-Werte bleiben unangetastet
- Galerie und Video können mit einem Shooting verknüpft werden
- Galerie und Video können optional direkt mit genau einer konkreten Veröffentlichung (`release`) verknüpft werden

Diese Struktur bleibt bis zu den einzelnen kontrollierten Umbau-Schritten funktionsfähig.

## Neues fachliches Soll-Modell

### Grundsatz

- `Shooting = Entstehung`
- `Galerie/Video = Werk`
- `Rubrik/Titel/Printausgabe = Veröffentlichungskontext`
- `release = kleinste konkrete Veröffentlichung`
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

Sichtbarer Begriff: **Galerie**.

Rolle:
- Foto-Werk/Container
- Fotos werden direkt der Galerie zugeordnet.

Beziehungen:
- Verweis auf das zugehörige Shooting
- direkte optionale Zuordnung zu einer konkreten Veröffentlichung (`release`)

Geerbte Angaben aus dem Shooting:
- Models
- Fotograf(en)
- Datum
- Ort

Model-Abweichungen:
- Standardmäßig alle Models des Shootings übernehmen.
- Bei Bedarf einzelne Models für eine Galerie ausschließen.
- Ausschlüsse statt unnötiger Vollkopien bevorzugen.

Eigene fachliche Angaben:
- keine manuell erforderliche Bezeichnung
- keine eigene Notiz erforderlich
- kein eigenes Veröffentlichungsdatum erforderlich

### Video

Video ist fachlich parallel zur Galerie.

Rolle:
- Video-Werk/Container
- Videodatei(en) werden direkt dem Video-Werk zugeordnet.

Beziehungen und Vererbung:
- Verweis auf Shooting
- Models, Fotograf(en), Datum und Ort aus Shooting
- bei Bedarf einzelne Models/Fotograf(en) ausschließen
- direkte optionale Zuordnung zu einer konkreten Veröffentlichung (`release`)

Galerie und Video bleiben technisch getrennte Werke, auch wenn sie zur selben Veröffentlichung gehören.

### Konkrete Veröffentlichung

Der zentrale sichtbare Orientierungspunkt ist der **kleinste fachlich sinnvolle Veröffentlichungszusammenhang**.

Beispiele:
- `Busty Babes • Tiffany Ryan`
- `Cyber Girl of the Year 2007 • Breann McGregor`

Seit Etappe 3a existiert dafür der technische Typ `release`.

Aktuell umgesetzt:
- `release` anlegen
- `release` bearbeiten
- `release` löschen
- optionale Zuordnung zu einer Rubrik
- optionale Zuordnung zu einem Titel
- Rubrik und Titel können kombiniert werden
- neutraler Fall `Nicht zugeordnet` ohne künstliches Rubrikobjekt
- optionale Zuordnung von Galerie und Video zu `release`; Zuordnung kann gesetzt, gewechselt und entfernt werden

Noch nicht umgesetzt:
- individueller Veröffentlichungsname
- Nummerierung pro Veröffentlichung
- endgültige Vorschau-/Darstellungslogik

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

Ziel:
- Wenn mindestens eine Galerie vorhanden ist, führt die Galerie die Vorschau.
- Nach Möglichkeit erstes geeignetes hochformatiges Foto der führenden Galerie verwenden.
- Videos werden nicht innerhalb der Galerie angezeigt.
- Videos derselben Veröffentlichung erscheinen als anklickbare Videosymbole in der Veröffentlichungsvorschau.
- Bei mehreren Videos erscheinen mehrere Symbole.
- Video-only-Veröffentlichungen verwenden eine Video-Darstellung.
- Bei mehreren Galerien führt zunächst die erste/führende Galerie.

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

Seit Etappe 3a kann eine konkrete Veröffentlichung (`release`) einer Rubrikdefinition (`series`) zugeordnet werden.

Galerie(n) und Video(s) können seit Etappe 3c an der konkreten Veröffentlichung hängen.
Models, Fotograf(en), Datum und Ort werden soweit möglich über Galerie/Video → Shooting abgeleitet.

### Playboy Plus und individuelle Veröffentlichungsnamen

Bei Playboy Plus gibt es häufig individuelle Veröffentlichungsnamen, z. B.:
- `Divine Morning`
- `Rustic Charmer`

Regeln:
- Der individuelle Name gehört zur konkreten Veröffentlichung, nicht zur Galerie oder zum Video.
- Wenn Galerie und Video zusammengehören, führt die Galerie die Darstellung.
- Der frühere Begriff `Individual` wird nicht als eigener fachlicher Haupttyp fortgeführt.

Noch nicht umgesetzt: eigenes Namensfeld am `release`.

### Nicht zugeordnet

`Nicht zugeordnet` soll innerhalb **Rubriken** erscheinen, nicht als eigener Haupttab.

Zweck:
- neutrale Ablage für bekannte Veröffentlichungen, deren korrekte Rubrik noch nicht sicher bestimmt ist
- kein behaupteter historischer Playboy-Rubrikname

Wichtig:
- Ein bekannter individueller Veröffentlichungsname muss gespeichert werden können.
- Wird die richtige Rubrik später bekannt, wird nur die Zuordnung ergänzt/geändert; die Veröffentlichung bleibt bestehen.
- Bestehende Legacy-Individuals werden erst in einem späteren kontrollierten Migrationsschritt behandelt.

Dieser Punkt ist seit Etappe 3b umgesetzt und auf dem Gerät getestet.

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

Seit Etappe 3a kann `release` bereits optional einem bestehenden Titel zugeordnet werden. Titelhierarchien sind noch nicht umgesetzt.

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

Der bestehende technische Typ `publication` bleibt bis zum späteren kontrollierten Printausgaben-Umbau bestehen.

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

## Historische/taxonomische Arbeitsannahmen

Diese Punkte sind fachliche Beobachtungen und dürfen nicht ungeprüft zu automatischen Regeln werden.

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
- Der genaue Plattform-/Strukturwechsel bleibt nicht vollständig gesichert.
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
- der Erschließungsgrad verwendet noch nicht die neuen Galerie-/Video-/Veröffentlichungsobjekte als endgültige Zählgrundlage
- ein Shooting mit Fotos darf langfristig nicht pauschal mit einer Galerie/Pictorial gleichgesetzt werden

Erst nach stabiler Werk-/Veröffentlichungsmigration umstellen.

## Schema 6 -- bereits umgesetzt

- `shoot.modelIds`
- `shoot.credits`
- `shoot.location`
- Werktypen `gallery` und `video`
- konkreter Veröffentlichungstyp `release` ohne Schema-Bump
- `modelsMode = inherit | custom`
- `creditsMode = inherit | custom`
- `publication.owned`
- `publication.hasPdf`

Wichtig:
- `DATA_SCHEMA_VERSION` niemals zurücksetzen.
- Keine automatische Erzeugung von `gallery`/`video`/`release` aus Legacy-Daten.
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
- bestehenden technischen Typ `publication` nicht als neue konkrete Veröffentlichung (`release`) umdeuten

## Offene Umsetzungsblöcke

Die Migration soll klein, kontrolliert und gerätetestbar erfolgen.

1. **Begriffe und Grundnavigation** — umgesetzt/getestet
   - sichtbares `Fotogalerie` → `Galerie`
   - sichtbares `Serien` → `Rubriken`
   - keine Bestandsmigration

2. **Shooting / Galerie / Video** — Grundlogik umgesetzt/getestet
   - schlankes Werkmodell
   - automatische Werkbezeichnung
   - Vererbung aus Shooting
   - Model-/Fotografen-Ausschlüsse
   - direkte Medienzuordnung

3. **Konkrete Veröffentlichung** — in Arbeit
   - 3a: technischer Typ `release`, CRUD, Rubrik-/Titel-Zuordnung — umgesetzt/getestet
   - 3b: `Nicht zugeordnet` innerhalb Rubriken — umgesetzt/getestet
   - 3c: Galerie/Video an konkrete Veröffentlichung hängen — umgesetzt/getestet
   - 3d: individueller Veröffentlichungsname — als Nächstes
   - danach Nummerierung, Vorschau- und Video-Symbol-Logik

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
- genaue Printausgaben-Identitätsfelder
- technische Darstellung/Sortierung von Seitenbereichen
- Umgang mit Werk ohne bekanntes Shooting (`shootingId = null` zulassen oder später entscheiden)
- endgültige technische Ablage der permanenten Werknummern
- endgültige sichtbare Benennung/Sortierung konkreter Veröffentlichungen ohne individuellen Namen

Die technische ID der konkreten Veröffentlichung ist seit Etappe 3a entschieden: `release`.

## Gerätetests

Erfolgreich dokumentiert:
- Schema-6-Grundlage
- Erschließungsgrad
- Etappe 1: sichtbare Begriffe `Rubriken` / `Galerien`
- Etappe 2a: Shooting mit Datum, Ort, Models und Fotograf(en)
- Etappe 2b: Galerie erbt Models, Fotograf(en), Datum und Ort aus dem Shooting
- Korrektur Etappe 2b: neu angelegte Shootings erscheinen in der Galerie-Shooting-Auswahl
- Etappe 2c: bestehende Galerie bearbeiten und Shooting nachträglich zuordnen, ändern oder entfernen
- Etappe 2d: neue Galerie ohne manuelle Bezeichnung/Notiz; bestehende Legacy-Werte bleiben unangetastet
- Galerie: Model-/Fotografen-Ausnahmen, direkte Foto-Zuordnung sowie Anzeige/Öffnung direkt zugeordneter Fotos
- Video: nachträgliche Shooting-Bearbeitung
- Video: Model-/Fotografen-Ausnahmen
- Video: direkte Videodatei-Zuordnung und Wiedergabe über die bestehende Medienlogik
- Etappe 3a: `Veröffentlichungen` (`release`) als eigener Verwaltungstyp; Anlegen, Bearbeiten, Löschen sowie Zuordnung nur Rubrik, nur Titel oder Rubrik + Titel erfolgreich getestet
- Etappe 3b: `Nicht zugeordnet` als neutraler Rubrik-Fall; ohne künstliches Rubrikobjekt, mit späterer Umstellung derselben Veröffentlichung auf reale Rubrik und zurück erfolgreich getestet
- Etappe 3c: Galerie und Video optional einer konkreten Veröffentlichung zuordnen, Zuordnung wechseln und entfernen; vorhandene Shooting-Zuordnung bleibt dabei erhalten
- bestehende Models, Shootings, Galerien, Videos und Printpublikationen blieben dabei funktionsfähig

Noch nicht separat dokumentiert:
- Research-Löschschutz

Alle kommenden Umbau-Schritte müssen einzeln als App-Update getestet werden. App nicht deinstallieren.

## Zuletzt abgeschlossener Arbeitsblock

**Etappe 3c -- Galerie und Video einer konkreten Veröffentlichung zuordnen**

Auf dem Gerät erfolgreich getestet:
- Galerie kann optional einer vorhandenen konkreten Veröffentlichung (`release`) zugeordnet werden
- Video kann optional einer vorhandenen konkreten Veröffentlichung (`release`) zugeordnet werden
- Zuordnung kann bei bestehenden Galerie-/Video-Werken gesetzt werden
- Zuordnung kann auf eine andere Veröffentlichung gewechselt werden
- Zuordnung kann wieder entfernt werden
- die bestehende Shooting-Beziehung bleibt dabei erhalten und unabhängig
- Galerie und Video bleiben getrennte Werkobjekte
- keine automatische Legacy-Migration
- Daten-Schema bleibt `6`

Bewusst noch nicht umgesetzt:
- individueller Veröffentlichungsname
- Nummerierungslogik pro Veröffentlichung
- endgültige Veröffentlichungsvorschau / Video-Symbol-Logik
- kontrollierte Legacy-Migration
- optischer und praktischer Feinschliff

Referenz-Commit:
`52b0c292ffb56d6f0fc3926ea232d1c442a88e46`

`www/index.html` Blob:
`4cf24084943878d37ec1f63520bfe9312ce32e7b`

## Nächster sinnvoller Schritt

**Etappe 3d: individueller Veröffentlichungsname an `release`.**

Ziel:
1. Eine konkrete Veröffentlichung erhält ein optionales eigenes Namensfeld, z. B. `Divine Morning` oder `Rustic Charmer`.
2. Der Name gehört ausschließlich zur Veröffentlichung (`release`), nicht zu Galerie oder Video.
3. Der Name muss bei bestehenden Veröffentlichungen gesetzt, geändert und entfernt werden können.
4. Rubrik- und Titel-Zuordnung bleiben unabhängig davon bestehen.
5. Bei vorhandenem individuellen Namen soll dieser die fachliche Identität der Veröffentlichung sichtbar ergänzen, ohne Rubrik/Titel zu verlieren.
6. Noch keine Nummerierung und keine endgültige Vorschau-Hierarchie.
7. Keine Legacy-Migration und kein Schema-Bump.
