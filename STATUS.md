# Playboy Archiv -- Projektstatus

Stand: 2026-09-14
Referenz-Commit: `5979c912a7cb1121a8e26870812d3e3ed586a2a0`

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
- Getesteter Referenz-Code: `5979c912a7cb1121a8e26870812d3e3ed586a2a0`
- Etappe 4c samt Folgekorrekturen getestet.
- Etappe 5a inklusive 5a.1 und 5a.2 gerätetest-bestätigt.
- Etappe 6a Print-Grundstruktur inklusive Reihen, Ausgaben, Cover/PDF und Löschschutz gerätetest-bestätigt.
- Print/Reihen-Feinschliff einschließlich einheitlicher Aktionsbuttons gerätetest-bestätigt.
- Rubriken-Übertragung auf die Print-Designsprache einschließlich anschließendem Feinschliff gerätetest-bestätigt.
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

## Galerie / Video

Galerie = Foto-Werk/Container; Video fachlich parallel. Beide optional mit Shooting und Beitrag (`release`). Models, Fotograf(en), Datum und Ort werden aus Shooting geerbt; Ausschlüsse möglich.

Zielmodell Medien:
- Foto → Galerie → Shooting
- Videodatei → Video → Shooting

Direkte Medien→Shooting-Zuordnung ist noch vorhanden und erst nach Abhängigkeitsprüfung zu entfernen. Visuelle Galerie-/Video-Zielkarten sind getestet.

## Beitrag (`release`)

Kleinster fachlich sinnvoller Veröffentlichungszusammenhang. Er wird innerhalb des Veröffentlichungskontexts angelegt, nicht als Haupttyp. Galerie/Video sind Medien des Beitrags; Shooting ist Entstehung. Beitrag kann Rubrik, Titel und künftig Printausgabe referenzieren. Individueller Originalname gehört zum Beitrag. Models werden aus Galerie/Video → Shooting abgeleitet; der Model-Filter ist keine fachliche Quelle.

Beitragsnummerierung:
- einzelner Beitrag derselben Rubrik/Model-Kombination: keine `Nr. 1`;
- ab mindestens zwei Beiträgen derselben Rubrik und exakt derselben Model-Kombination: `Nr. 1`, `Nr. 2`, ...;
- andere Model-Kombination = eigene Reihe;
- individueller Name wird sichtbar bevorzugt;
- interne Nummer darf stabil gespeichert bleiben;
- manuelle Eingabe der Beitragsnummer ist aus Anlegen/Bearbeiten entfernt; die sichtbare Nummerierung wird automatisch aus dem Beitragskontext bestimmt.

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

Der gesamte Feinschliff wurde auf dem Android-Gerät bestätigt.

## Titel -- Etappe 5a stabil

Hierarchie: **Titel → Beitrag → Galerie/Video**.

Vorhandene Titel-Tabs bleiben fachlich bestätigt: erste Ebene z. B. `Coed`, `Cyber Girl`, `Playmate`, `SE Model`; zweite Ebene z. B. `of the Week`, `of the Month`, `of the Year`.

Titelüberschrift z. B. `Cyber Girl of the Week` / `3. Woche Juni 2001`; kleiner Pokal links, Modelname nicht in der Überschrift. `＋ Beitrag hinzufügen` steht oberhalb der Beiträge. Beiträge zeigen Modelableitung aus Galerie/Video → Shooting, individuellen Namen/Nummer sowie Galerie-/Video-Zugriff. `Lösen` entfernt nur Beitrag ↔ Titel. Gerätetest bestätigt.

Bei Galerie/Video → Beitrag wird zusätzlich der Titelkontext angezeigt. Alte direkte Titel→Shooting-Verknüpfungen bleiben unter `Bisher direkt zugeordnete Shootings` sichtbar und werden nicht automatisch migriert.

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

Antippen einer Ausgabe öffnet die Detailansicht mit vorhandenen Rubrik-/Printbeziehungen. Ausbau auf Beiträge und Seiten folgt später.

### Print-Design -- gerätetest-bestätigt, Kopf als nächster Feinschliff

Der Print/Reihen-Bereich bleibt die bestätigte Referenz für Ausgabekarten:
- Ausgabe als innere Karte in sehr hellem Aubergine;
- 2:3-Cover links mit Rundungen an allen vier Ecken;
- Ausgabennummer links und Zeitraum rechts in einer Zeile;
- `＋ Ausgabe`, `×`, `Bearbeiten` und `Lösen` bilden eine gemeinsame Buttonfamilie;
- `Lösen` trennt die Ausgabe von der Reihe, ohne die Ausgabe oder ihre Medien zu löschen.

Der finale Buttonstandard wurde auf dem Android-Gerät ausdrücklich bestätigt.

Beim Rubrik-Feinschliff hat sich jedoch der **kompaktere Rubrikkopf** als ruhiger und besser erwiesen als der bisherige Print-Reihenkopf. Deshalb ist der nächste geplante visuelle Schritt ausdrücklich, nur diesen bestätigten Rubrik-Kopf auf Print/Reihen zurückzuübertragen. Die bestätigten Print-Ausgabekarten bleiben dabei unangetastet.

## Verwaltung -- aktueller UI-Stand

- Model-Filter ganz oben.
- Archivbereich darunter als Auswahlfeld.
- Haupttabs horizontal: `Print`, `Titel`, `Rubriken`, `Galerien`, `Videos`, `Shootings`, `Medien`.
- Tabstreifen ist auf den Verwaltungscontainer begrenzt.
- Printbereich heißt innen `Reihen`.
- Vorschaubilder in den bearbeiteten Verwaltungskarten haben Rundungen an allen vier Ecken.
- Print-Ausgabekarten und Rubrik-Beitragskarten folgen inzwischen derselben Grundsprache.
- Rubriken sind nach dem Feinschliff gerätetest-bestätigt.
- Titel ist funktional stabil, aber visuell noch nicht auf das neue gemeinsame Muster übertragen.

## Weitere stabile Funktionen

SAF-Restore; permanente Signierung/Update-Installation; Foto-Pinch-Zoom; Video-Wiedergabe; Models, Titel, Rubriken, Shootings, Galerien, Videos, Beiträge, Medienverwaltung; Archivfilter/Suche/Bewertung/Profilbildfilter; Research mit `careerFacts`, `bioFacts`, `archiveFacts`, Korrekturlogik, Profilfakten, Bio-Generator, Import und Erschließungsgrad.

Research-Löschschutz implementiert, separater Gerätetest noch nicht dokumentiert.

## Risiken / offene Punkte

- Direkte Medien→Shooting-Zuordnung vor Entfernung auf Abhängigkeiten prüfen.
- Research-Löschschutz separat noch nicht gerätetest-dokumentiert.
- Legacy-Titel→Shooting bleibt kontrolliert sichtbar; keine automatische Migration.
- Print ist noch nicht auf **Ausgabe → Beitrag → Galerie/Video** und Seiten/Fundstellen ausgebaut.
- Titel ist optisch noch nicht auf das gemeinsame Print-/Rubrik-Muster umgestellt.
- Print-Reihenkopf soll vor Titel noch auf den bestätigten kompakteren Rubrikkopf angeglichen werden.
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

## Nächster Schritt

**Den gerätetest-bestätigten kompakten Rubrikkopf kontrolliert auf den Print-Reihenkopf übertragen.**

Vorgehen:
1. Nur den Print-Reihenkopf ändern: kompakte Höhe/Anordnung, Reihenname horizontal in der verfügbaren Mittelzone zentrieren, Aktionen auf derselben Ebene.
2. Print-Ausgabekarten und sämtliche Print-Fachlogik unverändert lassen.
3. Gerätetest nur dieses Print-Kopfblocks.
4. Nach Bestätigung das gemeinsame Muster kontrolliert auf Titel übertragen.
5. Danach Etappe 6b: **Printausgabe → Beitrag → Galerie/Video**, anschließend Seiten/Fundstellen.

Der aktuelle Rubrik-Stand ist dabei die konkrete Kopf-Referenz; die bestehenden Print-Ausgabekarten bleiben die konkrete Karten-Referenz.
