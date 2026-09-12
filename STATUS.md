# Playboy Archiv -- Projektstatus

Stand: 2026-09-12  
Referenz-Commit: `c7b69be184b04111d275688d92701061c4b599ed`

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
- Vor jedem Gerätetest zuerst erklären:
  1. Was soll fachlich passieren?
  2. Was wurde geändert?
  3. Wo ist es sichtbar?
  4. Woran erkennt man das korrekte Verhalten?

## Aktueller stabiler Code-Stand

- App-Paket: `de.playboy.archiv`
- Daten-Schema-Version: `6`
- Fachlich berücksichtigter und auf dem Gerät getesteter Code-Stand: Commit `c7b69be184b04111d275688d92701061c4b599ed`
- `www/index.html` auf diesem Stand: Blob `d6adefa24efbbf9b9f12b0d7871bbc318e0b68da`
- Etappe 4a ist auf dem Gerät erfolgreich getestet.
- Keine automatische Legacy-Migration.
- Kein Schema-Bump.

### Etappe 4a -- Rubriken verständlich machen

Fachliches Ziel:
- Rubrik = Veröffentlichungskontext.
- Darunter erscheinen konkrete Beiträge.
- Galerie und Video gehören zum Beitrag.
- Shooting bleibt Entstehungskontext und erscheint nicht als nächste Hierarchieebene innerhalb einer Rubrik.
- `release` bleibt technischer Träger des Beitrags, wird aber nicht als zusätzlicher gleichrangiger Hauptbereich etabliert.

Auf dem Gerät erfolgreich getestet:
- Öffnen einer echten Rubrik zeigt Beiträge statt Shootings.
- Beitragskarten zeigen den vorhandenen Beitragsnamen.
- Galerie-/Video-Anzahlen bleiben sichtbar.
- Galerie-/Video-Zugriffe funktionieren.
- `Beitrag bearbeiten` funktioniert.
- `Rubriken → Nicht zugeordnet` bleibt als Arbeitsliste erhalten und verwendet die sichtbare Sprache `Beitrag`.
- Titel, Printausgaben, Galerien, Videos und Shootings bleiben separat erreichbar.
- Keine Datenmigration und kein Schema-Bump.

## Historischer Hinweis zum verworfenen ersten 4a-Entwurf

Nach Etappe 3g wurde zunächst ein nicht gerätetesteter Entwurf hochgeladen:
- Commit `7d17fc8f0c6ca8faa022431068c3623e383bb76e`
- damaliges `www/index.html` Blob `6f959ec768b6092462b3bb209bd7e774b717bc28`

Dieser Entwurf machte `Veröffentlichung` zu stark als zusätzliche sichtbare Bedienebene und wurde fachlich verworfen.

Der aktuelle stabile 4a-Stand ersetzt diese Sicht bewusst:
- sichtbar ist `Beitrag`
- der Beitrag steht innerhalb seines Veröffentlichungskontexts
- `release` bleibt intern nützlich, wird aber nicht als konkurrierender Hauptbereich behandelt

## Stabile Funktionen

- Archiv-Restore über SAF.
- Permanente Android-Signierung und Update-Installation ohne Deinstallation.
- Fotoanzeige mit Pinch-Zoom.
- Video-Wiedergabe; nativer WebView-Fullscreen bleibt deaktiviert.
- Models, Titel, Rubriken, Individuals, Shootings, Galerien, Videos, Veröffentlichungen (`release`) und Medienverwaltung funktionieren im bestehenden stabilen Stand.
- Archivfilter, freie Suche, Bewertungs- und Profilbildfilter funktionieren.
- Research mit Status, strukturierten Belegen und kontrollierter Übernahme.
- `careerFacts`, `bioFacts`, `archiveFacts`.
- Korrektur-/Rückzugslogik für bestätigte Fakten.
- Bestätigte Fakten im Model-Profil.
- Bio-Entwurfsgenerator.
- Research-Import.
- Research-Löschschutz ist implementiert; separater Gerätetest weiterhin nicht dokumentiert.
- Erschließungsgrad ist vorhanden und auf dem Gerät getestet.

## Bereits abgeschlossene Umbau-Etappen

### Etappe 1
- sichtbares `Fotogalerie` → `Galerie`
- sichtbares `Serien` → `Rubriken`
- getestet

### Etappe 2
- Shooting auf Datum, Ort, Models und Fotograf(en) ausgerichtet
- Galerie mit optionalem Shooting
- Galerie erbt Models/Fotograf(en)/Datum/Ort
- Shooting kann an bestehender Galerie gesetzt/geändert/entfernt werden
- Galerie ohne zwingenden manuellen Titel/Notiz
- Model-/Fotografen-Ausschlüsse
- direkte Fotozuordnung und Anzeige
- eigenständiger Video-Typ
- Video mit optionalem Shooting
- geerbte Daten und Ausschlüsse
- direkte Videodatei-Zuordnung und Wiedergabe
- getestet

### Etappe 3a
- technischer Typ `release` eingeführt
- CRUD
- optionale Zuordnung zu Rubrik und/oder Titel
- getestet

### Etappe 3b
- neutraler Fall `Nicht zugeordnet`
- kein künstliches `series`-Objekt
- getestet

### Etappe 3c
- Galerie/Video können optional genau einem `release` zugeordnet werden
- Zuordnung setzen, wechseln und entfernen
- Shooting-Beziehung bleibt unabhängig
- getestet

### Etappe 3d
- optionaler individueller Veröffentlichungsname am `release`
- setzen, ändern, entfernen
- getestet

### Etappe 3e
- permanente interne Werknummern pro `release`, getrennt für Galerie und Video
- sichtbare Werknummern erst ab mehreren Werken desselben Typs
- gelöschte Nummern werden nicht wiederverwendet
- technisch stabil und getestet
- fachlich jedoch nicht mehr als endgültiges sichtbares Nummerierungsziel anzusehen

### Etappe 3f
- galeriegeführte Veröffentlichungsvorschau
- bevorzugt geeignetes Hochformatfoto
- Galerie-/Video-Werkzugriffe
- separate Video-Schnellzugriffe
- Video-only-Darstellung
- getestet

### Etappe 3g
- `Nicht zugeordnet` sichtbar innerhalb Rubriken
- Zähler für `release` ohne Rubrik
- Arbeitsliste
- Bearbeiten-Zugriff
- automatische Entfernung aus der Liste nach Rubrik-Zuordnung
- Modellzugehörigkeit kann über Galerie/Video → Shooting abgeleitet werden
- kein künstliches Rubrikobjekt
- getestet

### Etappe 4a
- Rubriken zeigen konkrete Beiträge statt Shootings als nächste Ebene
- sichtbarer Begriff `Beitrag` statt zusätzliche Bedienebene `Veröffentlichung`
- Galerie/Video bleiben direkt am Beitrag erreichbar
- `Nicht zugeordnet` spricht ebenfalls von Beiträgen
- keine Nummerierungsänderung
- keine Migration
- kein Schema-Bump
- getestet

## Wichtig: bestehender Code versus Soll-Modell

Bestehende technische Typen:
- `series` = Rubrik
- `gallery` = Galerie
- `video` = Video-Werk
- `release` = konkreter Beitrag / konkrete Veröffentlichung
- `publication` = bestehende Printpublikations-/Printausgabenstruktur
- `Individual` ist strukturell noch vorhanden

Regeln:
- `publication` darf nicht mit `release` gleichgesetzt werden.
- Galerie und Video können mit einem Shooting verknüpft sein.
- Galerie und Video können optional einem `release` zugeordnet sein.
- Ein `release` kann Rubrik, Titel oder beides haben.
- Ein `release` kann einen individuellen Originalnamen besitzen.
- Unklare Beziehungen dürfen unklar bleiben.
- Keine künstlichen Shootings, Medien oder Veröffentlichungszuordnungen.

## Fachliches Soll-Modell

### Grundsatz

- `Shooting = Entstehung`
- `Galerie/Video = Werk bzw. Medium`
- `Rubrik/Titel/Printausgabe = Veröffentlichungskontexte`
- `release = einzelner konkreter Beitrag innerhalb eines Veröffentlichungskontexts`
- `Seiten = konkrete Fundstelle innerhalb einer Printausgabe`

`release` ist technisch wichtig zur Bündelung zusammengehöriger Galerie/Video, aber kein zusätzlicher gleichrangiger Hauptbereich für den Benutzer.

### Verwaltung und Archivübersicht

Rubrik, Titel und Printausgabe bleiben getrennt.

Verwaltung:
- Rubriken
- Titel
- Printausgaben
- Galerien
- Videos
- Shootings

Archivübersicht:
- Rubriken
- Titel
- Printausgaben

Ähnliche technische Bedienmuster dürfen intern wiederverwendet werden, ohne diese fachlichen Bereiche sichtbar zusammenzuführen.

### Archivbereiche

Ziel:
- Playboy
- Special Editions
- Cyber Club
- Playboy Plus

Regeln:
- Ein Model kann in mehreren Archivbereichen vorkommen.
- Keine automatische Bereichszuordnung allein anhand eines Datums.
- Cyber Club und Playboy Plus bleiben getrennt.
- Titelidentität kann bereichsübergreifend sein, insbesondere `Cyber Girl`.

### Shooting

Rolle:
- Entstehungskontext

Eigene Daten:
- Datum
- Ort

Direkte Beziehungen:
- Model(s)
- Fotograf(en)

Nicht vorgesehen:
- eigener fachlicher Titel
- eigene Notiz ohne konkreten Bedarf
- künstliche Platzhalter

### Galerie

Rolle:
- Foto-Werk/Container
- Fotos direkt zugeordnet

Beziehungen:
- optionales Shooting
- optionale direkte Zuordnung zu einem `release`

Geerbt aus Shooting:
- Models
- Fotograf(en)
- Datum
- Ort

Bei Bedarf:
- einzelne Models/Fotograf(en) ausschließen

Keine zwingende eigene fachliche Bezeichnung oder Notiz.

### Video

Fachlich parallel zur Galerie.

Rolle:
- Video-Werk/Container
- Videodatei(en) direkt zugeordnet

Beziehungen:
- optionales Shooting
- optionale direkte Zuordnung zu einem `release`

Geerbte Daten und Ausschlüsse analog Galerie.

### Konkreter Beitrag (`release`)

Der Beitrag ist der kleinste fachlich sinnvolle Veröffentlichungszusammenhang.

Beispiele:
- `Busty Babes • Tiffany Ryan • Nr. 1`
- `Cyber Girl of the Year • Breann McGregor • Nr. 1`
- `Divine Morning`
- `High Time`
- `Hot Sands`

Regeln:
- Hat der Beitrag einen echten individuellen Originalnamen, hat dieser sichtbar Vorrang.
- Fehlt ein Originalname, soll die sichtbare Bezeichnung generisch aus Kontext + Model + Beitrags-/Veröffentlichungsnummer gebildet werden.
- Galerie und Video sind Medien dieses Beitrags.
- Shooting ist die Entstehung und nicht die Veröffentlichung.
- Ein Beitrag kann Rubrik, Titel oder beides zugeordnet sein.
- Ein Beitrag soll nicht als eigener konkurrierender Hauptbereich zwischen Kontext und Medien erscheinen.

### Nummerierungslogik -- fachliches Ziel

Die bestehende 3e-Nummerierung nummeriert Galerie und Video innerhalb eines `release`. Diese Logik ist technisch stabil, entspricht aber nicht mehr dem endgültigen sichtbaren Ziel.

Neues Ziel:
- Die sichtbare `Nr. 1`, `Nr. 2`, ... unterscheidet primär mehrere konkrete Beiträge desselben Veröffentlichungskontexts und Models.
- Galerie und Video derselben Veröffentlichung gehören zu demselben Beitrag und erhalten daraus keine getrennte Identität.
- Beispiel:
  - `Busty Babes • Tiffany Ryan • Nr. 1`
  - `Busty Babes • Tiffany Ryan • Nr. 2`
- Hat der Beitrag einen Originalnamen, wird dieser sichtbar bevorzugt:
  - `High Time`
  - `Hot Sands`
- Vor Umsetzung muss geprüft werden, welche bestehende Nummernlogik wiederverwendet werden kann, ohne Bestandsdaten zu beschädigen.
- Keine Migration oder Umnummerierung ungeprüft durchführen.

### Rubriken

Sichtbarer Begriff: **Rubriken**.

Beispiele:
- Busty Babes
- Women of Playboy
- Girls with Girls
- College Girls
- Lingerie
- Nudes
- Amateur
- Cyber Girl

Hierarchie nach 4a:
- Rubrik
- konkreter Beitrag
- Galerie / Video

Beispiel:
- `Busty Babes`
  - `Tiffany Ryan • Nr. 1`
    - Galerie
    - Video

Shootings erscheinen hier nicht als Hierarchieebene.

### Nicht zugeordnet

`Nicht zugeordnet` bleibt innerhalb **Rubriken**, nicht als eigener Haupttab.

Zweck:
- Arbeitsbestand für Beiträge/Medien, deren korrekter Veröffentlichungskontext noch nicht eindeutig geklärt ist.
- kein behaupteter historischer Playboy-Rubrikname
- kein künstliches `series`-Objekt

Aktuell getestet:
- `release` ohne Rubrik erscheint dort.
- Zuordnung zu echter Rubrik entfernt den Beitrag aus der Liste.
- sichtbare Sprache lautet seit 4a `Beitrag`.

Noch offen:
- Bedienmodell so vereinfachen, dass auch noch nicht eindeutig einem `release` zugeordnete Medien dort verständlich auffindbar werden, ohne technische Zwischenzustände sichtbar machen zu müssen.

### Titel

Titel bleiben eigener Verwaltungs- und Übersichtsbereich.

Bekannte Zielstruktur:
- Coed: `of the Week → of the Month`
- Cyber Girl: `of the Week → of the Month → of the Year`
- Special Editions Model: `of the Year`

Regeln:
- Cyber Girl kann bereichsübergreifend dieselbe Titelfamilie sein.
- Kein automatisches `Cyber Girl = Cyber Club`.
- Keine automatische Bereichszuordnung ausschließlich anhand eines Datums.
- `release` kann bereits optional einem Titel zugeordnet werden.
- Titelhierarchien sind noch nicht umgesetzt.

### Printausgaben

Printausgabe = konkreter physischer/digitaler Veröffentlichungskontext.

Eigene Angaben:
- Ausgabe/Identität
- Datum
- Cover-Vorschau
- optional PDF
- `im Bestand`

Regeln:
- PDF und physischer Bestand sind unabhängig.
- Eine Ausgabe kann dokumentiert werden, auch wenn weder PDF noch physisches Exemplar vorhanden ist.
- Bereits vorhandene Galerie-/Video-Werke werden bei Print-Wiederverwendung nicht dupliziert.
- technischer Typ `publication` bleibt bis zum kontrollierten Umbau bestehen.

### Seiten

Unterhalb einer Printausgabe:
- Cover
- Seite
- Seitenbereich

An einer Fundstelle können hängen:
- Model(s)
- Fotograf(en)
- Rubrik
- ggf. Titel
- ggf. Galerie
- ggf. Video

## Historische/taxonomische Arbeitsannahmen

Diese Punkte sind Beobachtungen und dürfen nicht ungeprüft zu automatischen Regeln werden.

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

Die mögliche Entwicklung `Amateur → Cyber Girl` ist keine gesicherte automatische Regel.

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
- Keine automatische Bereichsumschaltung anhand eines festen Datums.

## Reale Referenzfälle

### Tiffany Ryan

Bekannter Bestand:
- `Busty Babes`: 3 Shootings, 2 Pictorials/Galerien, 1 Video
- `Women of Playboy`: 3 Shootings, 2 Pictorials/Galerien, 1 Video
- 4 benannte Pictorials bereits im Archiv
- 2 Videos bestätigt, je eines für Busty Babes und Women of Playboy
- konkrete Shooting-Zuordnung der beiden Videos derzeit nicht sicher

Regel:
- kein Platzhalter-Shooting
- kein künstliches Videoobjekt nur zur Darstellung bestätigten Wissens

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

Individuelle Namen in ihrem Bestand zeigen, dass Originalnamen nicht auf Playboy Plus als technische Sonderregel beschränkt werden dürfen:
- `High Time`
- `Hot Sands`

Die beobachtete 1:1-Struktur darf nicht als globale Regel erzwungen werden.

## Erschließungsgrad

Im Model-Profil existiert die Karte `Erschließungsgrad`.

Aktueller stabiler Stand:
- keine subjektive Gesamtpunktzahl
- Titel, Serien, Pictorials und Videos getrennt
- `unbekannt` wird nicht als `0` behandelt
- reale `linkedArchiveIds` reduzieren offene Restmengen und vermeiden Doppelzählungen

Bekanntes Übergangsproblem:
- verwendet noch nicht Galerie/Video/Beitrag als endgültige Zählgrundlage
- ein Shooting mit Fotos darf langfristig nicht pauschal mit einer Galerie gleichgesetzt werden

Erst nach stabiler Werk-/Beitragsmigration umstellen.

## Schema 6 -- bereits umgesetzt

- `shoot.modelIds`
- `shoot.credits`
- `shoot.location`
- Werktypen `gallery` und `video`
- technischer Beitragstyp `release`
- `modelsMode = inherit | custom`
- `creditsMode = inherit | custom`
- `publication.owned`
- `publication.hasPdf`

Wichtig:
- `DATA_SCHEMA_VERSION` niemals zurücksetzen.
- Keine automatische Erzeugung von `gallery`/`video`/`release` aus Legacy-Daten.
- Keine automatische Änderung bestehender Shooting-/Medienbeziehungen.
- Keine künstlichen Daten.
- Verworfene 6.3.2-Oberfläche `Vorhandenes Archivobjekt` bleibt entfernt.

## Nicht wieder einführen

- kein nativer Video-Fullscreen
- kein globaler Ein-Finger-`touchmove` mit `preventDefault()`
- kein `main`-Scrollcontainer mit `height:100vh`
- `DATA_SCHEMA_VERSION` niemals zurücksetzen
- keine künstlichen Shootings/Medien
- keine dauerhafte Gleichsetzung Shooting mit Pictorial/Galerie
- kein allgemeines 6.3.2-Feld `Vorhandenes Archivobjekt` für Video/Pictorial-Fakten
- keine automatische Massenmigration ohne separaten Test
- `Individual` nicht als neuen fachlichen Haupttyp verfestigen
- Archivbereich nicht allein aus Datum oder Titel ableiten
- `publication` nicht als `release` umdeuten
- Rubrik, Titel und Printausgabe nicht in einer gemeinsamen Verwaltungsansicht vermischen
- `release` nicht wieder als konkurrierenden sichtbaren Hauptbereich etablieren

## Play-Protect-Hinweis

Während der 3g-Eingrenzung blockierte Google Play Protect einen Zwischenbuild.

Gesichert:
- kontrollierte Diagnosebuilds und der abschließende 3g-Stand ließen sich als UPDATE installieren
- daraus wird keine eindeutige Quellcode-Ursache abgeleitet

Regel:
- Play Protect bleibt aktiviert.
- Warnungen werden nicht blind übergangen.
- App bei Tests nicht deinstallieren.

## Offene Umsetzungsblöcke

### 4b -- Beitragsnummerierung und generische Beitragsbezeichnung
Nächster Schritt.

Ziel:
- sichtbare Nummer gehört zum konkreten Beitrag, nicht zu Galerie/Video
- Originalname hat Vorrang
- ohne Originalname: Kontext + Model + Nr.
- vorhandene 3e-Werknummerierung zunächst technisch analysieren
- keine Bestandsdaten beschädigen
- keine Migration oder Schemaänderung ohne separaten Beschluss/Test

### Danach: Titel
- Titelansicht nach demselben verständlichen Grundmuster wie Rubriken
- Titel bleiben eigener Bereich
- Titelfamilien und Stufen
- bereichsabhängig erlaubte Stufen
- Cyber Girl bereichsübergreifend

### Danach: Printausgaben
- Ausgabe
- Cover
- PDF
- `im Bestand`
- Seiten-Fundstellen

### Später: kontrollierte Legacy-Migration
- Serien → Rubriken
- Individuals → Beiträge / Nicht zugeordnet
- bestehende Medienbeziehungen
- Erschließungsgrad auf echte Werke/Beiträge umstellen

## Noch nicht endgültig entschieden

Vor den jeweiligen Umsetzungsblöcken konkret festlegen:
- genaue Printausgaben-Identitätsfelder
- technische Darstellung/Sortierung von Seitenbereichen
- Umgang mit Werk ohne bekanntes Shooting
- technische Ablage der permanenten Beitragsnummer
- Sortierreihenfolge gleichartiger Beiträge
- genaue Behandlung bestehender 3e-Werknummern nach Einführung der Beitragsnummer
- vereinfachte Einbindung von Medien ohne `release` in `Nicht zugeordnet`

Die technische ID des konkreten Beitrags bleibt `release`.

## Gerätetests

Erfolgreich dokumentiert:
- Schema-6-Grundlage
- Erschließungsgrad
- Etappe 1
- Etappe 2a–2d und Galerie-/Video-Grundfunktionen
- Etappe 3a–3g
- Etappe 4a

Etappe 4a konkret:
- echte Rubrik zeigt Beiträge statt Shootings
- Beitrag zeigt Galerie-/Video-Anzahl
- Galerie/Video direkt erreichbar
- Beitrag bearbeitbar
- `Nicht zugeordnet` weiterhin funktionsfähig
- Titel, Printausgaben, Galerien, Videos und Shootings weiterhin erreichbar
- keine Migration
- Schema bleibt `6`

Noch nicht separat dokumentiert:
- Research-Löschschutz

Alle kommenden Umbau-Schritte einzeln als App-Update testen. App nicht deinstallieren.

## Zuletzt abgeschlossener Arbeitsblock

**Etappe 4a -- Rubriken verständlich machen**

Stabiler und gerätetesteter Code:
- Commit `c7b69be184b04111d275688d92701061c4b599ed`
- `www/index.html` Blob `d6adefa24efbbf9b9f12b0d7871bbc318e0b68da`

Ergebnis:
- Rubrik bleibt Veröffentlichungskontext.
- Darunter stehen Beiträge.
- Galerie/Video hängen am Beitrag.
- Shooting bleibt Entstehung und wird in dieser Hierarchie nicht als Zwischenebene gezeigt.
- Sichtbarer Begriff auf dieser Ebene: `Beitrag`.
- `Nicht zugeordnet` bleibt Arbeitsbestand innerhalb Rubriken.
- Der Benutzer muss den technischen Begriff `release` nicht verstehen.
- Bestehende Funktionen blieben erhalten.
- Keine Migration.
- Kein Schema-Bump.

## Nächster sinnvoller Schritt

**Etappe 4b -- Beitragsnummerierung und generische Bezeichnung**

Vor Code:
1. Aktuelles `STATUS.md` vollständig lesen.
2. Referenz-Commit mit `main` vergleichen.
3. Aktuelle `www/index.html` aus `main` lesen.
4. Bestehende Funktionen für `releaseDisplayTitle`, Modellableitung und 3e-Werknummerierung gezielt prüfen.

Dann als kleiner separater Schritt:
- Beitragsnummer fachlich definieren und technisch so speichern/ableiten, dass sie stabil bleibt.
- Originalname weiterhin bevorzugen.
- Ohne Originalname generische Anzeige erzeugen, z. B.:
  - `Busty Babes • Tiffany Ryan • Nr. 1`
  - `Cyber Girl of the Year • Breann McGregor • Nr. 1`
- Galerie/Video bleiben Medien desselben Beitrags und werden nicht als getrennte Veröffentlichungsidentitäten behandelt.
- Keine Legacy-Migration und kein Schema-Bump, sofern die Analyse nicht ausdrücklich zeigt, dass dies unvermeidbar ist.
