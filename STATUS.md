# Playboy Archiv -- Projektstatus

Stand: 2026-09-12  
Referenz-Commit: `be0ae5fe61ca12a8cb53e4f578a2bbe5de993d1f`

> Verbindliche Übergabedatei. Vor neuer Arbeit `AGENTS.md` vollständig lesen und prüfen, ob `main` seit dem Referenz-Commit weitergelaufen ist.

## Verbindlicher Arbeitsablauf

- Vor jeder inhaltlichen oder technischen Änderung `STATUS.md` vollständig lesen.
- Referenz-Commit mit aktuellem `main` vergleichen.
- Wenn `main` weitergelaufen ist, Änderungen zuerst rekonstruieren.
- Vor jeder Bearbeitung die aktuelle betroffene Datei aus `main` lesen.
- Nur auf Basis dieser Version arbeiten.
- Änderungen klein und gezielt halten.
- Fertige Dateien als vollständige Ersatzdateien mit exakt dem Repository-Dateinamen bereitstellen.
- Benutzer ersetzt die Datei selbst in GitHub.
- Nach Upload und vor weiterer Änderung `main` erneut prüfen.
- Android-APKs immer als UPDATE installieren; App nicht deinstallieren.
- Vor jedem Gerätetest zuerst erklären:
  1. Was soll fachlich passieren?
  2. Was wurde geändert?
  3. Wo ist es sichtbar?
  4. Woran erkennt man das korrekte Verhalten?

## Aktueller stabiler Code-Stand

- App-Paket: `de.playboy.archiv`
- Daten-Schema-Version: `6`
- Fachlich berücksichtigter und auf dem Gerät getesteter Code-Stand: Commit `be0ae5fe61ca12a8cb53e4f578a2bbe5de993d1f`
- `www/index.html` auf diesem Stand: Blob `431ce5b95ef5b1817a131f97b6b24bd8f88774b9`
- Etappe 4c inklusive Folgekorrektur ist funktional auf dem Gerät getestet.
- Keine automatische Legacy-Migration.
- Kein Schema-Bump.

## Aktuell wichtigster Hinweis zur Testqualität

Die Funktionalität der aktuellen Rubrik-/Beitragslogik wurde bestätigt. Die Bedienbarkeit ist jedoch noch nicht verlässlich bewertet.

Grund:
- Der aktuelle Datenbestand besteht überwiegend aus beliebigen Testobjekten.
- Beziehungen und Objekte sind dadurch für den Benutzer nicht eindeutig wiedererkennbar.
- Bei Auswahlfeldern wird teilweise eher blind gewählt.
- Deshalb können Logik, Übersichtlichkeit und Bedienfluss nur eingeschränkt sinnvoll beurteilt werden.

Konsequenz:
- Vor weiteren größeren Bedienumbauten soll zunächst ein kleines, verständliches Referenzszenario mit eindeutig identifizierbaren Models, Shootings, Rubriken, Beiträgen, Galerien und Videos verwendet werden.
- Keine künstlichen Produktivdaten dauerhaft etablieren.
- Ziel ist eine kontrollierte Testbasis, nicht eine neue Datenmigration.

## Stabile Funktionen

- Archiv-Restore über SAF.
- Permanente Android-Signierung und Update-Installation ohne Deinstallation.
- Fotoanzeige mit Pinch-Zoom.
- Video-Wiedergabe; nativer WebView-Fullscreen bleibt deaktiviert.
- Models, Titel, Rubriken, Individuals, Shootings, Galerien, Videos, Beiträge (`release`) und Medienverwaltung.
- Archivfilter, freie Suche, Bewertungs- und Profilbildfilter.
- Research mit Status, strukturierten Belegen und kontrollierter Übernahme.
- `careerFacts`, `bioFacts`, `archiveFacts`.
- Korrektur-/Rückzugslogik für bestätigte Fakten.
- Bestätigte Fakten im Model-Profil.
- Bio-Entwurfsgenerator.
- Research-Import.
- Erschließungsgrad.
- Research-Löschschutz ist implementiert; separater Gerätetest weiterhin nicht dokumentiert.

## Fachliches Soll-Modell

### Grundsatz

- `Shooting = Entstehung`
- `Galerie/Video = Werk bzw. Medium`
- `Rubrik/Titel/Printausgabe = Veröffentlichungskontexte`
- `release = einzelner konkreter Beitrag innerhalb eines Veröffentlichungskontexts`
- `Seiten = konkrete Fundstelle innerhalb einer Printausgabe`

`release` bleibt technisch wichtig zur Bündelung zusammengehöriger Galerie/Video, soll aber kein eigener konkurrierender Hauptbereich für den Benutzer sein.

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

Ähnliche technische Bedienmuster dürfen intern wiederverwendet werden, ohne diese Bereiche sichtbar zusammenzuführen.

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
- optionaler Beitrag (`release`)

Geerbt aus Shooting:
- Models
- Fotograf(en)
- Datum
- Ort

Bei Bedarf:
- einzelne Models/Fotograf(en) ausschließen

### Video

Fachlich parallel zur Galerie.

Rolle:
- Video-Werk/Container
- Videodatei(en) direkt zugeordnet

Beziehungen:
- optionales Shooting
- optionaler Beitrag (`release`)

Geerbte Daten und Ausschlüsse analog Galerie.

### Konkreter Beitrag (`release`)

Der Beitrag ist der kleinste fachlich sinnvolle Veröffentlichungszusammenhang.

Regeln:
- Beitrag wird innerhalb seines Veröffentlichungskontexts angelegt, nicht als eigener sichtbarer Haupttyp.
- Galerie und Video sind Medien dieses Beitrags.
- Shooting ist Entstehung, nicht Veröffentlichung.
- Ein Beitrag kann Rubrik, Titel oder beides zugeordnet sein.
- Ein individueller Originalname gehört zum Beitrag, nicht zu Galerie oder Video.
- In einer Rubrik wird die Rubrik im Beitragslabel nicht wiederholt.
- Das Model wird auf der Beitragskarte immer sichtbar geführt.
- Ohne individuellen Namen ist die sichtbare Bezeichnung Model + ggf. Nummer.
- Mit individuellem Namen ist die sichtbare Bezeichnung Model + Originalname.

Beispiele innerhalb einer bereits geöffneten Rubrik:
- `Tiffany Ryan`
- `Tiffany Ryan • Nr. 1`
- `Tiffany Ryan • Nr. 2`
- `Tahlia Paris • High Time`
- `Tahlia Paris • Hot Sands`

### Modelableitung am Beitrag

Seit Etappe 4c-Folgekorrektur gilt:
- Der übergeordnete Model-Filter ist nur Filter, nicht Quelle der fachlichen Zuordnung.
- Models eines Beitrags werden aus den zugeordneten Galerie-/Video-Werken und deren Shootings abgeleitet.
- Der Benutzer muss für `Beitrag hinzufügen` kein Model im übergeordneten Filter auswählen.
- Solange noch kein Werk/Shooting zugeordnet ist, kann noch kein fachlich belastbares Model angezeigt werden.
- Mehrere Models werden als gemeinsame Model-Kombination betrachtet.

### Beitragsnummerierung

Aktueller stabiler sichtbarer Stand:
- Beitragsnummer identifiziert mehrere Beiträge derselben Rubrik mit exakt derselben Model-Kombination.
- Ein einzelner solcher Beitrag zeigt keine `Nr. 1`.
- Erst ab mindestens zwei Beiträgen derselben Rubrik und derselben Model-Kombination werden Nummern sichtbar.
- Dann erscheinen `Nr. 1`, `Nr. 2`, ...
- Eine andere Model-Kombination bildet eine eigene Reihe.
- Hat ein Beitrag einen individuellen Namen, wird dieser sichtbar bevorzugt; die Nummer wird dort nicht zusätzlich angezeigt.
- Interne Nummer kann stabil gespeichert bleiben, auch wenn sie nicht sichtbar ist.
- Galerie und Video erhalten dadurch keine eigene Veröffentlichungsidentität.

Die ältere 3e-Werknummerierung für Galerie/Video bleibt technisch bestehen, ist aber nicht das endgültige sichtbare Identitätsmodell.

## Rubriken -- aktueller stabiler Stand

Sichtbarer Begriff: **Rubriken**.

Hierarchie:
- Rubrik
- Beitrag
- Galerie / Video

Seit Etappe 4c:
- sichtbarer Haupttab `Veröffentlichungen` ist entfernt.
- `release` ist im normalen Hinzufügen-Dialog nicht mehr als auswählbarer Typ sichtbar.
- Eine echte Rubrik bietet `＋ Beitrag hinzufügen`.
- Der Beitrag wird direkt innerhalb der Rubrik erzeugt; intern entsteht weiterhin ein `release`.
- Rubrik muss im Beitragsdialog nicht noch einmal ausgewählt werden.
- Beitragskarten haben links eine 2:3-Vorschau.
- Galerie-Vorschau hat Vorrang; bevorzugt wird ein geeignetes Hochformatfoto.
- Rechts steht das Model fett und horizontal zentriert.
- Darunter steht entweder der individuelle Name oder die nur bei Bedarf sichtbare Beitragsnummer.
- Galerie-/Video-Zugriffe bleiben direkt am Beitrag erreichbar.
- `Beitrag bearbeiten` bleibt verfügbar.

### Nicht zugeordnet

`Nicht zugeordnet` bleibt innerhalb **Rubriken**, nicht als eigener Haupttab.

Zweck:
- Arbeitsbestand für Beiträge/Medien, deren korrekter Veröffentlichungskontext noch nicht eindeutig geklärt ist.
- kein historischer Rubrikname
- kein künstliches `series`-Objekt

Aktuell getestet:
- `release` ohne Rubrik erscheint dort.
- Zuordnung zu echter Rubrik entfernt den Beitrag aus der Liste.

Noch offen:
- Medien ohne eindeutigen `release` so einbinden, dass der Benutzer keine technischen Zwischenzustände verstehen muss.

## Titel

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

## Printausgaben

Printausgabe = konkreter physischer/digitaler Veröffentlichungskontext.

Eigene Angaben:
- Ausgabe/Identität
- Datum
- Cover-Vorschau
- optional PDF
- `im Bestand`

Regeln:
- PDF und physischer Bestand sind unabhängig.
- Ausgabe kann dokumentiert werden, auch wenn weder PDF noch physisches Exemplar vorhanden ist.
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

## Reale Referenzfälle

### Tiffany Ryan

Bekannter Bestand:
- `Busty Babes`: 3 Shootings, 2 Galerien, 1 Video
- `Women of Playboy`: 3 Shootings, 2 Galerien, 1 Video
- 4 benannte Pictorials/Galerien bereits im Archiv
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

Individuelle Namen u. a.:
- `High Time`
- `Hot Sands`

Die beobachtete 1:1-Struktur darf nicht als globale Regel erzwungen werden.

## Bereits abgeschlossene Umbau-Etappen

### Etappe 1
- sichtbares `Fotogalerie` → `Galerie`
- sichtbares `Serien` → `Rubriken`
- getestet

### Etappe 2
- Shooting auf Datum, Ort, Models und Fotograf(en) ausgerichtet
- Galerie/Video mit optionalem Shooting
- Vererbung von Models/Fotograf(en)/Datum/Ort
- Ausschlüsse
- direkte Medienzuordnung
- getestet

### Etappe 3a–3g
- technischer Typ `release`
- neutraler Fall `Nicht zugeordnet`
- Galerie/Video optional einem `release` zugeordnet
- individueller Veröffentlichungsname
- interne Werknummern
- galeriegeführte Vorschau
- `Nicht zugeordnet` innerhalb Rubriken
- getestet

### Etappe 4a
- Rubriken zeigen Beiträge statt Shootings als nächste Ebene
- sichtbarer Begriff `Beitrag`
- Galerie/Video direkt am Beitrag
- getestet

### Etappe 4c
- sichtbarer Hauptbereich `Veröffentlichungen` entfernt
- Beitrag wird direkt innerhalb einer Rubrik angelegt
- `release` läuft als technische Hintergrundstruktur weiter
- 2:3-Vorschau links
- Model fett und zentriert rechts
- individueller Name bzw. Nummer darunter
- Galerie-/Video-Zugriffe erhalten
- getestet

### Etappe 4c -- Folgekorrektur
- Modelauswahl im übergeordneten Filter ist keine Voraussetzung mehr für `Beitrag hinzufügen`
- Models werden aus Galerie/Video → Shooting abgeleitet
- Nummer wird erst sichtbar, wenn mindestens zwei Beiträge derselben Rubrik mit derselben Model-Kombination existieren
- individueller Name hat sichtbar Vorrang
- andere Model-Kombination = eigene Nummernreihe
- getestet
- Commit `be0ae5fe61ca12a8cb53e4f578a2bbe5de993d1f`
- `www/index.html` Blob `431ce5b95ef5b1817a131f97b6b24bd8f88774b9`

## Historischer Hinweis zum verworfenen 4b-Zwischenstand

Zwischenstand `b397e99dbd391e62f2e4b29dc514bb87084ea743` war fachlich nicht endgültig:
- Benutzer musste weiterhin technisch eine Veröffentlichung anlegen.
- Das widersprach dem Ziel, `release` in den Hintergrund zu verlagern.

Nicht wieder einführen:
- separates sichtbares Anlegen einer `Veröffentlichung`
- sichtbarer Haupttab `Veröffentlichungen`
- Modelidentität eines Beitrags aus dem übergeordneten Filter ableiten
- sichtbare `Nr. 1` bei nur einem Beitrag derselben Rubrik/Model-Kombination

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

## Schema 6 -- wichtige Regeln

Bereits umgesetzt:
- `shoot.modelIds`
- `shoot.credits`
- `shoot.location`
- Werktypen `gallery` und `video`
- technischer Beitragstyp `release`
- `modelsMode = inherit | custom`
- `creditsMode = inherit | custom`
- `publication.owned`
- `publication.hasPdf`

Nicht tun:
- `DATA_SCHEMA_VERSION` zurücksetzen
- automatische Erzeugung von `gallery`/`video`/`release` aus Legacy-Daten
- automatische Änderung bestehender Shooting-/Medienbeziehungen
- künstliche Daten erzeugen

## Nicht wieder einführen

- kein nativer Video-Fullscreen
- kein globaler Ein-Finger-`touchmove` mit `preventDefault()`
- kein `main`-Scrollcontainer mit `height:100vh`
- keine künstlichen Shootings/Medien
- keine dauerhafte Gleichsetzung Shooting mit Galerie
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

## Gerätetests

Erfolgreich dokumentiert:
- Schema-6-Grundlage
- Erschließungsgrad
- Etappe 1
- Etappe 2a–2d und Galerie-/Video-Grundfunktionen
- Etappe 3a–3g
- Etappe 4a
- Etappe 4c
- Etappe 4c-Folgekorrektur

Aktuell konkret bestätigt:
- kein sichtbarer Haupttab `Veröffentlichungen`
- Beitrag direkt innerhalb Rubrik anlegbar
- Model-Filter nicht mehr Voraussetzung
- Modelableitung aus Shooting über zugeordnete Werke
- einzelne Rubrik/Model-Kombination ohne sichtbare `Nr. 1`
- Nummerierung ab mehreren gleichartigen Beiträgen
- individueller Name sichtbar statt Nummer
- 2:3-Beitragsvorschau links
- Galerie-/Video-Zugriff funktioniert

Einschränkung:
- Bedienbarkeit/Übersichtlichkeit noch nicht belastbar getestet, weil aktuelle Testobjekte kaum eindeutig identifizierbar sind.

Noch nicht separat dokumentiert:
- Research-Löschschutz

## Nächster sinnvoller Schritt

**Keine direkte Erweiterung auf Titel/Printausgaben, bevor die Bedienlogik mit einer verständlichen Testbasis überprüft wurde.**

Als nächster Arbeitsblock:

### Etappe 4c.1 -- kontrollierte Referenz-Testbasis

Ziel:
- wenige eindeutig erkennbare Objekte verwenden
- Beziehungen bewusst und nachvollziehbar aufbauen
- Bedienfluss nicht mehr mit anonymen Zufallsobjekten beurteilen

Empfohlenes kleines Szenario:
- 2 klar benannte Models
- 1 Rubrik mit zwei Beiträgen derselben Model-Kombination, damit Nummerierung sichtbar geprüft werden kann
- 1 Rubrik mit nur einem Beitrag, damit fehlende `Nr. 1` geprüft werden kann
- 1 Beitrag mit individuellem Namen (`High Time`-Muster)
- je klar benannte/erkennbare Shootings
- je eine Galerie mit eindeutigem Vorschaubild
- mindestens ein Video
- optional eine zweite Model-Kombination zur Prüfung einer unabhängigen Nummernreihe

Wichtig:
- Dies ist ein Testszenario, keine automatische Datenmigration.
- Keine künstlichen Beziehungen in echte Archivdaten übernehmen.

Erst wenn dieser Referenzfluss verständlich bedienbar ist:
- denselben Beitrag-im-Kontext-Ansatz auf **Titel** übertragen.
- danach **Printausgaben**.

## Noch offen

- Titelhierarchien und Beitragserstellung innerhalb Titel
- Printausgaben inkl. Cover/PDF/Bestand/Seiten
- vereinfachte Darstellung von Medien ohne `release` in `Nicht zugeordnet`
- genaue Behandlung bestehender 3e-Werknummern langfristig
- Legacy-Migration
- Erschließungsgrad auf endgültige Werk-/Beitragsstruktur umstellen
- Bedienbarkeit mit realistisch identifizierbaren Referenzdaten prüfen
