# Playboy Archiv -- Projektstatus

Stand: 2026-09-13
Referenz-Commit: `6333e37be646553ca6ec227c195fcf48b1afb260`

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
- Getesteter Referenz-Code: `6333e37be646553ca6ec227c195fcf48b1afb260`
- `www/index.html` Git-Blob: `cbf61b314a0cf5c2218b49b4d66b829b2f201e1b`
- Etappe 4c samt Folgekorrekturen getestet.
- Etappe 5a inklusive 5a.1 und 5a.2 gerätetest-bestätigt.
- Keine automatische Legacy-Migration; kein Schema-Bump.

## Fachliches Soll-Modell

- `Shooting = Entstehung`
- `Galerie/Video = Werk bzw. Medium`
- `Rubrik/Titel/Printausgabe = Veröffentlichungskontexte`
- `release = einzelner konkreter Beitrag innerhalb eines Veröffentlichungskontexts`
- `Seiten = konkrete Fundstelle innerhalb einer Printausgabe`

`release` bleibt technisch wichtig, ist aber kein sichtbarer konkurrierender Hauptbereich. Rubrik, Titel und Printausgabe bleiben getrennte Verwaltungs- und Übersichtsbereiche.

Archivbereiche als Ziel: Playboy, Special Editions, Cyber Club, Playboy Plus. Keine Bereichszuordnung allein anhand Datum; Cyber Club und Playboy Plus bleiben getrennt.

## Shooting

Entstehungskontext mit Datum/Ort sowie Model(s)/Fotograf(en). Kein fachlicher Shootingtitel und keine künstlichen Platzhalter.

Identifikation:
- Model bzw. exakte Model-Kombination + Shootingnummer;
- Muster `April Katherine • Shooting • Nr. 1`;
- Nummernreihe pro exakter Model-Kombination;
- Nummer ist stabile Identifikation, kein Titel;
- Datum/Ort nur Zusatz, nicht erforderlich.

Gerätetest bestätigt.

## Galerie / Video

Galerie = Foto-Werk/Container; Video fachlich parallel. Beide optional mit Shooting und Beitrag (`release`). Models, Fotograf(en), Datum und Ort werden aus Shooting geerbt; Ausschlüsse möglich.

Zielmodell Medien:
- Foto → Galerie → Shooting
- Videodatei → Video → Shooting

Direkte Medien→Shooting-Zuordnung ist noch vorhanden und erst nach Abhängigkeitsprüfung zu entfernen.

Visuelle Galerie-/Video-Zielkarten in der Medienverwaltung mit Vorschau und eindeutiger Shooting-Bezeichnung sind getestet.

## Beitrag (`release`)

Kleinster fachlich sinnvoller Veröffentlichungszusammenhang:
- innerhalb des Veröffentlichungskontexts anlegen, nicht als Haupttyp;
- Galerie/Video sind Medien des Beitrags;
- Shooting ist Entstehung;
- Beitrag kann Rubrik, Titel oder beides haben;
- individueller Originalname gehört zum Beitrag;
- Model immer sichtbar;
- ohne individuellen Namen: Model + ggf. Nummer;
- mit individuellem Namen: Model + Originalname.

Models eines Beitrags werden aus Galerie/Video und deren Shootings abgeleitet. Der Model-Filter ist nur Filter und keine fachliche Quelle.

### Beitragsnummerierung

- Einzelner Beitrag derselben Rubrik/Model-Kombination: keine `Nr. 1`.
- Ab mindestens zwei Beiträgen derselben Rubrik und exakt derselben Model-Kombination: `Nr. 1`, `Nr. 2`, ...
- Andere Model-Kombination = eigene Reihe.
- Individueller Name wird sichtbar bevorzugt; keine zusätzliche sichtbare Nummer erforderlich.
- Interne Nummer darf stabil gespeichert bleiben.

Kontrollierter Referenztest bestätigt.

## Rubriken -- stabiler Stand

Hierarchie: **Rubrik → Beitrag → Galerie/Video**.

- sichtbarer Haupttab `Veröffentlichungen` entfernt;
- `release` im normalen Hinzufügen nicht sichtbar;
- echte Rubrik bietet `＋ Beitrag hinzufügen`; intern entsteht `release`;
- schwarzer Primärbutton oberhalb der Beiträge;
- Beitragskarten mit 2:3-Vorschau, Model, ggf. individuellem Namen/Nummer;
- Galerie-/Video-Zugriff und `Beitrag bearbeiten` verfügbar;
- Galerie/Video → Beitrag verwendet visuelle Auswahlkarten;
- `Nicht zugeordnet` bleibt eigener Arbeitsbereich innerhalb Rubriken und zeigt sowohl Beiträge ohne Rubrik als auch Galerien/Videos ohne Beitrag.

Gerätetest bestätigt.

## Titel -- Etappe 5a stabil

Hierarchie: **Titel → Beitrag → Galerie/Video**.

Die vorhandenen Titel-Tabs bleiben unverändert und sind fachlich bestätigt:
- erste Ebene z. B. `Coed`, `Cyber Girl`, `Playmate`, `SE Model`
- zweite Ebene z. B. `of the Week`, `of the Month`, `of the Year`

### Titelüberschrift

Jeder Titel behält eine eigene sichtbare Überschrift.

Beispiele:
- `Cyber Girl of the Week` / `3. Woche Juni 2001`
- `Cyber Girl of the Month` / `Juni 2001`
- `Cyber Girl of the Year` / `2001`

Darstellung:
- kleiner `🏆` links;
- Pokal vertikal zum zweizeiligen Textblock zentriert;
- Titelname und Zeitraum rechts davon, innerhalb des Textbereichs zentriert;
- Modelname gehört nicht mehr in die Titelüberschrift, weil er aus dem Beitrag hervorgeht.

### Beiträge unter Titeln

- `＋ Beitrag hinzufügen` steht wie bei Rubriken oberhalb der Beiträge und als schwarzer Primärbutton.
- Beitrag wird direkt innerhalb eines Titels angelegt; technischer `release` bleibt im Hintergrund.
- Beitrag zeigt Modelableitung aus Galerie/Video → Shooting.
- individuelle Namen und sichtbare Nummerierungslogik folgen dem bestätigten Rubrikenmuster.
- Galerie/Video lassen sich direkt aus der Beitragskarte öffnen.
- `Beitrag bearbeiten` bleibt verfügbar.
- neuer Button `Lösen` entfernt nur die Beziehung Beitrag ↔ Titel.
- Beim Lösen bleiben Beitrag, Galerie/Video und eine eventuell vorhandene Rubrik-Zuordnung erhalten.

Gerätetest bestätigt.

### Orientierung bei Galerie/Video → Beitrag

Bei der visuellen Beitragsauswahl wird zusätzlich der Titelkontext angezeigt, z. B.:

`Titel: Cyber Girl of the Week · 3. Woche Juni 2001`

Die Rubrikangabe bleibt zusätzlich sichtbar. Hat ein Beitrag sowohl Titel als auch Rubrik, werden beide Kontexte gezeigt.

Gerätetest bestätigt.

### Legacy-Titelzuordnungen

Alte direkte Titel→Shooting-Verknüpfungen werden noch angezeigt unter:

`Bisher direkt zugeordnete Shootings`

Sie werden nicht automatisch migriert oder gelöscht. Einzelne direkte Shooting-Verknüpfungen können weiterhin gelöst werden.

## Titelhierarchien -- noch offen

Bekannte Zielhierarchien:
- Coed: `of the Week → of the Month`
- Cyber Girl: `of the Week → of the Month → of the Year`
- Special Editions Model: `of the Year`

Noch nicht umgesetzt:
- fachliche Hierarchie/Verknüpfung zwischen Week, Month und Year;
- kein automatisches `Cyber Girl = Cyber Club`;
- keine Bereichszuordnung nur anhand Datum.

## Printausgaben

Konkreter Veröffentlichungskontext mit Ausgabe/Identität, Datum, Cover, optional PDF und `im Bestand`. PDF und physischer Bestand unabhängig. Werke bei Print-Wiederverwendung nicht duplizieren.

Technischer Typ `publication` bleibt bis kontrolliertem Umbau.

Unterhalb: Cover, Seite, Seitenbereich. Fundstellen können Models, Fotograf(en), Rubrik, ggf. Titel, Galerie und Video referenzieren.

## Weitere stabile Funktionen

SAF-Restore; permanente Signierung/Update-Installation; Foto-Pinch-Zoom; Video-Wiedergabe; Models, Titel, Rubriken, Shootings, Galerien, Videos, Beiträge, Medienverwaltung; Archivfilter/Suche/Bewertung/Profilbildfilter; Research mit `careerFacts`, `bioFacts`, `archiveFacts`, Korrekturlogik, Profilfakten, Bio-Generator, Import und Erschließungsgrad.

Research-Löschschutz implementiert, separater Gerätetest noch nicht dokumentiert.

## Testqualität / Referenzbasis

Kontrollierter Referenztest mit `TEST Anna`, `TEST Bella`, unterscheidbaren Shootings und Galerien.

Bestätigt:
- Shooting-Unterscheidung ohne künstliches Datum;
- Beitragsnummerierung innerhalb Rubrik/Model-Kombination;
- einzelner Beitrag ohne unnötige `Nr. 1`;
- individueller Beitragsname ohne zusätzliche sichtbare Nummer;
- visuelle Galerie-/Video-Zielauswahl;
- visuelle Beitragsauswahl;
- `Nicht zugeordnet` für Beiträge und Werke;
- Titel → Beitrag → Galerie/Video;
- Titelüberschrift ohne Modeldopplung;
- Titelkontext in der Beitragsauswahl;
- Beitrag vom Titel lösen;
- einheitlicher schwarzer `＋ Beitrag hinzufügen`-Button oberhalb der Beiträge.

## Risiken / offene Punkte

- Direkte Medien→Shooting-Zuordnung vor Entfernung auf Abhängigkeiten prüfen.
- Research-Löschschutz separat noch nicht gerätetest-dokumentiert.
- Titelhierarchien offen.
- Printausgaben/Seiten noch nicht auf neues Beitragsmuster umgebaut.
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
- Galerie-/Video-Nummer als sichtbare Veröffentlichungsidentität.

## Nächster Schritt

**Etappe 5b vorbereiten: Titelhierarchien kontrolliert modellieren.**

Ziel:
- bestehende funktionierende Titelansicht und Tabs unangetastet lassen;
- zunächst fachlich klären, wie `of the Week`, `of the Month` und `of the Year` miteinander verknüpft werden sollen;
- keine automatische Bereichszuordnung aus Datum oder Titelklasse;
- keine Migration ohne getrennten Plan;
- erst nach geklärtem Modell technische Umsetzung beginnen.

Alternativ bleibt als technischer Nebenpunkt die spätere Abhängigkeitsprüfung für direkte Medien→Shooting-Zuordnung offen.
