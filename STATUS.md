# Playboy Archiv -- Projektstatus

Stand: 2026-09-06\
Referenz-Commit: `1a4a0038cfc3ae634b749f6b1c0979fa20e2dd80`

> Diese Datei ist die verbindliche Übergabedatei zwischen
> Arbeitssitzungen. Vor neuer Arbeit zusätzlich `AGENTS.md` lesen und
> prüfen, ob `main` seit dem Referenz-Commit weitergelaufen ist.

## Verbindlicher Arbeitsablauf

Für Änderungen gilt der in `AGENTS.md` festgelegte Datei-Workflow:

-   Vor jeder Bearbeitung zuerst die aktuelle Version der betroffenen
    Datei aus `main` lesen.
-   Nur diese aktuelle Datei bearbeiten.
-   Änderungen möglichst klein und gezielt halten; möglichst nur eine
    Sache gleichzeitig ändern und testen.
-   Fertige Dateien als vollständige Ersatzdateien mit exakt dem
    Repository-Dateinamen bereitstellen, z. B. `index.html` oder
    `STATUS.md`.
-   Keine Patches oder umbenannten Ersatzdateien, sofern nicht
    ausdrücklich gewünscht.
-   Der Benutzer lädt die fertige Datei selbst in GitHub hoch bzw.
    ersetzt dort die bestehende Datei.
-   Vor dem nächsten Arbeitsschritt den neuen Stand von `main` erneut
    prüfen.
-   APKs immer als UPDATE installieren; App nicht deinstallieren, damit
    lokale Archivdaten erhalten bleiben.

## Aktueller stabiler Stand

Die Android-App läuft stabil und wird per GitHub Actions als signierte
APK gebaut.

-   App-Paket: `de.playboy.archiv`
-   GitHub Actions Workflow: `.github/workflows/android-debug-apk.yml`
-   Daten-Schema-Version: `4`
-   `www/index.html` auf `main`: Blob
    `59b1098140612adadafdc451d85642573f623bd1`
-   Research 6.1.1 wurde auf dem Gerät erfolgreich getestet und vom
    Benutzer sprachlich als gelungen bestätigt.

## Wichtige Dateien

-   `www/index.html`
    -   Hauptoberfläche, Navigation, Archiv- und Model-Logik
    -   Foto- und Videoanzeige
    -   Filter und Bewertungsdarstellung
    -   Research-Bereich, Research-Import und Research-Datenlogik
    -   IndexedDB / lokale Datenlogik
    -   `careerFacts` und `bioFacts`
    -   bestätigte Fakten im Model-Profil
    -   Bio-Entwurfsgenerator
-   `native/MainActivity.java`
    -   Capacitor BridgeActivity
    -   registriert `ArchiveDirectoryPlugin`
-   `native/ArchiveDirectoryPlugin.java`
    -   SAF-Verzeichniszugriff für Archiv-Restore
-   `AGENTS.md`
    -   verbindliche Arbeits- und Übergaberegeln

## Was bereits funktioniert

### Archiv / App-Grundfunktionen

-   Archiv-Restore über SAF funktioniert; vollständiger Restore wurde
    erfolgreich getestet.
-   Medien, Shootings und Models werden korrekt wiederhergestellt.
-   Permanente Android-Signierung ist eingerichtet; neue Builds lassen
    sich als Update installieren.
-   Pinch-to-Zoom und Verschieben funktionieren; Zoom wird beim
    Bildwechsel zurückgesetzt.
-   Video-Wiedergabe in Hochkant und Querformat funktioniert.
-   Android-Zurück beendet die Videoansicht sauber.
-   Nativer Video-Fullscreen bleibt deaktiviert
    (`controlsList="nofullscreen"`), weil er reproduzierbar Freezes
    verursachte.
-   Navigation aus „Zuletzt bearbeitet" und Shooting-Detail
    funktioniert.
-   Profile aus Models, Titel, Serien und Individuals öffnen oben beim
    Profilbild.
-   Ein-Finger-Scrollen funktioniert; globales
    Ein-Finger-`touchmove`-Abfangen nicht wieder einführen.
-   Kleiner Pull-Down-Effekt am oberen Rand bleibt kosmetisch.

### Model-Übersicht / Filter

-   Kompakte Model-Karten.
-   Titel: nur höchste erreichte Stufe, Gleichstände bleiben.
-   Serien: nur Serien mit höchster Shooting-Anzahl, Gleichstände
    bleiben.
-   Sortierung `A–Z` oder `% ↓`; Prozentgleichstände alphabetisch.
-   Profilbild-Filter `Alle / ✓ / ✕` funktioniert zusammen mit
    Bewertungssortierung.
-   Model-/Profilbildfilter wirken auf Models, Titel, Serien und
    Individuals.
-   Gemeinsame Shootings bleiben sichtbar, wenn mindestens ein
    sichtbares Model beteiligt ist.
-   Archivfilter und freie Suche funktionieren stabil.
-   Gerätetests erfolgreich.

### Kurzbio

-   Eigene klappbare `Kurzbio`-Karte direkt unter den Kerndaten.
-   Pro Model als Freitext gespeichert; Absätze und
    Zwischenüberschriften bleiben erhalten.
-   Keine Karte bei leerer Bio.
-   Eingeklappt mit Textanfang, Verlauf und `Mehr anzeigen`.
-   Darstellung, Bearbeitung, Speichern und Auf-/Zuklappen auf dem Gerät
    getestet.

### Research

-   Eigener Hauptbereich getrennt vom finalen Archiv und Model-Profil.
-   Research-Fälle können angelegt, bearbeitet und gelöscht werden.
-   Status: `Offen`, `Indizienbasiert`, `Unklar`, `Bestätigt`,
    `Verworfen`.
-   Zuordnung zu Model und optional Shooting.
-   Strukturierte Einzelbelege mit Relation, Quelle, Fundstelle,
    Aussage, Quellenart, Qualität und Prüfdatum.
-   Alte Research-Notizen und einfache Belege werden normalisiert.
-   Allgemeines, nicht modelbezogenes Research wird unterstützt.
-   Grundsatz: `Indizienbasiert` ist kein bestätigter Fakt.
-   Research Schritt 1 und 2 wurden auf dem Gerät erfolgreich getestet.

### Faktenmodell -- Research Schritt 3

-   Daten-Schema-Version `4`.
-   `state.careerFacts` und `state.bioFacts` sind eigene kanonische
    Sammlungen.
-   Bestehende Zustände werden beim Normalisieren sicher um fehlende
    Sammlungen ergänzt.
-   Bestehende Archiv-, Model-, Research-, Evidence- und Revisionsdaten
    werden nicht umgebaut.
-   App-Start, vorhandene Daten, Speichern und erneutes Öffnen wurden
    auf dem Gerät getestet.

### Kontrollierte Research-Übernahme -- Research Schritt 4 / 4.1

-   Bestätigte modelbezogene Research-Fälle können kontrolliert als
    `Karrierefakt` oder `Biofakt` übernommen werden.
-   Vor dem Speichern erscheint eine konkrete Vorschau.
-   Erst `Übernahme bestätigen` schreibt den Fakt.
-   `sourceResearchIds` erhält die Herkunft; Belege und Fundstellen
    bleiben im Research-Fall.
-   Karrierefakten verwenden standardisierte Arten und Rollen.
-   Regelbasierte Vorschläge erkennen u. a. Magazinauftritt,
    Videoauftritt, TV-Auftritt, Werbung/Kampagne, Kalender, Auszeichnung
    und Event/Auftritt.
-   Medium/Organisation, Werk/Ausgabe, Datum/Zeitraum, Rolle und sichere
    Details werden soweit eindeutig ableitbar vorausgefüllt.
-   Vorschläge bleiben editierbar; bei Unsicherheit wird nicht geraten.
-   Biofakten erhalten vorsichtige Vorschläge für Kategorie und
    Aussageart, insbesondere für Selbstaussagen.
-   Gerätetests mit Tiffany Ryan erfolgreich.

### Bestätigte Fakten im Model-Profil

-   Aktive `careerFacts` und `bioFacts` erscheinen in einer klappbaren
    Karte `Bestätigte Fakten`.
-   Karriere- und Biografiefakten werden getrennt dargestellt.
-   Research-Herkunft wird angezeigt, wenn vorhanden.
-   Ansicht ist bewusst schreibgeschützt.
-   Gerätetest erfolgreich.

### Recherche-Import -- Research Schritt 5

-   `Recherche-Import` kann mehrere strukturierte Research-Fälle als
    JSON einlesen.
-   Model wird einmal für den Import gewählt.
-   Vorschau zeigt Anzahl und Statusverteilung.
-   Einzelbelege und deren strukturierte Angaben bleiben erhalten.
-   Import erzeugt keine kanonischen Fakten und überschreibt keine
    Kurzbio.
-   Erster echter Mehrfachimport mit Tiffany Ryan erfolgreich.
-   Getestete Kette:
    `Recherche-Import → Research → Prüfung → bestätigter Fall → kontrollierte Übernahme → kanonischer Karrierefakt → Model-Profil`.

### Bio-Generator -- Research 6 / 6.1 / 6.1.1

-   Im Profil-Editor kann über `Bio-Entwurf aus bestätigten Fakten` ein
    neuer Entwurf erzeugt werden.
-   Das Erzeugen verändert die bestehende Kurzbio nicht.
-   Der Entwurf ist separat editierbar.
-   Erst `In Kurzbio-Editor übernehmen` kopiert den Entwurf in den
    normalen Editor.
-   Auch danach wird erst durch das normale `Profil speichern`
    gespeichert.
-   `Zurück ohne Übernahme` verwirft keine bestehende Kurzbio und
    verändert den Kurzbio-Editor nicht.
-   Der Generator verwendet kanonische Archivdaten sowie aktive
    bestätigte `careerFacts` und `bioFacts`.
-   Offene, unklare, indizienbasierte oder verworfene Research-Fälle
    werden nicht als Tatsachen verwendet.
-   Sichtbare Stammdaten werden nicht unnötig in die Bio kopiert.
-   Playboy bleibt der Schwerpunkt.
-   Serien werden namentlich genannt, wenn sie aus der kanonischen
    Archivstruktur ableitbar sind.
-   Kanonisch verknüpfte Serien-Videos können mitgezählt werden.
-   Karrierefakten werden zu natürlicheren Absätzen zusammengefasst
    statt als wiederholte Einzelsätze ausgegeben.
-   Mehrere Magazinauftritte werden in einem gemeinsamen Satz gebündelt.
-   Wiederholte Formulierungen wie `Tiffany war ... vertreten` wurden
    reduziert.
-   Datumsangaben werden nicht nochmals angehängt, wenn sie bereits im
    Werk-/Ausgabetitel enthalten sind.
-   Technische Legacy-Details wie
    `Aus früherer Research-Notiz übernommen.` werden nicht in die Bio
    geschrieben.
-   Research 6.1.1 wurde auf dem Gerät erfolgreich getestet; der
    Benutzer bestätigte, dass die neue Formulierung gefällt.

### Behobener Schema-Zwischenfall bei Research 6.1

-   Eine zwischenzeitlich bereitgestellte 6.1-Datei enthielt fälschlich
    `DATA_SCHEMA_VERSION=1`.
-   Die Startschutz-Logik erkannte den vorhandenen lokalen
    Schema-4-Datenbestand als neuer und blockierte das Speichern.
-   Es wurde nicht gespeichert und die App wurde nicht deinstalliert;
    die lokalen Archivdaten blieben geschützt.
-   Die korrigierte Datei wurde aus dem zuvor verifizierten
    Research-6-Stand rekonstruiert und wieder mit
    `DATA_SCHEMA_VERSION=4` bereitgestellt.
-   Korrigierter Build und anschließendes Research 6.1.1 funktionieren
    auf dem Gerät.
-   Der Zwischenfall gilt als behoben und bestätigt gleichzeitig, dass
    der Startschutz bei einem Schema-Rückschritt greift.

### Bewertung

-   Fünf Stufen: `5 = 100%`, `4 = 90%`, `3 = 70%`, `2 = 40%`, `1 = 0%`.
-   `Nicht bewertet` bleibt getrennt.
-   Gewichtung: Größe, Gesicht, Busen, Pussy, Eindruck jeweils 20%.
-   Gesamtbewertung, Filter und Sortierung verwenden die etablierte
    gerundete Prozentlogik.
-   Bei exakt 100% wird `❤️` angezeigt.
-   Gerätetest erfolgreich.

## Fachliche Zielstruktur

Grundsatz:

`Research → bestätigtes Wissen / kanonischer Fakt → Archiverschließung → Bio / Auswertung`

-   Research bleibt Belege-, Prüf- und Herkunftsebene.
-   Bestätigte Fakten beschreiben gesichertes Wissen.
-   Das eigentliche Archiv bildet die tatsächlich erschlossenen Objekte
    und Beziehungen ab.
-   Ein bestätigtes Wissen darf künftig bereits existieren, obwohl das
    dazugehörige Archivobjekt oder seine vollständigen Medienbeziehungen
    noch nicht angelegt sind.
-   Dadurch sollen keine künstlichen Shootings oder Medienobjekte mehr
    nötig sein, nur um bekanntes Wissen festzuhalten.
-   `sourceResearchIds` hält die Herkunft zu Research-Fällen
    nachvollziehbar.
-   Selbstauskünfte werden als solche gekennzeichnet und nicht
    automatisch als unabhängige Bestätigung behandelt.
-   Keine doppelten Wahrheiten: Wenn ein zunächst faktisch erfasster
    Fund später vollständig im Archiv erschlossen wird, müssen Anzeige,
    Bio und Auswertung Doppelzählungen vermeiden.
-   `careerFacts` / ergänzende kanonische Fakten dürfen als Brücke für
    bestätigte Print-, Video-, Serien- und Titelfunde dienen, solange
    die eigentliche Archivstruktur noch unvollständig ist.
-   Eine veröffentlichte/redaktionell bearbeitete Kurzbio darf nie ohne
    ausdrückliche Bestätigung überschrieben werden.

## Research 6.2 -- vereinbarte Zielrichtung

Research 6.2 ist der nächste funktionale Schritt.

Ziel ist eine saubere Wissensebene zwischen Research und vollständiger
Archiverschließung. Bestätigte Informationen sollen übernommen werden
können, ohne dass dafür künstliche Platzhalter-Shootings, Pictorials
oder Videos angelegt werden müssen.

Reale Referenzfälle:

### Tiffany Ryan

-   Bestätigte Printauftritte und zwei TeasUm-Videos sollen als
    gesichertes Wissen nutzbar sein, auch wenn noch nicht für alles
    vollständige Archivobjekte existieren.
-   Spätere Archivierung darf diese Fakten nicht doppelt in Bio oder
    Statistik zählen.

### Tahlia Paris

-   `Cyber Girl of the Year` kann als bestätigter Titel bekannt sein,
    obwohl noch nicht geklärt ist, welche konkreten Pictorials diesem
    Titel zuzuordnen sind.
-   Vorhandene Shootings dürfen solange korrekt unter `Individuals`
    bleiben.
-   Ein bestätigter Titel darf nicht erzwingen, dass unbekannte
    Medienbeziehungen erfunden werden.
-   Besonders bei Playboy Plus muss Person → Titel/Programm → Serie →
    Pictorial/Video als voneinander trennbare Wissensebenen behandelbar
    sein.

Für 6.2 zunächst vorgesehen:

-   bestätigte Titel unabhängig von vollständiger
    Pictorial-/Videozuordnung abbilden
-   bestätigte Serien unabhängig von vollständig bekannten Medienzahlen
    abbilden
-   bestätigte Printauftritte abbilden, bevor eine Publikation
    vollständig erschlossen ist
-   bestätigte Video-/Medienauftritte abbilden, bevor das konkrete
    Medienobjekt vollständig archiviert ist
-   unbekannte Anzahl oder Zuordnung ausdrücklich unbekannt lassen statt
    `0` oder Platzhalterobjekte zu erzeugen
-   Herkunft über Research nachvollziehbar halten
-   spätere Verknüpfung mit echten Archivobjekten vorbereiten
-   Doppelzählungen zwischen Fakt und späterem Archivobjekt vermeiden

## Geplanter Folgebaustein: Erschließungsgrad im Model-Profil

Nach dem stabilen 6.2-Faktenmodell soll aus bestätigtem Wissen und
tatsächlicher Archiverschließung ein transparenter Erschließungsgrad
entstehen.

Der Erschließungsgrad soll nicht die Qualität des Archivs bewerten,
sondern zeigen, wie viel des bereits bestätigten Wissens strukturiert
erschlossen ist.

Beispielhafte Teilbereiche:

-   Serien: bestätigt / im Archiv / vollständig zugeordnet
-   Titel: bestätigt / im Archiv / Medienzuordnung geklärt
-   Pictorials: bestätigt / archiviert
-   Videos: bestätigt / archiviert
-   Printauftritte: bestätigt / als Publikation erschlossen
-   offene Detailangaben und unbekannte Zuordnungen

Wichtig:

-   `unbekannt` ist nicht dasselbe wie `0`
-   `bekannt, aber noch nicht archiviert` ist nicht dasselbe wie
    `offen/unbestätigt`
-   der Erschließungsgrad soll später als Grundlage für Filter dienen,
    mit denen gezielt Archivlücken gefunden werden können
-   gewünschte spätere Lückenansicht z. B.
    `Serie bekannt, Medien fehlen`,
    `Titel bestätigt, Pictorial-Zuordnung offen`,
    `Print bestätigt, Publikation noch nicht erschlossen`

## Offene Punkte

-   Research 6.2 Wissens-/Brückenmodell für Titel, Serien, Print und
    Medien
-   Doppelzählungs-/Abgleichlogik zwischen bestätigtem Fakt und späterem
    Archivobjekt
-   Erschließungsgrad im Model-Profil
-   spätere Filter-/Lückenansicht
-   eigene Übersicht und nachträgliche Bearbeitung kanonischer Fakten
-   kontrollierte Übernahme in Stammdaten und weitere bestehende
    Archivbeziehungen
-   automatisch erzeugtes `Auf einen Blick`
-   weitergehende Präzisierung von Galerie/Pictorial, Ausgabe/Issue,
    Collection/Reihe, Bereich/Plattform und Titelprogrammen

### Video-Fullscreen

Weiterhin offen, aktuell kein Arbeitsschwerpunkt. Bevorzugter
zukünftiger Ansatz: eigener In-App-Fullscreen statt nativer
WebView-Fullscreen-Umschaltung.

### Kleiner Pull-Down-Effekt

Niedrige Priorität.

Verworfene Ansätze:

-   `overscroll-behavior-y:none` als gezielter Lösungsversuch
-   `WebView.setOverScrollMode(View.OVER_SCROLL_NEVER)`
-   JavaScript-Abfangen von `touchmove`
-   `<main>` als eigener Scrollcontainer

## Dinge, die NICHT wiederholt werden sollten

-   Kein globales `WindowCompat.setDecorFitsSystemWindows(false)`.
-   Keine optionalen Plugin-Aufrufe wie `plugin?.method?.().catch(...)`,
    wenn die Methode möglicherweise nicht existiert.
-   Kein `main{height:100vh;overflow-y:auto;...}` als eigener
    Haupt-Scrollcontainer.
-   Kein globaler Ein-Finger-`touchmove`-Handler mit `preventDefault()`.
-   Kein nativer Video-Fullscreen.
-   Kein Rücksetzen von `DATA_SCHEMA_VERSION` unter den vorhandenen
    Datenstand.
-   Keine künstlichen Shootings/Medienbeziehungen nur deshalb anlegen,
    damit bestätigtes Wissen im Archiv sichtbar wird.

## Zuletzt abgeschlossener Arbeitsblock

Research 6.1.1 -- sprachliche Verfeinerung des Bio-Generators:

-   Research 6 führte den kontrollierten Bio-Entwurf ein.
-   Research 6.1 machte Playboy- und Karriereabsätze konkreter, nannte
    Serien und unterdrückte redundante Datumsangaben.
-   Research 6.1.1 bündelt mehrere gleichartige Karrierefakten zu
    natürlicher Prosa.
-   Der Generator bleibt strikt ein Entwurf; keine automatische Änderung
    oder Speicherung der Kurzbio.
-   Schema bleibt `4`.
-   Gerätetest erfolgreich.
-   Der Benutzer bestätigte am 2026-09-06, dass die neue Kurzbio-Fassung
    gefällt.

Der fachlich getestete Code-Stand ist Commit
`1a4a0038cfc3ae634b749f6b1c0979fa20e2dd80`.

## Letzter sinnvoller nächster Schritt

Vor der nächsten funktionalen Änderung:

1.  diese aktualisierte `STATUS.md` als vollständige Ersatzdatei
    hochladen,
2.  anschließend `main` erneut prüfen,
3.  erst dann Research 6.2 klein und isoliert beginnen.

Research 6.2 soll zunächst die Datenstruktur und kontrollierte Übernahme
für bestätigtes, aber noch nicht vollständig archiviertes Wissen lösen.
Erschließungsgrad und Lückenfilter folgen erst auf dieser stabilen
Grundlage.

## Pflegehinweis

Nach jeder abgeschlossenen funktionalen Änderung:

-   prüfen, ob diese Datei angepasst werden muss
-   Referenz-Commit auf den zuletzt fachlich berücksichtigten Code-Stand
    setzen
-   offene bzw. nächste Schritte korrigieren
-   Sitzung erst danach als abgeschlossen betrachten
