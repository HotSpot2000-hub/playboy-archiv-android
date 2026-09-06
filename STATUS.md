# Playboy Archiv -- Projektstatus

Stand: 2026-09-06  
Referenz-Commit: `2ae9de6300fa38b47fba28928ce387a1a012e0c6`

> Diese Datei ist die verbindliche Übergabedatei zwischen Arbeitssitzungen.
> Vor neuer Arbeit zusätzlich `AGENTS.md` lesen und prüfen, ob `main` seit
> dem Referenz-Commit weitergelaufen ist.

## Verbindlicher Arbeitsablauf

Für Änderungen gilt der in `AGENTS.md` festgelegte Datei-Workflow:

- Vor jeder Bearbeitung zuerst die aktuelle Version der betroffenen Datei aus `main` lesen.
- Nur diese aktuelle Datei bearbeiten.
- Änderungen möglichst klein und gezielt halten; möglichst nur eine Sache gleichzeitig ändern und testen.
- Fertige Dateien als vollständige Ersatzdateien mit exakt dem Repository-Dateinamen bereitstellen, z. B. `index.html` oder `STATUS.md`.
- Keine Patches oder umbenannten Ersatzdateien, sofern nicht ausdrücklich gewünscht.
- Der Benutzer lädt die fertige Datei selbst in GitHub hoch bzw. ersetzt dort die bestehende Datei.
- Vor dem nächsten Arbeitsschritt den neuen Stand von `main` erneut prüfen.
- APKs immer als UPDATE installieren; App nicht deinstallieren, damit lokale Archivdaten erhalten bleiben.

## Aktueller stabiler Stand

Die Android-App läuft stabil und wird per GitHub Actions als signierte APK gebaut.

- App-Paket: `de.playboy.archiv`
- GitHub Actions Workflow: `.github/workflows/android-debug-apk.yml`
- Daten-Schema-Version: `5`
- `www/index.html` auf `main`: Blob `f633fc61e57b2020b253f7c7304949208f3c3993`
- Research 6.1.1 bis 6.2.4 wurden schrittweise umgesetzt.
- Die aktuellen Research-6.2-Funktionen wurden auf dem Gerät getestet.
- Letzter bestätigter Praxistest: Playboy-Archivwissen vom Typ Video mit bekannter Serie, aber ohne Shooting- oder Medienobjekt, funktioniert.

## Wichtige Dateien

### `www/index.html`

Enthält:

- Hauptoberfläche und Navigation
- Archiv-, Model-, Serien-, Titel-, Shooting- und Medienlogik
- Foto- und Videoanzeige
- Filter und Bewertungsdarstellung
- Research-Bereich, Research-Import und Research-Datenlogik
- IndexedDB / lokale Datenlogik
- `careerFacts`, `bioFacts` und `archiveFacts`
- bestätigte Fakten im Model-Profil
- Korrektur- und Rückzugslogik kanonischer Fakten
- Bio-Entwurfsgenerator

### `native/MainActivity.java`

- Capacitor `BridgeActivity`
- registriert `ArchiveDirectoryPlugin`

### `native/ArchiveDirectoryPlugin.java`

- SAF-Verzeichniszugriff für Archiv-Restore

### `AGENTS.md`

- verbindliche Arbeits- und Übergaberegeln

## Was bereits funktioniert

### Archiv / App-Grundfunktionen

- Archiv-Restore über SAF funktioniert; vollständiger Restore wurde erfolgreich getestet.
- Medien, Shootings und Models werden korrekt wiederhergestellt.
- Permanente Android-Signierung ist eingerichtet; neue Builds lassen sich als Update installieren.
- Pinch-to-Zoom und Verschieben funktionieren; Zoom wird beim Bildwechsel zurückgesetzt.
- Video-Wiedergabe in Hochkant und Querformat funktioniert.
- Android-Zurück beendet die Videoansicht sauber.
- Nativer Video-Fullscreen bleibt deaktiviert (`controlsList="nofullscreen"`), weil er reproduzierbar Freezes verursachte.
- Navigation aus „Zuletzt bearbeitet“ und Shooting-Detail funktioniert.
- Profile aus Models, Titel, Serien und Individuals öffnen oben beim Profilbild.
- Ein-Finger-Scrollen funktioniert; globales Ein-Finger-`touchmove`-Abfangen nicht wieder einführen.
- Kleiner Pull-Down-Effekt am oberen Rand bleibt kosmetisch.

### Model-Übersicht / Filter

- Kompakte Model-Karten.
- Titel: nur höchste erreichte Stufe, Gleichstände bleiben.
- Serien: nur Serien mit höchster Shooting-Anzahl, Gleichstände bleiben.
- Sortierung `A–Z` oder `% ↓`; Prozentgleichstände alphabetisch.
- Profilbild-Filter `Alle / ✓ / ✕` funktioniert zusammen mit Bewertungssortierung.
- Model-/Profilbildfilter wirken auf Models, Titel, Serien und Individuals.
- Gemeinsame Shootings bleiben sichtbar, wenn mindestens ein sichtbares Model beteiligt ist.
- Archivfilter und freie Suche funktionieren stabil.
- Gerätetests erfolgreich.

### Kurzbio

- Eigene klappbare `Kurzbio`-Karte direkt unter den Kerndaten.
- Pro Model als Freitext gespeichert; Absätze und Zwischenüberschriften bleiben erhalten.
- Keine Karte bei leerer Bio.
- Eingeklappt mit Textanfang, Verlauf und `Mehr anzeigen`.
- Darstellung, Bearbeitung, Speichern und Auf-/Zuklappen auf dem Gerät getestet.

### Research Grundlagen

- Eigener Hauptbereich getrennt vom finalen Archiv und Model-Profil.
- Research-Fälle können angelegt, bearbeitet und gelöscht werden.
- Status: `Offen`, `Indizienbasiert`, `Unklar`, `Bestätigt`, `Verworfen`.
- Zuordnung zu Model und optional Shooting.
- Strukturierte Einzelbelege mit Relation, Quelle, Fundstelle, Aussage, Quellenart, Qualität und Prüfdatum.
- Alte Research-Notizen und einfache Belege werden normalisiert.
- Allgemeines, nicht modelbezogenes Research wird unterstützt.
- Grundsatz: `Indizienbasiert` ist kein bestätigter Fakt.
- Research Schritt 1 und 2 wurden auf dem Gerät erfolgreich getestet.

### Faktenmodell -- Research Schritt 3 / 6.2

Aktuelles Datenmodell:

- `state.careerFacts`
- `state.bioFacts`
- `state.archiveFacts`

`archiveFacts` bildet bestätigtes Playboy-bezogenes Wissen ab, das noch nicht vollständig als Archivstruktur erschlossen sein muss.

Ein `archiveFact` kann u. a. enthalten:

- `id`
- `modelId`
- `kind`: `Titel`, `Serie`, `Print`, `Video`, `Pictorial`, `Sonstiges`
- `name`
- `date`
- `quantity`: Ganzzahl oder `null` für unbekannt
- `archiveState`: `unerschlossen`, `teilweise`, `erschlossen`
- `details`
- `linkedArchiveIds`
- `sourceResearchIds`
- `status`
- Zeitstempel

Lebenszyklus:

- `aktiv`
- `ersetzt`
- `zurückgezogen`

Grundsatz:

- `unbekannt` ist nicht `0`
- bekanntes Wissen darf existieren, ohne künstliche Shootings, Pictorials oder Medienobjekte anzulegen
- spätere Archiverschließung muss Doppelzählungen vermeiden

### Kontrollierte Research-Übernahme -- Research Schritt 4 / 4.1

- Bestätigte modelbezogene Research-Fälle können kontrolliert als `Karrierefakt`, `Biofakt` oder `Archivwissen` übernommen werden.
- Vor dem Speichern erscheint eine konkrete Vorschau.
- Erst `Übernahme bestätigen` schreibt den Fakt.
- `sourceResearchIds` erhält die Herkunft; Belege und Fundstellen bleiben im Research-Fall.
- Karrierefakten verwenden standardisierte Arten und Rollen.
- Regelbasierte Vorschläge erkennen u. a. Magazinauftritt, Videoauftritt, TV-Auftritt, Werbung/Kampagne, Kalender, Auszeichnung und Event/Auftritt.
- Medium/Organisation, Werk/Ausgabe, Datum/Zeitraum, Rolle und sichere Details werden soweit eindeutig ableitbar vorausgefüllt.
- Vorschläge bleiben editierbar; bei Unsicherheit wird nicht geraten.
- Biofakten erhalten vorsichtige Vorschläge für Kategorie und Aussageart, insbesondere für Selbstaussagen.
- Gerätetests mit Tiffany Ryan erfolgreich.

### Research 6.2 -- Archivwissen als Brücke

Die Wissensebene zwischen Research und vollständiger Archiverschließung ist umgesetzt.

Fachliche Trennung:

- Externe Magazine, Kampagnen, Kalender, TV usw. bleiben Karrierefakten.
- `Archivwissen` ist für Playboy-bezogenes bestätigtes Wissen gedacht, dessen Archivstruktur noch nicht vollständig erschlossen ist.
- Externe Printausgaben können später optional im Karriere-/Biobereich als „im Bestand“ markiert werden, gehören aber nicht in das Playboy-Medienarchiv.

Archivwissen kann aktuell unabhängig von vollständigen Archivobjekten enthalten:

- bestätigte Titel
- bestätigte Serien
- bestätigte Playboy-Printinformationen
- bestätigte Videos
- bestätigte Pictorials
- sonstiges bestätigtes Playboy-Archivwissen

Es werden keine künstlichen Shootings oder Medienobjekte erzeugt.

### Research 6.2.1 -- kanonische Titellogik

Archivwissen vom Typ `Titel` verwendet dieselbe fachliche Titellogik wie das eigentliche Archiv:

Titelklassen:

- Coed
- SE Model
- Playmate
- Cyber Girl

Titelstufen werden passend zur Klasse angeboten.

Zeitlogik:

- Week: exaktes Datum oder Woche im Monat + Monat/Jahr
- Month: Monat + Jahr
- Year: Jahr

Titelname und Anzeige werden aus den bestehenden Titel-Helfern erzeugt.

Erfolgreicher Referenzfall Tahlia Paris:

- Cyber Girl of the Month — Januar 2016
- Cyber Girl of the Year — 2017

Unbekannte Pictorial-Zuordnungen bleiben ausdrücklich offen; vorhandene Shootings müssen dafür nicht umklassifiziert werden.

### Research 6.2.2 -- Korrektur, Ersetzen und Zurückziehen

Aktive Karriere-, Archiv- und Biofakten können im Profil bearbeitet werden über:

- `Korrigieren`
- `Zurückziehen`

Korrektur:

- erzeugt einen neuen aktiven Fakt
- alter Fakt erhält `status = ersetzt`
- alter Fakt verweist über `replacedByFactId` auf den neuen
- neuer Fakt verweist über `replacesFactId` auf den alten
- Research-Herkunft bleibt erhalten

Zurückziehen:

- setzt `status = zurückgezogen`
- speichert `withdrawnAt`
- der Fakt verschwindet aus der normalen Profilansicht
- historische Information bleibt erhalten

Research-Löschschutz:

- Ein Research-Fall mit aktiven referenzierenden Fakten darf nicht still gelöscht werden.
- Der Benutzer soll zuerst den aktiven Fakt korrigieren oder zurückziehen.
- Die Löschschutz-Logik ist implementiert.
- Ein expliziter separater Gerätetest nur für den Research-Löschschutz ist noch nicht dokumentiert.

Der Tahlia-Praxistest mit Zurückziehen veralteter Titel-Fakten war erfolgreich.

### Bestätigte Fakten im Model-Profil

Die Karte `Bestätigte Fakten` zeigt aktive:

- Karrierefakten
- Archivwissen
- Biografiefakten

Aktueller UI-Stand:

- optisch an die Kurzbio angeglichen
- kleinere, weniger dominante Typografie
- kompaktere Metadaten
- standardmäßig eingeklappt
- eingeklappt bleibt ein Textausschnitt sichtbar
- gleiche Verlaufsabblendung wie bei Kurzbio
- `Mehr anzeigen`
- Korrektur- und Rückzugsaktionen bleiben im aufgeklappten Zustand verfügbar

Gerätetest erfolgreich; Benutzer bestätigte, dass die Darstellung gefällt.

### Research 6.2.4 -- bekannte Serie ohne Shooting/Medium

Playboy-Archivwissen vom Typ `Video` oder `Pictorial` kann eine bereits vorhandene Serie referenzieren, obwohl:

- kein Shooting bekannt oder zugeordnet ist
- noch kein Medienobjekt im Archiv existiert
- die konkrete Medienbezeichnung unbekannt sein kann

Im Übernahmeformular steht dafür `Bekannte Serie` zur Verfügung.

Eigenschaften:

- es werden nur bestehende Serien ausgewählt
- es wird kein künstliches Shooting erzeugt
- es wird kein künstliches Medienobjekt erzeugt
- die Serienbeziehung wird über `linkedArchiveIds` erhalten
- bei Video/Pictorial darf die Bezeichnung unbekannt bleiben
- Research-Text kann eine vorhandene Serie als Vorschlag erkennen
- die Serienbeziehung erscheint im Profil bei den bestätigten Fakten
- Korrektur erhält bzw. ändert die bekannte Serienbeziehung kontrolliert
- Duplikatprüfung berücksichtigt bei Video/Pictorial auch die Serienbeziehung

Erfolgreicher Praxistest mit Tiffany Ryan:

- ein bestätigtes Video innerhalb `Busty Babes`
- ein bestätigtes Video innerhalb `Women of Playboy`
- jeweils als Archivwissen
- bekannte Serie vorhanden
- Shooting-Zuordnung unbekannt
- Medium noch nicht als Archivobjekt vorhanden
- kein Platzhalterobjekt nötig

Der Benutzer bestätigte am 2026-09-06: „Funktioniert“.

### Recherche-Import -- Research Schritt 5

- `Recherche-Import` kann mehrere strukturierte Research-Fälle als JSON einlesen.
- Model wird einmal für den Import gewählt.
- Vorschau zeigt Anzahl und Statusverteilung.
- Einzelbelege und deren strukturierte Angaben bleiben erhalten.
- Import erzeugt keine kanonischen Fakten und überschreibt keine Kurzbio.
- Erster echter Mehrfachimport mit Tiffany Ryan erfolgreich.
- Getestete Kette:
  `Recherche-Import → Research → Prüfung → bestätigter Fall → kontrollierte Übernahme → kanonischer Fakt → Model-Profil`.

### Bio-Generator -- Research 6 / 6.1 / 6.1.1

- Im Profil-Editor kann über `Bio-Entwurf aus bestätigten Fakten` ein neuer Entwurf erzeugt werden.
- Das Erzeugen verändert die bestehende Kurzbio nicht.
- Der Entwurf ist separat editierbar.
- Erst `In Kurzbio-Editor übernehmen` kopiert den Entwurf in den normalen Editor.
- Auch danach wird erst durch das normale `Profil speichern` gespeichert.
- `Zurück ohne Übernahme` verändert die bestehende Kurzbio nicht.
- Der Generator verwendet kanonische Archivdaten sowie aktive bestätigte Fakten.
- Offene, unklare, indizienbasierte oder verworfene Research-Fälle werden nicht als Tatsachen verwendet.
- Playboy bleibt der Schwerpunkt.
- Serien werden namentlich genannt, wenn sie aus der kanonischen Archivstruktur ableitbar sind.
- Karrierefakten werden zu natürlicheren Absätzen zusammengefasst.
- Mehrere Magazinauftritte werden gebündelt.
- Redundante Datumsangaben und technische Legacy-Details werden vermieden.
- Research 6.1.1 wurde auf dem Gerät erfolgreich getestet und sprachlich akzeptiert.

### Behobener Schema-Zwischenfall bei Research 6.1

- Eine zwischenzeitlich bereitgestellte 6.1-Datei enthielt fälschlich `DATA_SCHEMA_VERSION=1`.
- Die Startschutz-Logik erkannte den vorhandenen neueren Datenbestand und blockierte das Speichern.
- Es wurde nicht gespeichert und die App wurde nicht deinstalliert.
- Die lokalen Archivdaten blieben geschützt.
- Der Zwischenfall ist behoben.
- `DATA_SCHEMA_VERSION` niemals unter den vorhandenen Datenstand zurücksetzen.
- Aktueller Datenstand: Schema `5`.

### Bewertung

- Fünf Stufen: `5 = 100%`, `4 = 90%`, `3 = 70%`, `2 = 40%`, `1 = 0%`.
- `Nicht bewertet` bleibt getrennt.
- Gewichtung: Größe, Gesicht, Busen, Pussy, Eindruck jeweils 20%.
- Gesamtbewertung, Filter und Sortierung verwenden die etablierte gerundete Prozentlogik.
- Bei exakt 100% wird `❤️` angezeigt.
- Gerätetest erfolgreich.

## Fachliche Zielstruktur

Grundsatz:

`Research → bestätigtes Wissen / kanonischer Fakt → Archiverschließung → Bio / Auswertung`

- Research bleibt Belege-, Prüf- und Herkunftsebene.
- Bestätigte Fakten beschreiben gesichertes Wissen.
- Das eigentliche Archiv bildet die tatsächlich erschlossenen Objekte und Beziehungen ab.
- Ein bestätigtes Wissen darf bereits existieren, obwohl das dazugehörige Archivobjekt oder seine vollständigen Medienbeziehungen noch nicht angelegt sind.
- Dadurch sind keine künstlichen Shootings oder Medienobjekte nötig.
- `sourceResearchIds` hält die Herkunft nachvollziehbar.
- `linkedArchiveIds` kann bereits bekannte reale Archivbeziehungen festhalten.
- Selbstauskünfte werden als solche gekennzeichnet und nicht automatisch als unabhängige Bestätigung behandelt.
- Keine doppelten Wahrheiten: Wird ein zunächst faktisch erfasster Fund später vollständig im Archiv erschlossen, müssen Anzeige, Bio und Auswertung Doppelzählungen vermeiden.
- Eine veröffentlichte/redaktionell bearbeitete Kurzbio darf nie ohne ausdrückliche Bestätigung überschrieben werden.

## Reale Referenzfälle

### Tiffany Ryan

Aktuell wichtige Trennung:

- externe Printauftritte wie MuscleMag, American Curves usw. bleiben Karrierefakten
- Playboy-Medienwissen gehört in `archiveFacts`

Bestätigtes Playboy-Wissen:

- `Busty Babes`: ein Video bestätigt; Serie bekannt; Shooting unbekannt; Medium noch nicht als Archivobjekt vorhanden
- `Women of Playboy`: ein Video bestätigt; Serie bekannt; Shooting unbekannt; Medium noch nicht als Archivobjekt vorhanden

Diese Fälle sind auf dem Gerät erfolgreich getestet.

### Tahlia Paris

Bestätigtes Titelwissen:

- Cyber Girl of the Month — Januar 2016
- Cyber Girl of the Year — 2017

Die Titel können als kanonisches Wissen existieren, ohne konkrete Pictorials zu erfinden oder vorhandene Individuals zwangsweise einem Titel zuzuordnen.

Der Korrektur-/Rückzugsworkflow wurde mit Tahlia erfolgreich getestet.

## Geplanter Folgebaustein: Erschließungsgrad im Model-Profil

Nach dem stabilen 6.2-Faktenmodell soll aus bestätigtem Wissen und tatsächlicher Archiverschließung ein transparenter Erschließungsgrad entstehen.

Der Erschließungsgrad soll nicht die Qualität des Archivs bewerten, sondern zeigen, wie viel des bereits bestätigten Wissens strukturiert erschlossen ist.

Vorgesehene Teilbereiche:

- Serien: bestätigt / im Archiv / vollständig zugeordnet
- Titel: bestätigt / im Archiv / Medienzuordnung geklärt
- Pictorials: bestätigt / archiviert
- Videos: bestätigt / archiviert
- Playboy-Print: bestätigt / als Publikation erschlossen
- offene Detailangaben und unbekannte Zuordnungen

Wichtig:

- `unbekannt` ist nicht dasselbe wie `0`
- `bekannt, aber noch nicht archiviert` ist nicht dasselbe wie `offen/unbestätigt`
- Fakten und echte Archivobjekte dürfen nicht doppelt gezählt werden
- der Erschließungsgrad soll später als Grundlage für Filter dienen

Gewünschte spätere Lückenansichten z. B.:

- `Serie bekannt, Medien fehlen`
- `Titel bestätigt, Pictorial-Zuordnung offen`
- `Playboy-Print bestätigt, Publikation noch nicht erschlossen`

## Offene Punkte

- Doppelzählungs-/Abgleichlogik zwischen bestätigtem Fakt und späterem Archivobjekt
- Erschließungsgrad im Model-Profil
- spätere Filter-/Lückenansicht
- optionales `im Bestand` für externe Printausgaben im Karriere-/Biobereich
- kontrollierte Übernahme in weitere bestehende Archivbeziehungen
- automatisch erzeugtes `Auf einen Blick`
- weitergehende Präzisierung von Galerie/Pictorial, Ausgabe/Issue, Collection/Reihe, Bereich/Plattform und Titelprogrammen
- Research-Löschschutz separat auf dem Gerät testen, falls noch nicht geschehen

### Video-Fullscreen

Weiterhin offen, aktuell kein Arbeitsschwerpunkt. Bevorzugter zukünftiger Ansatz: eigener In-App-Fullscreen statt nativer WebView-Fullscreen-Umschaltung.

### Kleiner Pull-Down-Effekt

Niedrige Priorität.

Verworfene Ansätze:

- `overscroll-behavior-y:none` als gezielter Lösungsversuch
- `WebView.setOverScrollMode(View.OVER_SCROLL_NEVER)`
- JavaScript-Abfangen von `touchmove`
- `<main>` als eigener Scrollcontainer

## Dinge, die NICHT wiederholt werden sollten

- Kein globales `WindowCompat.setDecorFitsSystemWindows(false)`.
- Keine optionalen Plugin-Aufrufe wie `plugin?.method?.().catch(...)`, wenn die Methode möglicherweise nicht existiert.
- Kein `main{height:100vh;overflow-y:auto;...}` als eigener Haupt-Scrollcontainer.
- Kein globaler Ein-Finger-`touchmove`-Handler mit `preventDefault()`.
- Kein nativer Video-Fullscreen.
- Kein Rücksetzen von `DATA_SCHEMA_VERSION` unter den vorhandenen Datenstand.
- Keine künstlichen Shootings/Medienbeziehungen nur deshalb anlegen, damit bestätigtes Wissen im Archiv sichtbar wird.
- Externe Karriere-/Printfakten nicht in das Playboy-Medienarchiv verschieben.

## Zuletzt abgeschlossener Arbeitsblock

Research 6.2.4 -- bekannte Serienbeziehung für noch nicht erschlossene Playboy-Medien:

- Video/Pictorial kann eine vorhandene Serie referenzieren.
- Shooting und konkretes Medienobjekt dürfen weiterhin unbekannt sein.
- Leere Medienbezeichnung bleibt unbekannt statt künstlich ergänzt zu werden.
- Keine Platzhalterobjekte.
- Fachliche Grenze zwischen externen Karrierefakten und Playboy-Archivwissen wurde präzisiert.
- Schema bleibt `5`.
- Praxistest mit Tiffany Ryan erfolgreich.
- Benutzer bestätigte am 2026-09-06, dass die Funktion funktioniert.

Der fachlich getestete Code-Stand ist Commit
`2ae9de6300fa38b47fba28928ce387a1a012e0c6`.

## Letzter sinnvoller nächster Schritt

Als nächster kleiner Baustein bietet sich der erste **Erschließungsgrad im Model-Profil** an.

Vor der Umsetzung zuerst festlegen, wie bestätigte Fakten mit bereits vorhandenen Archivobjekten abgeglichen werden, damit keine Doppelzählungen entstehen.

Empfohlener erster Umfang:

- nur Playboy-Archivwissen berücksichtigen
- zunächst Titel, Serien, Videos und Pictorials
- bekannte, aber noch nicht archivierte Inhalte ausdrücklich getrennt anzeigen
- offene Zuordnungen sichtbar machen
- keine subjektive Gesamtpunktzahl
- noch keine komplexen Filter, bevor die Anzeige stabil getestet ist

## Pflegehinweis

Nach jeder abgeschlossenen funktionalen Änderung:

- prüfen, ob diese Datei angepasst werden muss
- Referenz-Commit auf den zuletzt fachlich berücksichtigten Code-Stand setzen
- offene bzw. nächste Schritte korrigieren
- Sitzung erst danach als abgeschlossen betrachten
