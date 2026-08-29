# AGENTS.md

## Zweck

Diese Datei enthält verbindliche Arbeitsregeln für Änderungen am Repository
`HotSpot2000-hub/playboy-archiv-android`.

## Sitzungsstart

Vor jeder inhaltlichen oder technischen Änderung:

1. `STATUS.md` vollständig lesen.
2. Den in `STATUS.md` genannten Referenz-Commit mit dem aktuellen Stand von `main` vergleichen.
3. Falls `main` seit diesem Referenz-Commit weitergelaufen ist:
   - die Änderungen seitdem prüfen,
   - `STATUS.md` nicht ungeprüft als aktuell behandeln,
   - den tatsächlichen Projektstand zuerst rekonstruieren.
4. Erst danach neue Änderungen planen oder umsetzen.

## Verbindlicher Datei-Workflow

Für Änderungen am Projekt gilt grundsätzlich dieser Ablauf:

1. Vor jeder Bearbeitung die aktuell auf `main` liegende Version der betroffenen Datei aus dem Repository lesen.
2. Ausschließlich auf Basis dieser aktuellen Datei arbeiten.
3. Die Änderung möglichst klein und auf den gewünschten Funktionsblock begrenzen.
4. Dem Benutzer die fertig bearbeitete Datei als vollständige Ersatzdatei mit exakt demselben Repository-Dateinamen bereitstellen, z. B. `index.html` oder `STATUS.md`.
5. Keine Patch-Datei und keinen umbenannten Ersatz liefern, wenn nicht ausdrücklich gewünscht.
6. Der Benutzer lädt die fertige Datei anschließend selbst in das Repository hoch bzw. ersetzt dort die bestehende Datei.
7. Nach dem Upload und vor weiteren Änderungen den neuen Stand von `main` erneut prüfen.

Direkte Schreibversuche in das Repository sind nicht der bevorzugte Arbeitsweg.
Der dokumentierte Standard ist: aktuellen Stand lesen → vollständige Ersatzdatei erstellen → Benutzer lädt hoch → neuen Stand prüfen.

## Während der Arbeit

- Den aktuell stabilen Stand nicht unnötig verändern.
- Änderungen klein halten.
- Möglichst nur eine Sache gleichzeitig ändern und testen.
- Bekannte verworfene Ansätze aus `STATUS.md` nicht erneut einführen, ohne dass es dafür einen ausdrücklich neuen Plan gibt.
- Android-APKs immer als Update installieren; die App nicht deinstallieren, damit lokale Archivdaten erhalten bleiben.

## Pflege von STATUS.md

`STATUS.md` ist die verbindliche Übergabedatei zwischen Arbeitssitzungen.

Nach jeder abgeschlossenen funktionalen Änderung muss geprüft werden, ob sich mindestens einer dieser Punkte geändert hat:

- aktueller stabiler Stand
- bereits funktionierende Funktionen
- bekannte Probleme oder Risiken
- offene Punkte
- verworfene Ansätze
- sinnvoller nächster Schritt
- relevante Dateien oder Build-/Installationshinweise
- Arbeitsablauf

Wenn ja, muss `STATUS.md` im selben Arbeitsgang aktualisiert werden.

Eine Arbeitssitzung gilt erst als abgeschlossen, wenn `STATUS.md` den neuen Stand korrekt beschreibt.

## Referenz-Commit

`STATUS.md` enthält oben einen Referenz-Commit. Dieser Commit bezeichnet den Code-Stand, auf den sich die Statusbeschreibung bezieht.

Wichtig:
- Der Referenz-Commit ist nicht zwingend der Commit, der `STATUS.md` selbst aktualisiert.
- Bei der nächsten Sitzung muss geprüft werden, ob `main` hinter diesem Stand weitere Commits enthält.
- Falls ja, müssen diese Änderungen vor der weiteren Arbeit berücksichtigt werden.

## Abschluss einer Sitzung

Vor dem Abschluss:

1. Funktionale Änderungen zusammenfassen.
2. Offene oder nicht getestete Punkte ausdrücklich kennzeichnen.
3. `STATUS.md` aktualisieren.
4. Den Referenz-Commit in `STATUS.md` auf den zuletzt fachlich berücksichtigten Code-Stand setzen.
5. Einen klaren nächsten Schritt hinterlassen.
