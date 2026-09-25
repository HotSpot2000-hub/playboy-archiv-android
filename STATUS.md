# Playboy Archiv -- Projektstatus

Stand: 2026-09-25
Referenz-Commit: `79fa39b12139655f7cb2f90a03d158a3365ad8df`

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
- Aktuell berücksichtigter Code: `79fa39b12139655f7cb2f90a03d158a3365ad8df`
- Titelkopf, Hinzufügen-Button, echte Titel-Beitragskarte und der klar getrennte Migrationsbereich für direkte Altzuordnungen sind gerätetest-bestätigt.
- Etappe 4c samt Folgekorrekturen getestet.
- Etappe 5a inklusive 5a.1 und 5a.2 gerätetest-bestätigt.
- Etappe 6a Print-Grundstruktur inklusive Reihen, Ausgaben, Cover/PDF und Löschschutz gerätetest-bestätigt.
- Print/Reihen-Feinschliff einschließlich einheitlicher Aktionsbuttons gerätetest-bestätigt.
- Rubriken-Übertragung auf die Print-Designsprache einschließlich anschließendem Feinschliff gerätetest-bestätigt.
- Medienkarten, Shootingkarten, der visuelle Zuordnungswähler `Shooting | Beitrag`, die indirekte Shooting-Medienauflösung, der bereinigte Importworkflow sowie der neue Beitrags-Zuordnungsmodus sind gerätetest-bestätigt.
- Direkte Rubrik-/Titel→Shooting-Altzuordnungen werden getrennt von echten Beiträgen als `Umstellung erforderlich` ausgewiesen. Eine einzeln bestätigte Migration kann daraus einen echten Beitrag mit ausgewählten freien Galerien/Videos erzeugen und entfernt erst anschließend genau diese Altzuordnung.
- Importworkflow-Korrektur gerätetest-bestätigt: Bereits einem Shooting zugeordnete Fotos können zusätzlich einer Galerie zugeordnet werden; vollständig unzugeordnete Dateien können auch bei `Alle Models` zurück in die Inbox verschoben werden.
- Galerie-/Videodarstellung in der Verwaltung gerätetest-bestätigt: natürliche Dateisortierung, erste Hochformataufnahme als Vorschau, separate reine Foto-Galeriekarte, einzelne Fotos daraus im Fullscreen und Videos ausschließlich über separate ▶︎-Aktionen.
- Der Rubrik-Arbeitsbereich `Nicht zugeordnet` zählt Beiträge nur noch dann, wenn weder Rubrik noch Titel noch Printausgabe verknüpft ist; Titelbeiträge und Printartikel werden nicht mehr fälschlich erfasst.
- Beiträge ohne Veröffentlichungskontext und Galerien/Videos ohne Beitrag besitzen getrennte Teilzähler und können nach Sicherheitsabfrage einzeln gelöscht werden. Elf tatsächlich verwaiste Beiträge wurden damit auf dem Gerät identifiziert und bewusst entfernt.
- `Nicht zugeordnet` ersetzt zugleich die frühere separate Individuals-Logik für fachlich gültige eigenständige Beiträge ohne Rubrik, Titel oder Printausgabe. Solche Beiträge können direkt dort mit optionalem Namen angelegt werden; Galerien und Videos lassen sich ihnen anschließend regulär zuordnen.
- Der reale Testfall `Bed Time` wurde als eigenständiger Beitrag angelegt und erfolgreich mit zwei Videos desselben Shootings verbunden. Er bleibt bewusst ohne Veröffentlichungskontext und wird nicht als verwaister Datensatz behandelt.
- Ehemalige Individuals besitzen nun einen eigenen kontrollierten Migrationsweg: freie Galerien/Videos werden pro Shooting unter `Bisherige Individuals` gebündelt und über `In Beitrag überführen` einem neuen eigenständigen Beitrag zugeordnet, ohne Shooting, Werke oder Dateien zu kopieren.
- Der reale Fall April Katherine wurde mit leerem individuellen Namen erfolgreich überführt: Die vorhandene Galerie wurde übernommen, danach erschien genau ein eigenständiger Beitrag mit `34 Fotos · 0 Videos`.
- Die kontrollierte Überführung der tatsächlichen Archivdaten ist abgeschlossen: `132 von 132` Zuordnungen verwenden die neue Beitragslogik, es besteht keine Altzuordnung mehr. Die nur während der Migration benötigte Fortschrittsanzeige wurde danach entfernt.
- Die durch die neue getrennte Galerie-/Video-Logik entstandene Verzögerung im Medienbereich ist behoben. Ein gemeinsamer Beziehungsindex ersetzt wiederholte vollständige Archivdurchläufe; Dateilisten, Modelableitungen und die Nummerierung gleicher Modelkombinationen werden pro Datenstand wiederverwendet. Beide Optimierungsstufen sind auf dem Android-Gerät bestätigt; die Medienverwaltung reagiert wieder unmittelbar.
- Medienvorschauen werden nur im sichtbaren beziehungsweise nahen Bereich geladen, temporäre Vorschau-URLs anschließend freigegeben und laufende Vorschauerzeugung beim Ansichtswechsel oder Wechsel in den Hintergrund beendet. Fullscreen-Foto- und Videowiedergabe bleiben unverändert.
- Der Verwaltungsumbau einschließlich Datenüberführung und Leistungsoptimierung ist abgeschlossen.
- Der abschließende visuelle Verwaltungsfeinschliff für Titel, Rubriken, Medien, Shootings und Print ist auf dem Android-Gerät bestätigt. Die Bereiche verwenden nun eine einheitliche Modelgruppierung, Kartenhierarchie und Foto-/Videodarstellung.
- Der während der Migration auffällige Akkuverbrauch hat sich nach Abschluss der Überführung und den Vorschau-/Indexoptimierungen bei normaler Nutzung wieder normalisiert.
- Keine automatische Massenmigration; kein Schema-Bump.

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
- vorhandene Legacy-Verknüpfungen zu Rubriken/Titeln bleiben lesbar und werden in den jeweiligen Verwaltungsbereichen ausdrücklich als `Umstellung erforderlich` gekennzeichnet;
- die kontrollierte Einzelmigration erzeugt nur mit mindestens einer ausgewählten, noch freien Galerie beziehungsweise einem freien Video einen echten Beitrag und entfernt erst nach erfolgreichem Speichern genau die betroffene Direktzuordnung;
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
- oberhalb der Karten steht die aus den zugeordneten Shootings abgeleitete exakte Modelkombination als zentrierte Gruppenüberschrift; mehrere Models werden ohne Trennzeichen untereinander dargestellt;
- rechts neben der Vorschau stehen die individuelle Bezeichnung beziehungsweise die bei mehreren Objekten derselben Modelkombination erforderliche Nummer, die Medienanzahl sowie `Bearbeiten` und `Löschen`;
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
- unter `Import → Zugeordnet` können Shooting-Fotos zusätzlich einer Galerie zugeordnet werden, ohne ihre Shootingbeziehung zu verlieren;
- `Zur Inbox` funktioniert für vollständig unzugeordnete Dateien auch bei aktivem Filter `Alle Models`; Dateien mit irgendeiner bestehenden Shooting-, Galerie-, Video- oder Printzuordnung bleiben geschützt;
- im Shootingdetail erscheint `Direkte Zuordnung lösen` ausschließlich bei einer tatsächlichen direkten Legacy-Datei↔Shooting-Beziehung; indirekte Dateien aus Galerie/Video erhalten keine wirkungslose Löseaktion;
- zweistufige Überschrift, Umschalter, Suche, Hinzufügen-Aktionen, Medienzugriff und Importworkflow sind gerätetest-bestätigt;
- der gesamte aktuelle Galerie-/Video-Kartenfeinschliff ist auf dem Android-Gerät bestätigt.
- Galeriezuordnung aus `Zugeordnet` und Rückgabe unzugeordneter Dateien in die Inbox sind auf dem Android-Gerät bestätigt.
- Galerie- und Videodateien werden nach Dateinamen natürlich sortiert (`1, 2, 10` statt `1, 10, 2`).
- Die Verwaltung verwendet für Galerie-/Beitragsvorschauen die erste vorhandene Hochformataufnahme; das gespeicherte Medium und seine Reihenfolge werden dadurch nicht verändert.
- Antippen einer Verwaltungsvorschau öffnet zuerst eine eigene Galeriekarte mit ausschließlich allen Fotos dieser Galerie; ein einzelnes Foto öffnet anschließend die vorhandene Fullscreen-Galerie.
- Videos werden nicht in der Galeriekarte angezeigt. Sie stehen auf der Beitragskarte als separate ▶︎-Aktionen bereit und öffnen direkt den Fullscreen-Videoplayer.
- `Diashow`, `Alle` und `×` wurden in der immersiven Fullscreen-Fotoansicht näher an den oberen Rand gesetzt; auch das Video-`×` verwendet die neue obere Position. Gerätetest bestätigt.

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
- `Nicht zugeordnet` verwendet denselben auberginefarbenen Auswahlkopf, denselben Hinzufügen-Button und dieselbe 2:3-Beitragskarte wie reguläre Rubriken; die frühere technische Arbeitsbereichsdarstellung wurde entfernt.
- Reine Videobeiträge verwenden auch dort die bekannte schwarze 2:3-Videovorschau und behalten zusätzlich ihre einzelnen ▶︎-Aktionen.
- Beitragskarten zeigen die Zahl der tatsächlich enthaltenen Fotos statt der Zahl ihrer Galerie-Container; identische Fotodateien werden dabei nicht doppelt gezählt.
- Beiträge einer Rubrik sowie eigenständige Beiträge unter `Nicht zugeordnet` werden nach exakter Modelkombination gruppiert. Der Modelname erscheint einmal als Gruppenüberschrift und wird in der einzelnen Karte nicht wiederholt.
- Innerhalb der Karte bleibt nur der individuelle Beitragsname beziehungsweise bei mehreren Beiträgen derselben Rubrik/Modelkombination `Nr. 1`, `Nr. 2`, ... sichtbar. Ein einzelner unbenannter Beitrag erhält weiterhin keine Nummer.

Der gesamte Feinschliff wurde auf dem Android-Gerät bestätigt.

Bestätigter Migrationsweg (inzwischen vollständig abgeschlossen):
- echte Beiträge und direkte Rubrik→Shooting-Altzuordnungen werden in der Verwaltung getrennt dargestellt;
- Altzuordnungen erscheinen als `Alte Direktzuordnungen` mit Zähler `Umstellung erforderlich` und gelten ausdrücklich nicht als Beiträge;
- `In Beitrag überführen` bietet ausschließlich Galerien/Videos des betreffenden Shootings an, die noch keinem Beitrag zugeordnet sind;
- die ausgewählten Werke werden einem neu erzeugten echten Beitrag der festen Rubrik zugeordnet; bereits anderweitig zugeordnete Werke werden nicht verändert;
- ohne auswählbares Werk entsteht kein leerer Beitrag;
- die alte Direktzuordnung wird erst nach erfolgreichem Speichern entfernt; bei Speicherfehler wird der Ausgangszustand wiederhergestellt;
- der erste reale Durchlauf wurde mit einer Titelzuordnung und einer neu angelegten Galerie erfolgreich auf dem Android-Gerät getestet; anschließend wurden alle realen Fälle einzeln kontrolliert über denselben Schreibweg abgeschlossen.

## Titel -- Etappe 5a funktional stabil, Beitragskarten gerätetest-bestätigt

Hierarchie: **Titel → Beitrag → Galerie/Video**.

Vorhandene Titel-Tabs bleiben fachlich bestätigt: erste Ebene z. B. `Coed`, `Cyber Girl`, `Playmate`, `SE Model`; zweite Ebene z. B. `of the Week`, `of the Month`, `of the Year`.

Titelüberschrift z. B. `Cyber Girl of the Week` / `3. Woche Juni 2001`; kleiner Pokal links, Modelname nicht in der Überschrift. `＋ Beitrag hinzufügen` steht oberhalb der Beiträge. Beiträge werden nach exakter Modelkombination gruppiert. Die zentrierte Gruppenüberschrift zeigt jedes Model ohne Trennzeichen in einer eigenen Zeile; die Karte wiederholt die Models nicht. In der Karte bleibt nur der individuelle Name beziehungsweise bei mehreren Beiträgen derselben Kombination die Nummer; die Medienanzahl bleibt sichtbar. Direkte Galerie-/Video-Sprungbuttons werden nicht angezeigt. `Lösen` entfernt nur Beitrag ↔ Titel. Gerätetest bestätigt.

Bei Galerie/Video → Beitrag wird zusätzlich der Titelkontext angezeigt. Alte direkte Titel→Shooting-Verknüpfungen werden getrennt als `Alte Direktzuordnungen` / `Umstellung erforderlich` angezeigt und können einzeln kontrolliert in echte Beiträge überführt werden.

Aktueller visueller Stand in Commit `8e627b13187d9f317634b2b7e6178883496f7ce0`:
- der kompakte auberginefarbene Titelkopf mit mittiger Titel-/Zeitraumdarstellung sowie `Bearbeiten` und quadratischem `×` ist gerätetest-bestätigt;
- `＋ Beitrag hinzufügen` entspricht nun optisch dem bestätigten Rubriken-Button und ist gerätetest-bestätigt;
- die echte Titel-Beitragskarte entspricht optisch der bestätigten Rubrik-Beitragskarte und ist auf dem Android-Gerät bestätigt;
- Medienvorschau und Medienanzahl bleiben sichtbar; die überflüssigen direkten Galerie-/Video-Sprungbuttons wurden wie bei Rubriken entfernt, ohne Beziehungen oder Medien zu verändern;
- `Bearbeiten` und `Lösen` bleiben die fachlichen Kartenaktionen;
- der frühere sichtbare Legacy-Bereich ist nun ein eindeutig gekennzeichneter Migrationsbereich; `Prüfen` öffnet das Shooting und `In Beitrag überführen` startet die kontrollierte Einzelmigration;
- eine reale Titel-Altzuordnung wurde erfolgreich in einen echten Beitrag mit einer Galerie überführt; anschließend war genau diese Direktzuordnung entfernt;
- die Galerie enthielt beim Test noch keine Fotos und zeigte deshalb fachlich korrekt `1 Galerie · 0 Videos` am Beitrag sowie `0 Fotos` an der Galerie.
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

Technischer Typ bleibt vorerst `publication`. Grunddaten: Reihe, Ausgabennummer, Zeitraum, Archivbereich, Cover-Models, Cover-Fotograf(en), physischer Bestandsstatus (`Nicht vorhanden`, `Bestellt`, `Im Bestand`).

Cover-Models sind Metadaten der Ausgabe und erzeugen keinen Beitrag bzw. keinen Nachweis für einen Inhalt im Heft. Verwaltungsdarstellung: Reihe als übergeordneter Kopf; Ausgabennummer und Zeitraum in einer Zeile. Mehrmonatige Zeiträume werden in der Verwaltung platzsparend angezeigt, z. B. `Jul./Aug. 2002`, ohne die gespeicherten Grunddaten zu verändern.

Cover-Fotograf(en) werden als eigene optionale ID-Liste direkt an der Ausgabe gespeichert. Sie erscheinen auf Ausgabekarte und Detailansicht als `Cover-Foto: …`, sind über die Verwaltungssuche auffindbar und werden weder in allgemeine Credits geschrieben noch auf Artikel oder Shootings übertragen. Anlegen, Bearbeiten, erneutes Öffnen, Anzeige und Suche sind auf dem Android-Gerät bestätigt.

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
- die redundante Ausgabenüberschrift oberhalb des Covers wurde entfernt, weil Ausgabennummer und Zeitraum bereits auf der Ausgabekarte sichtbar sind;
- Cover und Artikel verwenden dasselbe zweispaltige Kartenraster mit gleich großer 2:3-Vorschau;
- Antippen des Covers öffnet es in der vorhandenen Fullscreen-Fotoansicht;
- Cover-Models und Cover-Fotografen erscheinen zentriert, ohne Trennzeichen und bei mehreren Namen jeweils untereinander;
- Artikel zeigen ebenfalls Models und Fotografen untereinander; dazwischen stehen nur der Seitenbereich und – ausschließlich bei mehreren Shootings derselben exakten Modelkombination – `Nr. 1`, `Nr. 2`, ...;
- die auf vier Zeilen begrenzte und aufklappbare Kurzinfo sowie `Bearbeiten` und `Lösen` bleiben erhalten.

### Print-Design -- vollständig gerätetest-bestätigt

Der Print/Reihen-Bereich bleibt die bestätigte Referenz für Ausgabekarten:
- Ausgabe als innere Karte in sehr hellem Aubergine;
- 2:3-Cover links mit Rundungen an allen vier Ecken;
- Ausgabennummer links und Zeitraum rechts in einer Zeile;
- `＋ Ausgabe`, `×`, `Bearbeiten` und `Lösen` bilden eine gemeinsame Buttonfamilie;
- `Lösen` trennt die Ausgabe von der Reihe, ohne die Ausgabe oder ihre Medien zu löschen.
- Nur Ausgabennummer und kompakter Zeitraum behalten ihre links/rechts angeordnete gemeinsame Zeile. Archivbereich, Cover-Models, Cover-Fotografen, Bestands-/PDF-Kennzeichen und Aktionen sind horizontal zentriert.
- Mehrere Cover-Models und Cover-Fotografen stehen ohne Trennzeichen jeweils untereinander; der bisherige dezente graue Schriftstil der Ausgabekarte bleibt erhalten.

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
- Die Suchfelder der Verwaltungsbereiche behalten während der Eingabe dasselbe aktive Eingabefeld; nur die Ergebnisliste wird neu aufgebaut. Dadurch bleiben Android-Tastatur und Cursor beim Tippen stabil. Für Print, Titel, Rubriken, Medien, Shootings und Import gerätetest-bestätigt.
- Archivbereich darunter als Auswahlfeld.
- Haupttabs horizontal: `Print`, `Titel`, `Rubriken`, `Medien`, `Shootings`, `Import`.
- Der Haupttab `Medien` zeigt oben die feste Bereichsüberschrift und darunter den auberginefarbenen Auswahlkopf `Galerien` / `Videos`; `Import` bleibt fachlich und funktional getrennt. Diese Struktur ist gerätetest-bestätigt.
- Tabstreifen ist auf den Verwaltungscontainer begrenzt.
- Printbereich heißt innen `Reihen`.
- Vorschaubilder in den bearbeiteten Verwaltungskarten haben Rundungen an allen vier Ecken.
- Print-Ausgabekarten und Rubrik-Beitragskarten folgen inzwischen derselben Grundsprache.
- Print- und Rubrikkopf sowie ihre Hinzufügen-Buttons sind nun optisch identisch und gerätetest-bestätigt.
- Rubriken sind nach dem Feinschliff gerätetest-bestätigt.
- Titel ist funktional stabil. Titelkopf, `＋ Beitrag hinzufügen`, echte Beitragskarten und die als Migrationsfälle gekennzeichneten Shootingkarten sind visuell angeglichen und gerätetest-bestätigt.
- Shooting-Haupttab und der visuelle Galerie-/Video-Zuordnungswähler `Shooting | Beitrag` sind gerätetest-bestätigt.
- Rubriken und Titel trennen echte Beiträge jetzt sichtbar von alten Direktzuordnungen. Die kontrollierte Einzelmigration und der ergänzte Importweg `Zugeordnet → Galerie zuordnen` sind gerätetest-bestätigt.
- `Rubriken → Nicht zugeordnet` ist jetzt ein präziser Arbeitsbereich: Beiträge benötigen dort das vollständige Fehlen von Rubrik, Titel und Printausgabe; Galerien/Videos werden separat nur ohne Beitrag gezählt.
- Beide Gruppen zeigen eigene Anzahlen und besitzen manuelle Löschaktionen. Beim Löschen eines Beitrags bleiben Galerien, Videos und Mediendateien erhalten und werden lediglich wieder als unzugeordnet sichtbar. Anzeige, Sicherheitsabfrage und Löschung wurden auf dem Android-Gerät bestätigt.
- Eigenständige Beiträge können unter `Nicht zugeordnet` direkt angelegt werden. Die reguläre Medien→Beitrag-Zuordnung bündelt anschließend auch mehrere Videos zu einem nachvollziehbaren Beitrag, ohne eine künstliche Rubrik zu erzeugen.
- Noch nicht überführte ehemalige Individuals erscheinen dort shootingweise als `Bisherige Individuals · Umstellung erforderlich`. `Prüfen` öffnet das Shooting; `In Beitrag überführen` bietet ausschließlich seine noch freien Galerien/Videos an und wählt sie kontrollierbar voraus.
- Beim Speichern wird nur der fehlende kontextlose Beitrag ergänzt. Shooting, Galerie, Video und Mediendateien bleiben unverändert; bereits einem Beitrag zugeordnete Werke werden ausgeschlossen. Ein leerer Beitrag ohne ausgewähltes Werk wird verhindert.
- Ein individueller Name ist auch für eigenständige Beiträge optional. Ein einzelner unbenannter Beitrag derselben Modelkombination bleibt ohne Nummer; mehrere unbenannte Beiträge erhalten automatisch `Nr. 1`, `Nr. 2`, ... .
- Rubrik-, Titel- und eigenständige Beitragskarten zeigen `Fotos · Videos`; reine Videobeiträge besitzen eine 2:3-Videovorschau. Shootingkarten wählen das erste natürlich sortierte Hochformatfoto auch dann, wenn die Datei nur über `Foto → Galerie → Shooting` indirekt verbunden ist.
- Rubrikbeiträge sind nach Model beziehungsweise exakter Modelkombination gruppiert; die Karte wiederholt den Modelnamen nicht. Die vorhandene Regel „ein Beitrag ohne Nummer, mehrere Beiträge als Nr. 1, Nr. 2, ...“ bleibt bestehen.
- Die Überführung steht bei `100 %` (`132 von 132`); es bestehen keine alten Direktzuordnungen oder bisherigen Individuals mehr. Der temporäre Fortschrittsblock wird deshalb nicht mehr angezeigt.
- Der Medien-Tab verwendet indizierte Beziehungen und pro Datenstand zwischengespeicherte Werkberechnungen. Die zuvor mit wachsendem Archiv auftretende Verzögerung ist auf dem Android-Gerät behoben.
- Titel, Rubriken, Medien und Shootings verwenden dieselbe zweistufige Modeldarstellung: exakte Modelkombination als zentrierte Gruppenüberschrift, darunter die zugehörigen Karten. Vor- und Nachnamen bleiben zusammen; mehrere Models stehen ohne Trennzeichen untereinander.
- Karten zeigen eine vorhandene individuelle Bezeichnung oder – bei mehreren Objekten derselben Modelkombination – die erforderliche Nummer sowie die Medienanzahl. Bei genau einem unbenannten Objekt entfällt die Nummer weiterhin.
- Die Fotoansicht ist in diesen Verwaltungsbereichen vereinheitlicht: Eine 2:3-Vorschau öffnet eine reine, natürlich sortierte Fotokarte ohne redundante Bezeichnung und Medienanzahl; einzelne Fotos wechseln von dort in die Fullscreenansicht. Videos erscheinen ausschließlich als separate runde ▶︎-Aktionen und nicht in der Fotokarte.
- Der Printbereich ist ebenfalls abschließend angeglichen: zentrierte Ausgabemetadaten, gestapelte Model-/Fotografennamen sowie ein einheitliches Cover-/Artikelraster in der Detailansicht. Dieser Stand ist gerätetest-bestätigt.

## Weitere stabile Funktionen

SAF-Restore; permanente Signierung/Update-Installation; Foto-Pinch-Zoom; Video-Wiedergabe; Models, Titel, Rubriken, Shootings, Galerien, Videos, Beiträge, Medienverwaltung; Archivfilter/Suche/Bewertung/Profilbildfilter; Research mit `careerFacts`, `bioFacts`, `archiveFacts`, Korrekturlogik, Profilfakten, Bio-Generator, Import und Erschließungsgrad.

Research-Löschschutz implementiert, separater Gerätetest noch nicht dokumentiert.

## Risiken / offene Punkte

- Research-Löschschutz separat noch nicht gerätetest-dokumentiert.
- Print besitzt die gerätetest-bestätigte Grundstruktur **Ausgabe → Artikel → Shooting** einschließlich der fachlichen Artikeldaten und unabhängiger Cover-Fotografen.
- Übersicht wurde noch nicht auf die neue Print-/Bereichsstruktur umgebaut.
- Der während der intensiven Migration beobachtete hohe Akkuverbrauch ist technisch durch Lazy Loading, Abbruch nicht mehr benötigter Vorschauen und die neuen Berechnungsindizes adressiert. Nach abgeschlossener Migration hat sich der Akkuverbrauch bei normaler Nutzung wieder normalisiert.
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

**Die Verwaltung und ihre Datenüberführung sind abgeschlossen. Als nächstes kann die Übersicht kontrolliert auf die neue Beitrags-, Medien-, Print- und Bereichsstruktur umgebaut werden.**

Vor dem Umbau der Übersicht:
1. die neue Übersicht ausschließlich aus den bestätigten Beziehungen `Beitrag → Galerie/Video → Shooting` sowie `Rubrik/Titel/Printausgabe → Beitrag` ableiten;
2. keine frühere Individuals- oder Direktzuordnungslogik wieder einführen;
3. den Umbau bereichsweise und mit bestehenden echten Archivdaten prüfen.
