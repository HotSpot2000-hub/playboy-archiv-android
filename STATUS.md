# Playboy Archiv – Projektstatus

Stand: 2026-09-05  
Referenz-Commit: `fde95f3cffbe154ca2c3c2db1559f567d24cae43`

> Diese Datei ist die verbindliche Übergabedatei zwischen Arbeitssitzungen.
> Vor neuer Arbeit zusätzlich `AGENTS.md` lesen und prüfen, ob `main` seit dem
> Referenz-Commit weitergelaufen ist.

## Verbindlicher Arbeitsablauf

Für Änderungen gilt der in `AGENTS.md` festgelegte Datei-Workflow:

- Vor jeder Bearbeitung zuerst die aktuelle Version der betroffenen Datei aus `main` lesen.
- Nur diese aktuelle Datei bearbeiten.
- Änderungen möglichst klein und gezielt halten.
- Fertige Dateien als vollständige Ersatzdateien mit exakt dem Repository-Dateinamen bereitstellen, z. B. `index.html` oder `STATUS.md`.
- Keine Patches oder umbenannten Ersatzdateien, sofern nicht ausdrücklich gewünscht.
- Der Benutzer lädt die fertige Datei selbst in GitHub hoch bzw. ersetzt dort die bestehende Datei.
- Vor dem nächsten Arbeitsschritt den neuen Stand von `main` erneut prüfen.

Dieser Ablauf ist der bevorzugte Standard und soll auch nach einem Chat- oder Sitzungswechsel beibehalten werden.

## Aktueller stabiler Stand

Die Android-App läuft stabil und wird per GitHub Actions als signierte APK gebaut.

Wichtige Hinweise:
- APKs immer als UPDATE installieren.
- App nicht deinstallieren, damit lokale Archivdaten erhalten bleiben.
- GitHub Actions Workflow: `.github/workflows/android-debug-apk.yml`
- App-Paket: `de.playboy.archiv`

## Wichtige Dateien

- `www/index.html`
  - Hauptoberfläche, Navigation, Archiv- und Model-Logik
  - Foto- und Videoanzeige
  - Filter und Bewertungsdarstellung
  - Research-Bereich und Research-Datenlogik
  - IndexedDB / lokale Datenlogik
  - Datenmodell-Grundlage für `careerFacts` und `bioFacts`
  - schreibgeschützte Anzeige bestätigter Karriere-/Biofakten im Model-Profil
- `native/MainActivity.java`
  - Capacitor BridgeActivity
  - registriert `ArchiveDirectoryPlugin`
- `native/ArchiveDirectoryPlugin.java`
  - SAF-Verzeichniszugriff für Archiv-Restore
- `AGENTS.md`
  - verbindliche Arbeits- und Übergaberegeln

## Was bereits funktioniert

### Archiv
- Archiv-Restore über SAF funktioniert.
- Vollständiger Restore wurde erfolgreich getestet.
- Medien, Shootings und Models werden korrekt wiederhergestellt.
- Keine bekannten beschädigten Datensätze nach Restore.

### Signierung
- Permanente Android-Signierung ist eingerichtet.
- Neue Builds lassen sich als Update über bestehende Installation installieren.

### Fotoanzeige
- Pinch-to-Zoom und Verschieben funktionieren.
- Zoom wird beim Bildwechsel sauber zurückgesetzt.

### Video
- Video-Wiedergabe in Hochkant und Querformat funktioniert.
- Android-Zurück beendet die Videoansicht sauber.
- Nativer Video-Fullscreen bleibt deaktiviert (`controlsList="nofullscreen"`), weil er reproduzierbar Freezes verursachte.

### Navigation / Scrollen
- Rückkehr aus „Zuletzt bearbeitet“ und Shooting-Detail funktionieren.
- Profile aus `Models`, `Titel`, `Serien` und `Individuals` öffnen gezielt oben beim Profilbild.
- Ein-Finger-Scrollen in Bearbeitungskarten funktioniert nach Entfernung des globalen `touchmove`-Blocks.
- Globales Ein-Finger-`touchmove`-Abfangen nicht wieder einführen.
- Kleiner Pull-Down-Effekt am oberen Rand bleibt kosmetisch.

### Model-Übersicht
- Kompakte Karten.
- Titel: nur höchste erreichte Stufe, Gleichstände bleiben.
- Serien: nur Serien mit höchster Shooting-Anzahl, Gleichstände bleiben.
- Sortierung `A–Z` oder `% ↓`; Prozentgleichstände alphabetisch.
- Profilbild-Filter `Alle / ✓ / ✕` funktioniert und ist mit Bewertungssortierung kombinierbar.
- Model-/Profilbildfilter wirken auf Models, Titel, Serien und Individuals.
- Gemeinsame Shootings bleiben sichtbar, wenn mindestens ein sichtbares Model beteiligt ist.
- Gerätetests erfolgreich.

### Archivfilter / freie Suche
- Bottom-Sheet-Dialog stabil.
- Ein-Finger-Scrollen und freie Suche funktionieren.
- `Filter anwenden` und `Filter zurücksetzen` schließen die Karte nicht; `×` schließt.
- Freie Suche wird pro Filterlauf nur einmal berechnet und friert die App nicht mehr ein.
- Gerätetest erfolgreich.

### Kurzbio im Model-Profil
- Eigene klappbare `Kurzbio`-Karte direkt unter den Kerndaten.
- Pro Model als Freitext gespeichert; Absätze und Zwischenüberschriften bleiben erhalten.
- Keine Karte bei leerer Bio.
- Eingeklappt mit Textanfang, weißem Verlauf und `Mehr anzeigen`.
- Darstellung, Bearbeitung, Speichern und Auf-/Zuklappen auf dem Gerät getestet.

### Research
- Eigener Hauptbereich getrennt vom finalen Archiv und Model-Profil.
- Research-Fälle können angelegt, bearbeitet und gelöscht werden.
- Status: `Offen`, `Indizienbasiert`, `Unklar`, `Bestätigt`, `Verworfen`.
- Zuordnung zu Model und optional Shooting.
- Arten: Zuordnung, Datierung, Identifikation, Wiederverwendung, Titel/Auszeichnung, Stammdatum, Sonstiges.
- Frage/Behauptung, Zwischenergebnis, Prüfdatum und strukturierte Einzelbelege werden gespeichert.
- Belege unterstützen `Stützt` / `Widerspricht`, Quelle, Fundstelle, Aussage, Quellenart, Qualität und Prüfdatum.
- Belege können einzeln hinzugefügt und entfernt werden.
- Übersicht zählt stützende und widersprechende Belege.
- Alte freie Research-Notizen und einfache Belege werden normalisiert.
- Allgemeines, nicht modelbezogenes Research wird unterstützt.
- Grundsatz: `Indizienbasiert` ist kein bestätigter Fakt.
- Research Schritt 1 und 2 wurden auf dem Gerät erfolgreich getestet.

### Faktenmodell – Research Schritt 3, Grundstein
- Daten-Schema-Version ist auf `4` angehoben.
- `state.careerFacts` ist als eigene kanonische Sammlung vorbereitet.
- `state.bioFacts` ist als eigene kanonische Sammlung vorbereitet.
- Bestehende gespeicherte Zustände werden beim Normalisieren um fehlende leere Sammlungen ergänzt.
- Bestehende Archiv-, Model-, Research-, Evidence- und Revisionsdaten werden dabei nicht umgebaut.
- Playboy-/Archivfakten werden nicht parallel in diesen Sammlungen dupliziert; vorhandene Archivobjekte und Beziehungen bleiben dafür die kanonische Grundlage.
- App-Start, vorhandene Daten, Speichern und erneutes Öffnen wurden nach Schema-4-Umstellung auf dem Gerät erfolgreich getestet.

### Kontrollierte Research-Übernahme – erster Praxistest
- Bei bestätigten, modelbezogenen Research-Fällen kann eine kontrollierte Übernahme als `Karrierefakt` oder `Biofakt` gestartet werden.
- Vor dem Speichern wird eine konkrete Vorschau des neuen Fakts angezeigt.
- Erst `Übernahme bestätigen` schreibt den Fakt in die kanonische Sammlung.
- Die Herkunft wird über `sourceResearchIds` mit dem Research-Fall verknüpft; Belege und Fundstellen bleiben im Research-Fall und werden nicht in den Fakt kopiert.
- Bereits aus einem Research-Fall erzeugte Karriere-/Biofakten werden am Research-Fall kenntlich gemacht.
- Erster Gerätetest mit Tiffany Ryan / `American Curves: More Lingerie — Summer 2009` erfolgreich.
- Getesteter Karrierefakt: Magazinauftritt, Medium `American Curves`, Werk/Ausgabe `More Lingerie — Summer 2009`, Zeitraum `2009`, Rolle `Model`.
- Die gesicherte Fundstelle `ab Seite 110` bleibt als Herkunfts-/Beleginformation im Research-Fall.
- Die Übernahme und Speicherung funktionieren auf dem Gerät.

### Bestätigte Fakten im Model-Profil
- Bestätigte `careerFacts` und `bioFacts` werden im Model-Profil in einer eigenen klappbaren Karte `Bestätigte Fakten` angezeigt.
- Die Karte erscheint nur, wenn für das Model mindestens ein aktiver kanonischer Fakt vorhanden ist.
- Karriere- und Biografiefakten werden getrennt dargestellt.
- Bei vorhandener Herkunft wird die verknüpfte Research-ID angezeigt.
- Die Ansicht ist bewusst schreibgeschützt; Bearbeiten und Löschen wurden in diesem Schritt nicht ergänzt.
- Model ohne bestätigte Fakten: Karte bleibt ausgeblendet.
- Model mit bestätigtem Fakt: Karte, Inhalt und Auf-/Zuklappen funktionieren.
- Bestehende Kurzbio, Archivansicht und Navigation funktionieren weiterhin.
- Gerätetest erfolgreich.

Fachliche Zielstruktur:
- Research bleibt Belege- und Herkunftsebene.
- Bestätigte Ergebnisse können später kontrolliert in Stammdaten, bestehende Archivobjekte/-beziehungen, Titelvergaben, Karrierefakten oder Biofakten überführt werden.
- `careerFacts` ist für strukturierte Karriere-/Medienfakten außerhalb bzw. ergänzend zur bestehenden Playboy-Archivstruktur vorgesehen.
- `bioFacts` ist für allgemeine biografische Fakten vorgesehen.
- Stammdaten-Anzeige, recherchierter Bestwert und redaktionelle Kurzbio bleiben getrennt.
- Quellen sollen über `sourceResearchIds` auf Research-Fälle zurückverfolgbar sein, statt Belege unnötig zu kopieren.

### Bewertung / Filter
- Fünf bewertete Stufen: `5 = 100%`, `4 = 90%`, `3 = 70%`, `2 = 40%`, `1 = 0%`.
- `Nicht bewertet` bleibt getrennt; erneutes Antippen einer gewählten Stufe löscht die Bewertung.
- Gewichtung: Größe, Gesicht, Busen, Pussy, Eindruck jeweils 20%.
- Größenlogik: 5★ = 5'1"–5'4", 4★ = 5'0"/5'5", 3★ = 4'11"/5'6", 2★ = 4'10"/5'7", 1★ = Rest.
- Gesamtbewertung, Filter und Sortierung verwenden die etablierte gerundete Prozentlogik.
- Bei exakt 100% wird `❤️` angezeigt.
- Gerätetest erfolgreich.

## Offene Punkte

### Research / Faktenmodell – nächste Ausbaustufe

Bewusst noch nicht umgesetzt:
- eigene Übersicht und nachträgliche Bearbeitung von `careerFacts` und `bioFacts`
- kontrollierte Übernahme bestätigter Research-Ergebnisse in bestehende Archivobjekte/-beziehungen oder Stammdaten
- weitergehende Zieltypen und automatische Feldzuordnung für die Research-Übernahme
- weitergehende Präzisierung vorhandener Archivstrukturen für Galerie/Pictorial, Ausgabe/Issue, Collection/Reihe, Bereich/Plattform und Titelprogramme
- Recherche-Import, der mehrere recherchierte Erkenntnisse strukturiert vorbereitet
- Bio-Generator aus bestätigten Fakten
- automatisch erzeugtes „Auf einen Blick“

Fachliche Leitlinie:
- Übersicht und Model-Profil bleiben möglichst final und eindeutig.
- Offene, unklare, indizienbasierte und verworfene Annahmen gehören in Research.
- Ein Indiz, auch ein starkes, ist kein Fakt.
- Erst bestätigte Ergebnisse dürfen kontrolliert in kanonische Daten übernommen werden.
- Verworfene Annahmen bleiben als Forschungshistorie erhalten.
- Keine doppelten Wahrheiten: bestehende Playboy-Archivobjekte/-beziehungen werden weiterverwendet statt als generische Fakten kopiert.
- Eine Kurzbio soll aus bestätigten Fakten entstehen, sichtbare Stammdaten nicht unnötig wiederholen und neben Playboy als Schwerpunkt auch relevante bestätigte Informationen vor, außerhalb und nach der Playboy-Zeit berücksichtigen.
- Eine veröffentlichte/redaktionell bearbeitete Kurzbio darf später nicht ohne ausdrückliche Bestätigung überschrieben werden.

Geplante Reihenfolge:
1. Research 3: Faktenmodell und erste kontrollierte Übernahme in `careerFacts` / `bioFacts`.
2. Research 4: Übernahme-Assistent mit exakter Vorschau und ausdrücklicher Bestätigung, anschließend Erweiterung auf weitere kanonische Zieltypen.
3. Research 5: Recherche-Import zur strukturierten Vorbereitung mehrerer Erkenntnisse.
4. Research 6: Bio-Generator aus bestätigten Fakten mit Entwurf und Freigabe.

### Video-Fullscreen
Weiterhin offen, aktuell kein Arbeitsschwerpunkt.
Bevorzugter zukünftiger Ansatz: eigener In-App-Fullscreen, schwarzer Hintergrund, App-Navigation ausblenden, Android-Zurück sauber behandeln, keine native WebView-Fullscreen-Umschaltung.

### Kleiner Pull-Down-Effekt am oberen Rand
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
- Kein nativer Video-Fullscreen; der reproduzierbare Freeze bleibt der Grund.

## Zuletzt abgeschlossener Arbeitsblock

Die kanonische Faktenebene wurde im Model-Profil sichtbar gemacht und auf dem Gerät erfolgreich getestet:

- neue klappbare Karte `Bestätigte Fakten`
- Anzeige nur bei vorhandenen aktiven Karriere-/Biofakten
- getrennte Bereiche `Karriere` und `Biografie`
- Anzeige der Research-Herkunft über `sourceResearchIds`
- bewusst nur lesend, ohne Bearbeiten/Löschen
- Model ohne Fakten bleibt unverändert ohne zusätzliche Karte
- bestehende Kurzbio, Archivansicht und Navigation bleiben funktionsfähig

Der bereits übernommene Tiffany-Ryan-/American-Curves-Karrierefakt wird damit im Profil als kanonischer Fakt sichtbar.

Der fachlich getestete Code-Stand ist Commit `fde95f3cffbe154ca2c3c2db1559f567d24cae43`.

## Letzter sinnvoller nächster Schritt

Vor der nächsten funktionalen Änderung:
- aktuellen `main`-Stand erneut prüfen,
- aktuelle `www/index.html` aus `main` lesen,
- den stabilen Archiv-/Profilbereich nicht unnötig verändern.

Nächster kleiner Research-Schritt:
- den geplanten Recherche-Import konzipieren und anschließend klein umsetzen,
- Ziel: mehrere recherchierte Erkenntnisse strukturiert vorbereiten, damit sie nicht manuell einzeln zerlegt werden müssen,
- Übernahme in kanonische Fakten weiterhin nur kontrolliert und ausdrücklich bestätigt,
- noch kein automatisches Überschreiben der Kurzbio.

## Pflegehinweis

Nach jeder abgeschlossenen funktionalen Änderung:
- prüfen, ob diese Datei angepasst werden muss,
- Referenz-Commit aktualisieren,
- offenen bzw. nächsten Schritt korrigieren,
- Sitzung erst danach als abgeschlossen betrachten.
