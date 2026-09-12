# Playboy Archiv -- Projektstatus

Stand: 2026-09-12\
Referenz-Commit: `125333060f3aa043e150a0e66c84c4418f7bc4c4`

> Verbindliche Übergabedatei. Vor neuer Arbeit `AGENTS.md` vollständig
> lesen und prüfen, ob `main` seit dem Referenz-Commit weitergelaufen
> ist.

## Verbindlicher Arbeitsablauf

-   Vor jeder Änderung `STATUS.md` vollständig lesen, Referenz-Commit
    mit `main` vergleichen und Änderungen seitdem rekonstruieren.
-   Vor Bearbeitung aktuelle Datei aus `main` lesen; nur darauf
    arbeiten; Änderung klein halten.
-   Vollständige Ersatzdatei mit exaktem Repository-Dateinamen liefern.
    Benutzer lädt selbst hoch; danach `main` erneut prüfen.
-   Android-APK immer als UPDATE installieren, niemals deinstallieren.
-   Vor Gerätetests fachliches Ziel, Änderung, sichtbare Stelle und
    Erfolgskriterium erklären.

## Aktueller stabiler Code-Stand

-   App-Paket: `de.playboy.archiv`
-   Schema: `6`
-   Getesteter Referenz-Code: `125333060f3aa043e150a0e66c84c4418f7bc4c4`
-   Etappe 4c samt Folgekorrekturen getestet.
-   Shooting-Identifikation über Model/Model-Kombination +
    Shootingnummer getestet.
-   Visuelle Medienzuordnung zu Galerie/Video getestet.
-   Visuelle Beitragsauswahl in Galerie/Video getestet.
-   `Nicht zugeordnet` als Rubriken-Arbeitsbereich getestet; er zeigt
    Beiträge ohne Rubrik und Galerien/Videos ohne Beitrag.
-   Keine automatische Legacy-Migration; kein Schema-Bump.

## Fachliches Soll-Modell

-   `Shooting = Entstehung`
-   `Galerie/Video = Werk bzw. Medium`
-   `Rubrik/Titel/Printausgabe = Veröffentlichungskontexte`
-   `release = einzelner konkreter Beitrag innerhalb eines Veröffentlichungskontexts`
-   `Seiten = konkrete Fundstelle innerhalb einer Printausgabe`

`release` bleibt technisch zur Bündelung von Galerie/Video wichtig, ist
aber kein sichtbarer konkurrierender Hauptbereich. Rubrik, Titel und
Printausgabe bleiben getrennte Verwaltungs- und Übersichtsbereiche.

Archivbereiche als Ziel: Playboy, Special Editions, Cyber Club, Playboy
Plus. Keine Bereichszuordnung allein anhand Datum; Cyber Club und
Playboy Plus bleiben getrennt.

### Shooting

Entstehungskontext mit Datum/Ort sowie Model(s)/Fotograf(en). Kein
fachlicher Shootingtitel und keine künstlichen Platzhalter.

Identifikation: - überall bei Auswahl/Unterscheidung Model bzw. exakte
Model-Kombination + Shootingnummer; - Muster
`April Katherine • Shooting • Nr. 1`; - Nummernreihe pro exakter
Model-Kombination; - Nummer ist stabile Identifikation, kein Titel; -
Datum/Ort nur Zusatz, nicht erforderlich; - neue Shootings erhalten
feste Nummer; bestehende können ohne Migration sichtbar nummeriert und
beim Bearbeiten fest gespeichert werden; - Löschen soll feste
verbleibende Nummern nicht automatisch umnummerieren.

Gerätetest bestätigt.

### Galerie / Video

Galerie = Foto-Werk/Container; Video fachlich parallel. Beide optional
mit Shooting und Beitrag (`release`). Models, Fotograf(en), Datum und
Ort werden aus Shooting geerbt; Ausschlüsse möglich.

Zielmodell Medien: Foto → Galerie → Shooting; Videodatei → Video →
Shooting. Direkte Medien→Shooting-Zuordnung ist noch vorhanden und erst
nach Abhängigkeitsprüfung zu entfernen.

Visuelle Galerie-/Video-Zielkarten in der Medienverwaltung mit Vorschau
und eindeutiger Shooting-Bezeichnung sind getestet.

### Beitrag (`release`)

Kleinster fachlich sinnvoller Veröffentlichungszusammenhang: - innerhalb
des Veröffentlichungskontexts anlegen, nicht als Haupttyp; -
Galerie/Video sind Medien des Beitrags; - Shooting ist Entstehung; -
Beitrag kann Rubrik, Titel oder beides haben; - individueller
Originalname gehört zum Beitrag; - in geöffneter Rubrik Rubrikname nicht
im Beitragslabel wiederholen; - Model immer sichtbar; - ohne
individuellen Namen: Model + ggf. Nummer; - mit individuellem Namen:
Model + Originalname.

Beispiele: `Tiffany Ryan`, `Tiffany Ryan • Nr. 1`,
`Tiffany Ryan • Nr. 2`, `Tahlia Paris • High Time`,
`Tahlia Paris • Hot Sands`.

Models eines Beitrags werden aus Galerie/Video und deren Shootings
abgeleitet. Der Model-Filter ist nur Filter und keine fachliche Quelle.

Beitragsnummerierung: - einzelner Beitrag derselben
Rubrik/Model-Kombination: keine `Nr. 1`; - ab mindestens zwei Beiträgen
derselben Rubrik und exakt derselben Model-Kombination: `Nr. 1`,
`Nr. 2`, ...; - andere Model-Kombination = eigene Reihe; - individueller
Name wird sichtbar bevorzugt; keine zusätzliche sichtbare Nummer
erforderlich; - interne Nummer darf stabil gespeichert bleiben. - ältere
3e-Werknummerierung bleibt technisch bestehen, ist aber nicht das
endgültige sichtbare Identitätsmodell.

## Rubriken -- stabiler Stand

Hierarchie: Rubrik → Beitrag → Galerie/Video.

-   sichtbarer Haupttab `Veröffentlichungen` entfernt;
-   `release` im normalen Hinzufügen nicht sichtbar;
-   echte Rubrik bietet `＋ Beitrag hinzufügen`; intern entsteht
    `release`;
-   Beitragskarten: links 2:3-Vorschau, rechts Model fett/zentriert,
    darunter individueller Name oder nur bei Bedarf Nummer;
-   Galerie-/Video-Zugriff und `Beitrag bearbeiten` bleiben verfügbar;
-   Galerie/Video → Beitrag zuordnen verwendet visuelle Karten mit
    Vorschau, Model, ggf. Nummer/Name, Rubrik und Werkanzahl; Gerätetest
    bestätigt.

### Nicht zugeordnet

Bleibt innerhalb Rubriken; kein künstliches `series`-Objekt und keine
normale bearbeitbare Rubrik.

Seit Commit `125333060f3aa043e150a0e66c84c4418f7bc4c4` getestet: -
sichtbar als `ARBEITSBEREICH`; - Beiträge ohne Rubrik erscheinen dort; -
Galerien/Videos ohne Beitrag erscheinen dort ebenfalls; - Zähler umfasst
beide Arten; - unzugeordnete Werke als Karten mit Vorschau,
Shooting-Bezeichnung und Werktyp; - kein künstlicher Beitrag und keine
künstliche Rubrik werden erzeugt.

Damit ist der frühere offene Punkt geschlossen, unzugeordnete Medien
auffindbar zu machen, ohne dass der Benutzer technische Zwischenzustände
verstehen muss.

## Titel

Bleiben eigener Bereich. Zielhierarchien: - Coed:
`of the Week → of the Month` - Cyber Girl:
`of the Week → of the Month → of the Year` - Special Editions Model:
`of the Year`

Kein automatisches `Cyber Girl = Cyber Club`; keine Bereichszuordnung
nur anhand Datum. `release` kann optional Titel haben. Titelhierarchien
noch nicht umgesetzt.

## Printausgaben

Konkreter Veröffentlichungskontext mit Ausgabe/Identität, Datum, Cover,
optional PDF und `im Bestand`. PDF und physischer Bestand unabhängig.
Werke bei Print-Wiederverwendung nicht duplizieren. Technischer Typ
`publication` bleibt bis kontrolliertem Umbau.

Unterhalb: Cover, Seite, Seitenbereich. Fundstellen können Models,
Fotograf(en), Rubrik, ggf. Titel, Galerie und Video referenzieren.

## Weitere stabile Funktionen

SAF-Restore; permanente Signierung/Update-Installation; Foto-Pinch-Zoom;
Video-Wiedergabe; Models, Titel, Rubriken, Individuals, Shootings,
Galerien, Videos, Beiträge, Medienverwaltung;
Archivfilter/Suche/Bewertung/Profilbildfilter; Research mit
`careerFacts`, `bioFacts`, `archiveFacts`, Korrekturlogik, Profilfakten,
Bio-Generator, Import und Erschließungsgrad. Research-Löschschutz
implementiert, separater Gerätetest noch nicht dokumentiert.

## Testqualität / Referenzbasis

Kontrollierter Referenztest: - `TEST Anna`, `TEST Bella`; - mehrere
unterscheidbare Shootings; - Galerien mit unterschiedlichen
Vorschauen; - `TEST Rubrik A` mit zwei Beiträgen derselben
Model-Kombination; `Nr. 1` / `Nr. 2` bestätigt.

Dabei gefundene und behobene UX-Probleme: - Shooting-Unterscheidung ohne
künstliches Datum; - visuelle Galerie-/Video-Zielauswahl; - visuelle
Beitragsauswahl; - `Nicht zugeordnet` nicht mehr als scheinbar defekte
normale Rubrik; - Galerie/Video ohne Beitrag verschwindet nicht mehr aus
dem Rubriken-Arbeitsfluss.

Keine erfundenen Datums-/Ortsangaben oder dauerhaften Fake-Beziehungen
in echten Archivdaten.

## Risiken / offene Punkte

-   Direkte Medien→Shooting-Zuordnung vor Entfernung auf Abhängigkeiten
    prüfen.
-   Research-Löschschutz separat noch nicht gerätetest-dokumentiert.
-   Titelhierarchien offen.
-   Printausgaben/Seiten noch nicht auf neues Beitragsmuster umgebaut.
-   ältere 3e-Werknummerierung technisch vorhanden.
-   Play-Protect-Vorfall aus 3g nicht als Source-Code-Kausalität
    behaupten. Play Protect nicht deaktivieren; APK nur als Update.

## Verworfene Ansätze

Nicht ohne neuen ausdrücklichen Plan wieder einführen: -
`Veröffentlichung` als sichtbarer Hauptbereich; - 4b mit separat zu
erstellender Veröffentlichung; - sichtbares Zusammenführen von
Rubrik/Titel/Printausgabe; - `Individual` als separater fachlicher
Haupttyp; - synthetische Rubrik `Nicht zugeordnet`; - künstliche
Datums-/Ortsangaben zur Shooting-Unterscheidung; - Model-Filter als
Beitragsidentität; - Galerie-/Video-Nummer als sichtbare
Veröffentlichungsidentität.

## Nächster Schritt

Kontrollierten Referenztest fortsetzen; **noch keine Ausweitung auf
Titel oder Printausgaben**.

1.  In einer anderen Rubrik einen einzelnen Beitrag mit einer anderen
    Model-Kombination anlegen/zuordnen.
2.  Erwartung: Solange dort für exakt diese Model-Kombination nur ein
    Beitrag existiert, darf **keine `Nr. 1`** sichtbar sein.
3.  Danach einen Beitrag mit individuellem Namen nach Muster
    `TEST Anna • High Time` prüfen; Name sichtbar, keine zusätzliche
    Beitragsnummer nötig.
4.  Erst nach verständlichem Referenztest das Beitrag-im-Kontext-Muster
    auf Titel und danach Printausgaben übertragen.
