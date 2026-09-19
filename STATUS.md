# Playboy Archiv -- Projektstatus

Stand: 2026-09-19
Referenz-Commit: `ff7fda772782d6c863b63de51060716f91413657`

> Verbindliche Übergabedatei. Vor neuer Arbeit `AGENTS.md` vollständig lesen und prüfen, ob `main` seit dem Referenz-Commit weitergelaufen ist.

## Verbindlicher Arbeitsablauf

- Vor jeder Änderung `STATUS.md` vollständig lesen, Referenz-Commit mit `main` vergleichen und Änderungen seitdem rekonstruieren.
- Vor Bearbeitung aktuelle Datei aus `main` lesen; nur darauf arbeiten; Änderung klein halten.
- Vollständige Ersatzdatei mit exaktem Repository-Dateinamen liefern.
- Benutzer lädt selbst hoch; danach `main` erneut prüfen.
- Android-APK immer als UPDATE installieren, niemals deinstallieren.
- Vor Gerätetests fachliches Ziel, Änderung, sichtbare Stelle und Erfolgskriterium erklären.

## Aktueller stabiler Code-Stand

- App-Paket: `de.playboy.archiv`
- Schema: `6`
- Aktuell berücksichtigter Code: `ff7fda772782d6c863b63de51060716f91413657`
- Titelkopf, Hinzufügen-Button, echte Titel-Beitragskarte und der separate Legacy-Shootingbereich sind gerätetest-bestätigt.
- Etappe 4c samt Folgekorrekturen getestet.
- Etappe 5a inklusive 5a.1 und 5a.2 gerätetest-bestätigt.
- Etappe 6a Print-Grundstruktur inklusive Reihen, Ausgaben, Cover/PDF und Löschschutz gerätetest-bestätigt.
- Print/Reihen-Feinschliff einschließlich einheitlicher Aktionsbuttons gerätetest-bestätigt.
- Rubriken-Übertragung auf die Print-Designsprache einschließlich anschließendem Feinschliff gerätetest-bestätigt.
- Medienkarten, Shootingkarten, der visuelle Zuordnungswähler `Shooting | Beitrag`, die indirekte Shooting-Medienauflösung, der bereinigte Importworkflow sowie der neue Beitrags-Zuordnungsmodus sind gerätetest-bestätigt.
- Keine automatische Legacy-Migration; kein Schema-Bump.

## Fachliches Soll-Modell

- `Shooting = Entstehung`
- `Galerie/Video = Werk bzw. Medium`
- `Rubrik/Titel/Printausgabe = Veröffentlichungskontexte`
- `release = einzelner konkreter Beitrag innerhalb eines Veröffentlichungskontexts`
- `Seiten = konkrete Fundstelle innerhalb einer Printausgabe`

`release` bleibt technisch wichtig, ist aber kein sichtbarer konkurrierender Hauptbereich. Rubrik, Titel und Print bleiben getrennte Verwaltungs- und Übersichtsbereiche.

### Archivbereiche

Übergeordnete Archivbereiche: Playboy, Special Editions, Cyber Club, Playboy Plus. Der Archivbereich ist ein übergeordneter Filter/Kontext und wird bewusst zugeordnet. Keine Bereichszuordnung allein anhand Datum oder Titelklasse.

Bestätigte fachliche Beispiele:
- Special Editions: Titel `SE Model`; Rubriken/Reihen wie `Girlfriends`, `Lingerie` etc.
- Cyber Club: Titel `Coed`, `Cyber Girl`; Rubriken wie `Busty Babes`, `Women of Playboy` etc.
- Playboy Plus: Titel `Cyber Girl`; Rubriken wie `Amateur`, `Cyber Girl` etc.
- Playboy: Titel `Playmate`; reguläre Ausgaben.

In der Verwaltung steht der Model-Filter über dem Bereichsfilter. Der Archivbereich wird über ein Auswahlfeld gewählt; `Alle Bereiche` und `Ohne Bereich` sind fachlich getrennt. Bereichsfilterung ist für Titel, Rubriken und Print funktionsgeprüft.

## Shooting

Entstehungskontext mit Datum/Ort sowie Model(s)/Fotograf(en). Kein fachlicher Shootingtitel und keine künstlichen Platzhalter. Identifikation über Model bzw. exakte Model-Kombination + Shootingnummer, z. B. `April Katherine • Shooting • Nr. 1`. Nummernreihe pro exakter Model-Kombination; Datum/Ort nur Zusatz. Gerätetest bestätigt.

Aktueller gerätetest-bestätigter Verwaltungsstand:
- zweistufige Darstellung wie in den anderen Bereichen: feste Überschrift `Shootings`, darunter auberginefarbener Kopf `Shootings`;
- Karten in der gemeinsamen sehr hellen Kartenfarbe mit einzelner 2:3-Vorschau;
- `Noch kein Medium` ist in der leeren Vorschau zentriert;
- rechts stehen Shootingbezeichnung, Models, Foto-/Videoanzahl sowie die kompakten Aktionen `Bearbeiten` und `Löschen`;
- bei genau einem Shooting derselben exakten Modelkombination erscheint `Shooting`, ab mehreren `Shooting • Nr. 1`, `Shooting • Nr. 2`, ...;
- Shootings bleiben eigenständige Entstehungskontexte ohne direkte Archivbereichszuordnung;
- neue direkte Zuordnungen `Shooting → Rubrik/Titel` wurden einschließlich der dadurch funktionslosen Mehrfachauswahl entfernt;
- vorhandene Legacy-Verknüpfungen zu Rubriken/Titeln bleiben lesbar und können weiterhin gelöst werden; keine Migration oder Löschung.
- Shootingvorschau und Medienanzahl berücksichtigen sowohl direkt verknüpfte Legacy-Dateien als auch Fotos/Videos aus den zugeordneten Galerie-/Videoobjekten;
- identische Dateien werden dabei mit der bestehenden Medienidentität nur einmal gezählt;
- die korrekte Anzeige für neue Testdaten mit `Datei → Galerie/Video → Shooting` ist auf dem Android-Gerät bestätigt.
- Im Verwaltungsbereich kann zwischen `Alle` und `Ohne Medien` gefiltert werden; `Ohne Medien` zeigt ausschließlich Shootings, deren berechnete Medienanzahl `0 Fotos · 0 Videos` beträgt.
- Der Filter berücksichtigt damit direkte Legacy-Medien ebenso wie indirekte Medien aus Galerie/Video, ist mit Suche und Model-Filter kombinierbar und verändert keine Zuordnungen. Gerätetest bestätigt.

## Galerie / Video

Galerie = Foto-Werk/Container; Video fachlich parallel. Beide optional mit Shooting und Beitrag (`release`). Models, Fotograf(en), Datum und Ort werden aus Shooting geerbt; Ausschlüsse möglich.

Zielmodell Medien:
- Foto → Galerie → Shooting
- Videodatei → Video → Shooting

Der neue Schreibweg folgt ausschließlich `Datei → Galerie/Video → Shooting`. Die direkte Importaktion `Datei → Shooting` ist entfernt. Vorhandene direkte Legacy-Verknüpfungen bleiben lesbar und können gezielt gelöst werden. Visuelle Galerie-/Video-Zielkarten sind getestet.

Aktueller gerätetest-bestätigter Verwaltungsstand in Commit `fb9ff5a5370f426842930a8f33de0cec1de88945`:
- `Galerien` und `Videos` sind unter dem gemeinsamen Haupttab `Medien` zusammengefasst;
- der Bereich besitzt oben die feste Überschrift `Medien` mit Suche und passender Hinzufügen-Aktion;
- darunter folgt wie bei Rubriken ein eigener auberginefarbener Auswahlkopf, über den zwischen `Galerien` und `Videos` gewechselt wird;
- `Import` bleibt als eigenständiger Haupttab mit seinem bisherigen Workflow und den Foto-/Video-/PDF-Filtern erhalten;
- Galerie- und Videokarten verwenden eine einzelne 2:3-Vorschau in der gemeinsamen sehr hellen Kartenfarbe;
- die redundante sichtbare Kartenbezeichnung `Galerie` beziehungsweise `Video` rechts neben der Vorschau wurde entfernt;
- rechts neben der Vorschau stehen nur die aus den zugeordneten Shootings abgeleitete Modelbezeichnung, die Medienanzahl sowie `Bearbeiten` und `Löschen`;
- existiert pro Medientyp nur eine Galerie beziehungsweise ein Video derselben exakten Model-Kombination, bleibt die Bezeichnung ohne Nummer; ab zwei Einträgen derselben Kombination werden sichtbar `Nr. 1`, `Nr. 2`, ... ergänzt;
- Beitrag, Shooting, Datum, Ort, Fotografen und Notiz bleiben gespeichert und über `Bearbeiten` erreichbar, werden auf der kompakten Karte aber nicht mehr angezeigt;
- beim Bearbeiten einer Galerie oder eines Videos steht ein gemeinsamer Umschalter `Shooting | Beitrag` zur Verfügung;
- der Shooting-Wähler verwendet wie der Beitragswähler visuelle Auswahlkarten mit 2:3-Vorschau, Modelkombination, Shootingbezeichnung, Datum/Ort und Medienanzahl;
- der Umschalter wechselt nur die sichtbare Zuordnungsansicht: Shooting- und Beitragsbeziehung bleiben unabhängig voneinander gleichzeitig speicherbar;
- `Shooting noch nicht bekannt` und `Noch keinem Beitrag zugeordnet` bleiben als bewusste neutrale Optionen erhalten;
- Model- und Fotografenvererbung aus dem gewählten Shooting funktioniert unverändert;
- bestehende Galerie- und Videozuordnungen wurden jeweils mit dem visuellen Umschalter geprüft; Shooting- und Beitragskarte zeigen die korrekten Beziehungen;
- `Shooting zuordnen` wurde aus dem Import entfernt; der zugehörige Dialog und direkte Schreibcode sind nicht mehr vorhanden;
- die Importtabs und `Alle auswählen` verwenden dieselbe Statuslogik und erkennen Zuordnungen zu Galerie, Video, Printausgabe sowie direkte Legacy-Shootingbeziehungen;
- im Shootingdetail erscheint `Direkte Zuordnung lösen` ausschließlich bei einer tatsächlichen direkten Legacy-Datei↔Shooting-Beziehung; indirekte Dateien aus Galerie/Video erhalten keine wirkungslose Löseaktion;
- zweistufige Überschrift, Umschalter, Suche, Hinzufügen-Aktionen, Medienzugriff und Importworkflow sind gerätetest-bestätigt;
- der gesamte aktuelle Galerie-/Video-Kartenfeinschliff ist auf dem Android-Gerät bestätigt.

## Beitrag (`release`)

Kleinster fachlich sinnvoller Veröffentlichungszusammenhang. Er wird innerhalb des Veröffentlichungskontexts angelegt, nicht als Haupttyp. Galerie/Video sind Medien des Beitrags; Shooting ist Entstehung. Beitrag kann Rubrik, Titel und künftig Printausgabe referenzieren. Individueller Originalname gehört zum Beitrag. Models werden aus Galerie/Video → Shooting abgeleitet; der Model-Filter ist keine fachliche Quelle.

Beitragsnummerierung:
- einzelner Beitrag derselben Rubrik/Model-Kombination: keine `Nr. 1`;
- ab mindestens zwei Beiträgen derselben Rubrik und exakt derselben Model-Kombination: `Nr. 1`, `Nr. 2`, ...;
- andere Model-Kombination = eigene Reihe;
- individueller Name wird sichtbar bevorzugt;
- interne Nummer darf stabil gespeichert bleiben;
- manuelle Eingabe der Beitragsnummer ist aus Anlegen/Bearbeiten entfernt; die sichtbare Nummerierung wird automatisch aus dem Beitragskontext bestimmt.

Gerätetest-bestätigter Beitragseditor:
- der Veröffentlichungskontext wird bewusst als `Keine Zuordnung`, `Rubrik`, `Titel` oder `Rubrik und Titel` gewählt;
- Standardfall ist genau ein Kontext: Rubrik oder Titel;
- nur der zur Auswahl passende Zuordnungsbereich wird angezeigt;
- `Rubrik und Titel` bleibt als bewusster Ausnahmefall möglich und zeigt einen deutlichen Warnhinweis;
- vorhandene Doppelzuordnungen werden beim Öffnen erkannt und automatisch im Ausnahmefall `Rubrik und Titel` angezeigt;
- beim Speichern verlangt der Doppelmodus beide Werte; eine bestehende Beziehung wird nicht stillschweigend beim Öffnen gelöscht.

## Rubriken -- gerätetest-bestätigter Feinschliff

Hierarchie: **Rubrik → Beitrag → Galerie/Video**.

Echte Rubrik bietet `＋ Beitrag hinzufügen`; intern entsteht `release`. Beiträge können von Rubriken gelöst werden, ohne Beitrag/Medien zu löschen. `Nicht zugeordnet` zeigt Beiträge ohne Rubrik sowie Galerien/Videos ohne Beitrag und folgt dem gewählten Archivbereich.

Aktuell bestätigte Rubrik-Darstellung:
- kompakter auberginefarbener Rubrikkopf;
- Rubriktitel horizontal in der verfügbaren Mittelzone zentriert;
- Kopfaktionen stehen ruhig auf derselben Höhe;
- gemeinsame kompakte Aktionsbuttons sind explizit weiß, fein umrandet und schattenfrei;
- innere Beitragskarten sehr hell aubergine;
- 2:3-Vorschau mit Rundungen an allen vier Ecken;
- Modelname auf Beitragskarten bewusst ruhiger (`14px`, weniger fett), damit Mehrfach-Models nicht dominieren;
- Medienanzahl bleibt sichtbar;
- direkte Galerie-/Video-Sprungbuttons wurden aus der Rubrik-Beitragskarte entfernt; die Beziehungen selbst bleiben unverändert;
- `Bearbeiten` und `Lösen` bleiben die fachlichen Kartenaktionen.
- Der reine Erklärungstext oberhalb von `＋ Beitrag hinzufügen` wurde entfernt; der Button beginnt direkt im Rubriken-Inhaltsbereich.

Der gesamte Feinschliff wurde auf dem Android-Gerät bestätigt.

## Titel -- Etappe 5a funktional stabil, Beitragskarten gerätetest-bestätigt

Hierarchie: **Titel → Beitrag → Galerie/Video**.

Vorhandene Titel-Tabs bleiben fachlich bestätigt: erste Ebene z. B. `Coed`, `Cyber Girl`, `Playmate`, `SE Model`; zweite Ebene z. B. `of the Week`, `of the Month`, `of the Year`.

Titelüberschrift z. B. `Cyber Girl of the Week` / `3. Woche Juni 2001`; kleiner Pokal links, Modelname nicht in der Überschrift. `＋ Beitrag hinzufügen` steht oberhalb der Beiträge. Beiträge zeigen Modelableitung aus Galerie/Video → Shooting, individuellen Namen/Nummer sowie die Medienanzahl. Direkte Galerie-/Video-Sprungbuttons werden wie bei Rubriken nicht angezeigt. `Lösen` entfernt nur Beitrag ↔ Titel. Gerätetest bestätigt.

Bei Galerie/Video → Beitrag wird zusätzlich der Titelkontext angezeigt. Alte direkte Titel→Shooting-Verknüpfungen bleiben unter `Bisher direkt zugeordnete Shootings` sichtbar und werden nicht automatisch migriert.

Aktueller visueller Stand in Commit `8e627b13187d9f317634b2b7e6178883496f7ce0`:
- der kompakte auberginefarbene Titelkopf mit mittiger Titel-/Zeitraumdarstellung sowie `Bearbeiten` und quadratischem `×` ist gerätetest-bestätigt;
- `＋ Beitrag hinzufügen` entspricht nun optisch dem bestätigten Rubriken-Button und ist gerätetest-bestätigt;
- die echte Titel-Beitragskarte entspricht optisch der bestätigten Rubrik-Beitragskarte und ist auf dem Android-Gerät bestätigt;
- Medienvorschau und Medienanzahl bleiben sichtbar; die überflüssigen direkten Galerie-/Video-Sprungbuttons wurden wie bei Rubriken entfernt, ohne Beziehungen oder Medien zu verändern;
- `Bearbeiten` und `Lösen` bleiben die fachlichen Kartenaktionen;
- der sichtbare Bereich `Bisher direkt zugeordnete Shootings` verwendet nun dieselbe 2:3-Vorschau, helle Kartenfarbe, Shooting-/Modelanzeige, Medienanzahl und kompakte Buttonfamilie wie der Shooting-Haupttab;
- Darstellung, `Bearbeiten` und die Sicherheitsabfrage von `Lösen` sind separat auf dem Android-Gerät bestätigt;
- sämtliche Legacy-Beziehungen blieben unverändert; beim Test wurde die Löseabfrage abgebrochen und keine Beziehung entfernt.
- Titel-Fachlogik, Beziehungen und Schema wurden nicht verändert.

## Titelhierarchien -- vorerst fachlich geklärt

Für den aktuellen Ausbau ist keine zusätzliche gespeicherte Week→Month→Year-Verknüpfung nötig. Die vorhandenen Felder für Titelklasse, Ebene und konkreten Zeitraum reichen zunächst aus.

Bekannte Hierarchien:
- Coed: `of the Week → of the Month`
- Cyber Girl: `of the Week → of the Month → of the Year`
- Special Editions Model: `of the Year`

Keine automatische Beziehung nur aus Datum und kein automatisches `Cyber Girl = Cyber Club`. Spätere optionale Funktion: eigene Model-Seite `Titelhistorie`, abgeleitet aus tatsächlich vorhandenen Titeln/Beiträgen.

## Print -- Etappe 6a stabil

Fachliche Grundhierarchie: **Reihe → Ausgabe**.

Referenzfall:
- Archivbereich: `Special Editions`
- Reihe: `Girlfriends`
- Ausgabe: `Nr. 5`
- Zeitraum: `Juli/August 2002`
- Cover-Models: `Candice Michelle + Harmony Guffey`
- physischer Status: `Bestellt`

`Nr. 5` ist die Ausgabennummer innerhalb der Printreihe und nicht die Beitragsnummerierung.

### Reihen

- eigene technische Objekte `printSeries`;
- `＋ Reihe` legt eine Reihe mit bewusst gewähltem Archivbereich an;
- `＋ Ausgabe` innerhalb der Reihe legt eine konkrete Ausgabe an;
- Bereichsfilter zeigt passende Reihen/Ausgaben;
- leere Reihen können gelöscht werden;
- Reihe mit zugeordneten Ausgaben ist gegen Löschen geschützt;
- Legacy-Ausgaben mit altem Reihen-Text bleiben sichtbar/übernehmbar; keine automatische Migration.

### Ausgaben

Technischer Typ bleibt vorerst `publication`. Grunddaten: Reihe, Ausgabennummer, Zeitraum, Archivbereich, Cover-Models, physischer Bestandsstatus (`Nicht vorhanden`, `Bestellt`, `Im Bestand`).

Cover-Models sind Metadaten der Ausgabe und erzeugen keinen Beitrag bzw. keinen Nachweis für einen Inhalt im Heft. Verwaltungsdarstellung: Reihe als übergeordneter Kopf; Ausgabennummer und Zeitraum in einer Zeile. Mehrmonatige Zeiträume werden in der Verwaltung platzsparend angezeigt, z. B. `Jul./Aug. 2002`, ohne die gespeicherten Grunddaten zu verändern.

### Cover und PDF

Cover und PDF werden über die normale Medienverwaltung zugeordnet:
- Foto → `Als Cover zuordnen` → konkrete Ausgabe;
- PDF → `Als PDF zuordnen` → konkrete Ausgabe;
- vorhandenes Cover/PDF kann nach Rückfrage ersetzt werden;
- `Cover lösen` / `PDF lösen` entfernt nur die Zuordnung; Medium bleibt erhalten;
- Löschen des Mediums entfernt die Print-Verknüpfung;
- alte spezielle Cover-/PDF-Speicherung bleibt als Fallback lesbar.

Zuordnen, Ersetzen und Lösen sind gerätetest-bestätigt.

### Print-Detailansicht

Antippen einer Ausgabe öffnet die Detailansicht mit vorhandenen Rubrik-/Printbeziehungen sowie dem neuen Artikelbereich. Der aktuelle Stand ist auf dem Android-Gerät bestätigt:
- die Reihenansicht bleibt ruhig und zeigt ausschließlich die kompakten Ausgaben untereinander;
- Artikel werden nicht mehr direkt unter der Ausgabekarte aufgefächert, sondern ausschließlich in der geöffneten Ausgabedetailansicht;
- die sichtbare Aktion heißt im Printkontext `＋ Artikel hinzufügen`; technisch bleibt das Artikelobjekt vorerst `release`;
- `Bearbeiten` und `Lösen` stehen am Artikel zur Verfügung; `Lösen` entfernt nur Artikel ↔ Ausgabe und erhält Artikel, Galerien und Videos;
- der Artikeleditor zeigt die konkrete Printausgabe;
- saubere Printartikel besitzen keine zusätzliche Rubrik-/Titelwahl mehr, da die Ausgabe ihr eindeutiger Veröffentlichungskontext ist;
- vorhandene ältere Zusatzverknüpfungen werden nicht automatisch gelöscht: Nur in diesem Fall erscheint einmalig ein gekennzeichneter Bereinigungsbereich, über den bewusst `Keine Zuordnung` gewählt werden kann; die Printzuordnung bleibt dabei erhalten;
- Seiten/Fundstelle, ausdrücklich gespeicherte Models und ein deutscher Erklärungstext können beim Anlegen und Bearbeiten erfasst werden und bleiben vollständig vorausgewählt;
- jeder Artikel kann genau einem vorhandenen Shooting zugeordnet werden; angeboten werden nur Shootings, die alle ausdrücklich gewählten Artikel-Models enthalten;
- einzelne Fotos und Galerien werden Artikeln bewusst nicht direkt zugeordnet;
- die feste 2:3-Vorschau sowie Fotograf(en), Ort und Entstehungsdatum werden ausschließlich aus dem zugeordneten Shooting abgeleitet;
- die Artikelkarte zeigt die Seiten separat und darunter die kompakten Shootingdaten, z. B. `📷 Mizuno · Los Angeles` sowie `Shooting • Nr. 1 · 1995 (?)`;
- der deutsche Erklärungstext ist standardmäßig auf vier Zeilen begrenzt und lässt sich durch Antippen vollständig ein- und wieder ausklappen;
- Galerie-/Videozahlen werden im Printartikel nicht angezeigt;
- Printartikel und Printausgaben besitzen keine eigene Recherchemarkierung mehr; der Arbeitsbedarf wird zentral über `Shootings → Ohne Medien` ermittelt;
- vorhandene ältere Artikel-Metadaten für Fotograf, Ort und Entstehungsjahr werden nicht automatisch in Shootings übertragen; automatische Mehrfachnutzung oder Datenüberschreibung findet nicht statt;
- beim erneuten Speichern eines Artikels werden noch vorhandene direkte Foto-/Galeriezuordnungen am Artikel entfernt, die Mediendateien selbst bleiben erhalten;
- Cover/PDF, Reihenlogik, Bestand und vorhandene Beziehungen blieben unverändert;
- kein Schema-Bump und keine automatische Migration.

Noch offen bleibt die eigene Erfassung von Cover-Fotograf(en) an der Ausgabe.

### Print-Design -- vollständig gerätetest-bestätigt

Der Print/Reihen-Bereich bleibt die bestätigte Referenz für Ausgabekarten:
- Ausgabe als innere Karte in sehr hellem Aubergine;
- 2:3-Cover links mit Rundungen an allen vier Ecken;
- Ausgabennummer links und Zeitraum rechts in einer Zeile;
- `＋ Ausgabe`, `×`, `Bearbeiten` und `Lösen` bilden eine gemeinsame Buttonfamilie;
- `Lösen` trennt die Ausgabe von der Reihe, ohne die Ausgabe oder ihre Medien zu löschen.

Der finale Buttonstandard wurde auf dem Android-Gerät ausdrücklich bestätigt.

Der Print-Reihenkopf verwendet nun direkt dieselbe bestätigte Struktur und Designklasse wie der Rubrikkopf:
- gleich kompakte Kopfzeile mit Symbol, mittig angeordnetem Reihennamen samt Auswahlpfeil, `Bearbeiten` und quadratischem `×`;
- `＋ Ausgabe hinzufügen` entspricht optisch `＋ Beitrag hinzufügen`;
- Reihenwahl und Reihenbearbeitung funktionieren weiterhin;
- die bestätigten Print-Ausgabekarten und die Print-Fachlogik blieben unverändert.

Diese Angleichung wurde auf dem Android-Gerät vollständig bestätigt.

## Verwaltung -- aktueller UI-Stand

- Model-Filter ganz oben; mehrere Models können nacheinander ausgewählt und als einzeln entfernbare Chips angezeigt werden.
- Mehrfachauswahl verwendet eine echte UND-Verknüpfung: Ein gemeinsames Shooting beziehungsweise ein gemeinsamer Beitrag muss alle gewählten Models enthalten; getrennte Einzelvorkommen gelten nicht als gemeinsamer Treffer.
- Die UND-Filterung gilt für Print, Titel, Rubriken, Medien, Shootings und Import einschließlich ihrer inneren Karten und Container.
- Model-Mehrfachfilter, Suche, Archivbereich und `Shootings → Ohne Medien` bleiben miteinander kombinierbar. Auswahl, Entfernen und Ergebnisfilterung sind auf dem Android-Gerät bestätigt.
- Archivbereich darunter als Auswahlfeld.
- Haupttabs horizontal: `Print`, `Titel`, `Rubriken`, `Medien`, `Shootings`, `Import`.
- Der Haupttab `Medien` zeigt oben die feste Bereichsüberschrift und darunter den auberginefarbenen Auswahlkopf `Galerien` / `Videos`; `Import` bleibt fachlich und funktional getrennt. Diese Struktur ist gerätetest-bestätigt.
- Tabstreifen ist auf den Verwaltungscontainer begrenzt.
- Printbereich heißt innen `Reihen`.
- Vorschaubilder in den bearbeiteten Verwaltungskarten haben Rundungen an allen vier Ecken.
- Print-Ausgabekarten und Rubrik-Beitragskarten folgen inzwischen derselben Grundsprache.
- Print- und Rubrikkopf sowie ihre Hinzufügen-Buttons sind nun optisch identisch und gerätetest-bestätigt.
- Rubriken sind nach dem Feinschliff gerätetest-bestätigt.
- Titel ist funktional stabil. Titelkopf, `＋ Beitrag hinzufügen`, echte Beitragskarten und Legacy-Shootingkarten sind visuell angeglichen und gerätetest-bestätigt.
- Shooting-Haupttab und der visuelle Galerie-/Video-Zuordnungswähler `Shooting | Beitrag` sind gerätetest-bestätigt.

## Weitere stabile Funktionen

SAF-Restore; permanente Signierung/Update-Installation; Foto-Pinch-Zoom; Video-Wiedergabe; Models, Titel, Rubriken, Shootings, Galerien, Videos, Beiträge, Medienverwaltung; Archivfilter/Suche/Bewertung/Profilbildfilter; Research mit `careerFacts`, `bioFacts`, `archiveFacts`, Korrekturlogik, Profilfakten, Bio-Generator, Import und Erschließungsgrad.

Research-Löschschutz implementiert, separater Gerätetest noch nicht dokumentiert.

## Risiken / offene Punkte

- Research-Löschschutz separat noch nicht gerätetest-dokumentiert.
- Legacy-Titel→Shooting bleibt kontrolliert sichtbar; keine automatische Migration.
- Print besitzt die gerätetest-bestätigte Grundstruktur **Ausgabe → Artikel → Shooting** einschließlich der fachlichen Artikeldaten; Cover-Fotograf(en) fehlen noch.
- Übersicht wurde noch nicht auf die neue Print-/Bereichsstruktur umgebaut.
- ältere 3e-Werknummerierung technisch vorhanden.
- Play-Protect-Vorfall aus 3g nicht als Source-Code-Kausalität behaupten. Play Protect nicht deaktivieren; APK nur als Update.

## Verworfene Ansätze

Nicht ohne neuen ausdrücklichen Plan wieder einführen:
- `Veröffentlichung` als sichtbarer Hauptbereich;
- 4b mit separat zu erstellender Veröffentlichung;
- sichtbares Zusammenführen von Rubrik/Titel/Printausgabe;
- `Individual` als separater fachlicher Haupttyp;
- synthetische Rubrik `Nicht zugeordnet`;
- künstliche Datums-/Ortsangaben zur Shooting-Unterscheidung;
- Model-Filter als Beitragsidentität;
- Galerie-/Video-Nummer als sichtbare Veröffentlichungsidentität;
- Cover/PDF als eigener Datei-Upload direkt im Printformular.
- direkte Foto- oder Galeriezuordnung am Printartikel; Artikel referenzieren ausschließlich ein Shooting.
- eigene Recherchemarkierungen an Printartikeln und Printausgaben; Recherchebedarf wird über Shootings ohne Medien sichtbar.

## Nächster Schritt

**Print: Cover-Fotograf(en) als eigene Angabe der Ausgabe ergänzen.**

Vorgehen:
1. Cover-Fotograf(en) direkt an der konkreten Printausgabe erfassen und bearbeiten können.
2. Die Angabe als Cover-Metadatum behandeln; daraus weder Artikel noch Shooting erzeugen.
3. Cover-Fotograf(en) nicht automatisch auf Artikel oder deren Shootings übertragen.
4. Bestehende Cover-/PDF-Zuordnungen, Bestandsstatus, Cover-Models und Artikel unverändert halten.
5. Danach die Übersicht kontrolliert an die neue Print-/Bereichsstruktur anpassen.

Print, Rubriken, Titelbeiträge, Medienkarten, Shootingkarten und die visuellen Zuordnungswähler bleiben die gerätetest-bestätigte Referenz.
