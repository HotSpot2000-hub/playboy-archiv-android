# Playboy Archiv -- Projektstatus

Stand: 2026-10-23
Referenz-Commit: `ca679774ec88ad40fe6af553cf703afaa9bad796`

> Verbindliche Übergabedatei. Vor neuer Arbeit `AGENTS.md` vollständig lesen und prüfen, ob `main` seit dem Referenz-Commit weitergelaufen ist.

## Verbindlicher Arbeitsablauf

- Vor jeder Änderung `STATUS.md` vollständig lesen, Referenz-Commit mit `main` vergleichen und Änderungen seitdem rekonstruieren.
- Vor Bearbeitung aktuelle Datei aus `main` lesen; nur darauf arbeiten; Änderung klein halten.
- Vollständige Ersatzdatei mit exaktem Repository-Dateinamen liefern.
- Benutzer lädt selbst hoch; danach `main` erneut prüfen.
- Android-APK immer als UPDATE installieren, niemals deinstallieren.
- Vor Gerätetests fachliches Ziel, Änderung, sichtbare Stelle und Erfolgskriterium erklären.

## Aktuelle Übergabe: Bildmodell intern FP16, FP32-Ausführung vorbereitet

Berücksichtigtes main: `ca679774ec88ad40fe6af553cf703afaa9bad796`. Seit Referenz 8b79f79 wurden Preflight, STATUS und native Diagnose hochgeladen und geprüft. **APK-Build 566**, Lauf **39994767028**, war erfolgreich gebaut, auf verpackte Assets/Telemetriesperren geprüft und dauerhaft signiert. Benutzer hat diese APK installiert und Diagnose 2 ausgeführt.

### Gerätetest Build 566 und Eingrenzung

Screenshot: `Decoder/feste Eingaben OK (max. 7.62939e-05)`; Bildparameter Form-Delta **0.0509033**, Pose-Delta **0.0457764**; Gesamtkette **55100/55317** Koordinaten abweichend, max. **1.60893**, erster Punkt 1/Achse 1: **4.70747 statt 5.17596**.

Damit besteht der Decoder den synthetischen Paritätstest auf dem S25, wenn exakt dieselben Eingabewerte verwendet werden. Die Form-/Poseausgabe des Bildmodells unterscheidet sich bereits vor dem Decoder. Das belegt keine Genauigkeit realer Körpermaße. Keine Archivfotos verwendet.

### Tatsächliche Analyse des Bildmodells

- Gepinntes Original-ONNX aus der Build-563-APK untersucht. Trotz Float32-Ein-/Ausgängen rechnet der Graph intern mit Float16: **826 Float16-Initializer**, weitere Float16-Konstanten, Eingabecasts auf Typ 10 und Ausgabecasts zurück auf Float32. Es enthält keine Dropout-/Random-Operatoren.
- Vier identische hiesige CPU-Inferenzen sind bitgleich. Die vollständigen 45 Form- und 204 Poseparameter stimmen hier außerdem exakt mit den Referenzparametern aus dem tatsächlichen CI-Bericht von Build 566 überein. Der Bildmodellunterschied auf dem S25 ist damit bestätigt; die genaue verantwortliche Kernel-/Rundungsstelle wurde nicht auf dem Gerät instrumentiert.
- Interne Float16-Ausführung ist eine konkrete, jetzt gezielt adressierte Ursache für plattformabhängige numerische Unterschiede. Nicht pauschal als Handydefekt, Eingabefehler oder unzureichende Hardware darstellen.

### Vorbereitete Änderung, noch nicht hochgeladen

Flaches ZIP **3D-FP32-Test.zip**, drei vollständige Dateien, keine Gewichte enthalten. Reihenfolge:

1. `body-3d-preflight.yml` nach `.github/workflows/` ersetzen (Revision **6**).
2. `STATUS.md` im Hauptverzeichnis ersetzen.
3. **`Body3DPlugin.java` zuletzt nach `native/` ersetzen.** Erst die APK dieses letzten Uploads verwenden.

Kein neuer Index und keine Änderung von `android-debug-apk.yml` nötig. Kein zusätzlicher manueller Diagnose-Workflow erforderlich: der APK-Build führt die Vorbereitung selbst aus.

### Technische Änderung

- Originaler Download bleibt gepinnt und SHA-256-verifiziert. Daraus wird in CI `instanthmr-fp32.onnx` erzeugt. Als APK-Asset bleibt der bekannte Name `body3d/instanthmr.onnx` erhalten; verpackt wird ausdrücklich die neue FP32-Datei, nicht der ursprüngliche FP16-Download.
- Vorhandene Float16-Tensorwerte werden exakt als Float32 gespeichert. Dies stellt keine verlorene Trainingspräzision wieder her, verändert keine Gewichtswerte und trainiert nichts nach. Entscheidend ist die Umstellung der internen Tensor-/Ausführungstypen und Float16-Casts auf Float32.
- **1020 Tensoren**, **1457 Typdeklarationen**, **18 Cast-Ziele** umgestellt. Initializer, Konstanttensoren, Typdeklarationen und verschachtelte Graphen/Funktionen berücksichtigt. Rekursive abschließende Prüfung verweigert verbleibende Float16-Tensoren, -Typen oder -Casts. ONNX-Graphprüfung bestanden.
- Neue Bildmodelldatei **161.106.320 Bytes**, vorher **80.961.622 Bytes**. APK und installierter Modellbestand werden größer. Native mmap-Größenobergrenze deshalb gezielt von 120 auf 200 MiB angehoben. S25-Arbeitsspeicher und Laufzeit mit FP32 noch nicht gemessen.
- `mlas.enable_gemm_fastmath_arm64_bfloat16=0` und `optimization.enable_gelu_approximation=0` im CPU-Test und auf Android ausdrücklich gesetzt; beides entspricht dem dokumentierten Standard der verwendeten Version. Keine NPU-/GPU-/NNAPI-Ausführung ergänzt.
- Manifest trägt `diagnostic_revision: 3`, `image_compute: float32`. Die bekannten synthetischen CI-Eingabeparameter und die aus dem offiziellen MHR-Decoder erzeugte Netzreferenz werden mit dem neuen FP32-Bildmodell neu bestimmt. Native Seite verweigert alte bzw. nicht als FP32 vorbereitete Metadaten. Ausführliche Fehler beginnen mit **„Prüfung 3“**.
- MHR-Decoderklasse und Körpermodellgleichungen bleiben identisch. **Keine Aufweitung der Toleranz:** weiterhin atol 0,001 und rtol 0,0001. Isolierter Decoder und Gesamtkette müssen bestehen. Keine stillschweigende Freigabe einer Abweichung.
- Telemetrie bleibt vor Bibliotheksinitialisierung gesperrt; automatischer Android-Telemetrieprovider bleibt entfernt. Permanente Signierung, APK-/Fingerprintprüfungen, Datenmodell und UI bleiben unverändert. Keine Medien-Uploads, Profiländerungen, Archivschreibzugriffe, Migration oder Schemaänderung. Modelllizenzen bleiben erhalten; FP32-Umstellung im APK-NOTICE dokumentiert.

### Abgeschlossene Prüfungen und Grenzen

- Exakte ausgelieferte Konvertierungsfunktion auf dem tatsächlichen gepinnten Originalmodell ausgeführt. Für jeden umgestellten Tensor Rückkonvertierung gegen die originalen Float16-Werte geprüft; verbleibende halbe Präzision rekursiv ausgeschlossen; ONNX-Checker bestanden. Konverter mit aktuellen und älteren Protobuf-Deskriptor-APIs kompatibel gemacht und erneut getestet.
- Vollständige lokale FP32-Bildmodell→MHR-ONNX-Kette mit zwei Wiederholungen je Konfiguration ausgeführt: volle Optimierung bei 1/2/4 Threads und ohne Graphoptimierungen bei 2 Threads. Innerhalb jeder Konfiguration bitgleiche Wiederholungen. Alle Netze endlich und korrekt geformt; **alle vier Konfigurationen innerhalb der unveränderten Netzvergleichstoleranz**. Maximaler Unterschied zwischen Konfigurationen: Form 5,96e-7, Pose 1,13249e-6, Netz 7,62939e-5 in nativen Recheneinheiten. Das ist kein S25-Benchmark und keine Körpermaßgenauigkeit.
- Exakter ausgelieferter Konverter nochmals geprüft; erzeugte ONNX-Datei bytegleich mit dem zuvor tatsächlich ausgeführten FP32-Modell.
- YAML, Shell, eingebetteter Python-Code, unveränderte Decoderklasse, richtige Auswahl des FP32-Assets bei Verpackung, Manifestversion/-Präzision und native Grenzwert-/Fehlersperren geprüft. Verpackungszweig mit kleinen Dateien getestet, keine fertige neue APK damit behauptet.
- Native Java-Datei gegen tatsächliche ORT-Android-1.31.0-Klassen und Android-/Capacitor-Stubs typ-/syntaxgeprüft. Vergleichstests auf JVM erneut bestanden: genaue Übereinstimmung, alte Toleranz, Fehlerzählung/erster Punkt, NaN-Abweisung und JSON-Floatkonvertierung.
- Lokaler neuer vollständiger Torch-/APK-Build nicht ausgeführt (Torch hier nicht verfügbar). Die unveränderten Original-MHR-/Export-Paritätsprüfungen und neue FP32-Vorbereitung laufen im nächsten CI-APK-Build. Gerätetest mit FP32 und die Beseitigung der Android-Abweichung sind daher noch offen. Keine voreilige Erfolgsmeldung.

### Verbindlicher nächster Schritt

Drei Dateien in obiger Reihenfolge hochladen. Danach main und APK-Build des letzten Plugin-Uploads prüfen und dessen genaue Buildnummer nennen. APK ausschließlich als **Update** installieren, niemals deinstallieren. Unter Einstellungen den vorhandenen 3D-Techniktest einmal starten. Erwartet wird die bestehende Bestanden-Meldung; falls nicht, vollständigen Text **„Prüfung 3“** auswerten. Build 566 nicht nochmals testen.

Ziel: Stabilität der FP32-Bildmodellrechnung und unveränderte Decoder-Parität auf S25 prüfen. Noch keine realen Körpermaße und keine weitere manuelle Foto-Feldbewertung. Erst nach bestandenem Rechentest folgt ein einzelnes echtes Foto mit korrektem Ausschnitt und räumlicher Darstellung. Tahlia-Gruppen 1/2/6 vs. übrige vorläufig unverändert.

## Historische Übergabe: Gerätetest Build 563, Diagnose 2 bereitgestellt

Berücksichtigtes main: `8b79f792899adfd69f635b6554ec2232be229bd8`. Seit der bisherigen Referenz d68b4fc wurden www/index.html, Preflight-Workflow, STATUS und aktiver APK-Workflow hochgeladen; Änderungen geprüft. APK-Build **563**, Lauf **39961989033**, war erfolgreich, einschließlich 3D-Assetbereitstellung, Android-Kompilierung, tatsächlicher APK-/Manifestprüfung, permanenter Signatur und Upload. Richtiger APK-Artifact: `Playboy-Archiv-Build-563`; `3D-Preparation-563` ist nur der Prüfbericht.

### Beobachtung und noch ungeklärte Ursache

- Benutzer hat Build 563 auf Samsung Galaxy S25 Ultra SM-S938B/DS installiert. Screenshot des gestarteten Tests zeigt: **„Test nicht abgeschlossen: Der 3D-Vergleich weicht von der Referenz ab.“**
- Meldung entsteht nach Ausführung beider Modelle und Lesen der Decoder-Ausgabe. Die bisherige Sammelbedingung unterscheidet weder nichtendliche Werte von Zahlenabweichungen noch Bildmodell- von Decoderabweichungen. Kein vollständiger Erfolg und noch keine Ursachenfeststellung.
- Tatsächliche Modell-/Referenzdateien aus der signierten Build-563-APK hier geprüft, ohne Archivfotos. Fehler auch auf hiesiger CPU mit ORT 1.31.0 reproduziert: maximale Kettenabweichung ca. **0,0210414 in nativen MHR-Recheneinheiten**, 30.198 von 55.317 Koordinaten außerhalb der bisherigen Toleranz. Abschalten von CPU-Arena und Memory Pattern wie auf Android verändert dies nicht; ohne Graphoptimierungen bleibt die Abweichung bestehen (max. ca. 0,0211434; 21.874 Koordinaten außerhalb). Keine dieser Varianten wird als Abhilfe ausgeliefert.
- Abweichung also nicht ausschließlich auf S25 beobachtet. Ihre Ursache und Aufteilung auf Bildmodell/Decoder bleiben offen. Zahlen sind ausschließlich Rechenabweichungen bei künstlicher Eingabe, keine Genauigkeit realer Körpermaße.

### Neue vollständige Ersatzdateien, noch nicht hochgeladen

Flaches Paket **3D-Diagnose-2.zip**, ohne versteckte Unterordner und ohne Modellgewichte:

1. `body-3d-preflight.yml` nach `.github/workflows/` ersetzen. Workflowrevision **5** speichert die exakten synthetischen Form-/Poseparameter des CI-Bildmodells zusätzlich in der vorhandenen APK-Datei `body3d/manifest.json`: `diagnostic_revision: 2`, `synthetic_inputs`. Der Float32→JSON→Float32-Roundtrip wird bitgenau geprüft.
2. `STATUS.md` im Repository-Hauptverzeichnis ersetzen.
3. **`Body3DPlugin.java` zuletzt nach `native/` ersetzen.** Nur den danach erzeugten APK-Build verwenden.

Kein neuer Index und keine Änderung des aktiven APK-Workflows notwendig. Dieser führt automatisch den aktualisierten Preflight aus. Die neuen kleinen Metadaten werden mit der bestehenden Manifestdatei verpackt; vorhandene Gewichts-Fingerprints, Lizenzkopien und APK-Prüfungen bleiben gültig. Zwischenbuilds nicht installieren; den separaten manuellen Preflight nicht zusätzlich starten.

### Aussage der Diagnose 2

- Bildmodell weiterhin mit künstlichem Nulltensor. Meldung zeigt maximale Abweichung seiner Form- und Poseparameter von den gespeicherten CI-Werten.
- Decoder einmal mit tatsächlichen Geräteparametern (Gesamtkette), zusätzlich mit exakt den CI-Parametern (isolierter Vergleich). Beide Netze werden vollständig gegen die in CI aus dem offiziellen Originaldecoder erzeugte Referenz geprüft.
- Meldung beginnt bei einem Fehler mit **„Diagnose 2“**. Sie nennt `Decoder/feste Eingaben`, Form-/Pose-Delta und `Gesamtkette`; bei Abweichungen Zahl betroffener Koordinaten, Maximum und ersten Punkt/Achse mit Ist-/Referenzwert. Frühere Fehler erhalten die betroffene Stufe im Text. Nichtendliche Werte werden ausdrücklich gemeldet.
- **Unveränderte Fehlergrenzen:** `atol=0,001`, `rtol=0,0001`. Beide Vergleiche müssen bestehen. Ein bestandener isolierter Decoder macht eine abweichende Gesamtkette nicht grün. Kein Aufweiten von Toleranzen und kein stillschweigendes Akzeptieren eines Fehlers.
- Bei vollständigem Erfolg weiterhin die bestehende Bestanden-Anzeige. Ein erneuter roter Text mit ausführlicher Diagnose ist ein verwertbares Diagnoseergebnis. Noch keine Zusage, dass die Ursache behoben ist.
- Modellgleichungen, Gewichte, CPU-Ausführungsoptionen, Telemetriesperren vor Laden samt Entfernung des automatischen Providers, Pluginregistrierung, UI und Signierung unverändert. Keine Archivschreibzugriffe, Medien-Uploads, Profiländerungen, Migration oder Schemaänderung.

### Abgeschlossene Prüfungen und Grenzen

- Originalfehler mit tatsächlichen Build-563-Assets unter drei CPU-Konfigurationen reproduziert. Alle ORT-Aufrufe mit `ORT_DISABLE_TELEMETRY=1` vor Import und zusätzlicher API-Abschaltung.
- Neue Java-Datei gegen echte ORT-Android-1.31.0-Klassen, Android-API-JAR und Capacitor-/Os-Stubs typ-/syntaxgeprüft. Neue Vergleichslogik auf JVM getestet: exakte Übereinstimmung, ursprüngliche Toleranz, vollständige Fehlerzählung, erster Punkt/Achse, NaN-Abweisung und exakte JSON-Floatkonvertierung bestanden. Kein neuer Android-Build hier behauptet.
- YAML, Shell und eingebetteter Python-Code geprüft. Decoder-Klasse gegenüber Build 563 byteidentisch; ursprüngliche Grenzen und kombinierte Fehlersperre im neuen Java geprüft.
- Tatsächliche neue `package_assets`-Funktion mit kleinen Verpackungsdateien und realen synthetischen CPU-Parametern getestet: JSON-Roundtrip bitgenau, Feldgrößen 45/204, Metadaten unter dem nativen 32-KB-Limit. Dies ist ein Serialisierungs-/Verpackungstest, keine erneute vollständige Torch-Exportprüfung.
- Dateien waren beim zwischenzeitlichen Ausfall der Arbeitsumgebung erhalten. Prüfungen nach Wiederverfügbarkeit erneut bestanden; ZIP-Inhalte byteweise mit Quelldateien abgeglichen. Die vorherige Sitzung endete vor Erstellung der STATUS-Datei und des ZIP; dies ist jetzt nachgeholt.
- Vollständiger neuer CI-/APK-Build der Revision 5 und Gerätetest Diagnose 2 stehen noch aus. CI führt beim Upload die unveränderten Originaldecoder-/Exporttests sowie die neue Serialisierungsprüfung aus.

### Verbindlicher nächster Schritt

Drei Dateien in obiger Reihenfolge hochladen. Danach main und den Build des letzten Plugin-Uploads prüfen und dessen genaue APK-Buildnummer nennen. Nur diese APK als **Update** installieren, niemals deinstallieren. In Einstellungen einmal den vorhandenen 3D-Techniktest starten und vollständige Diagnose-2-Meldung per Screenshot auswerten. Ziel ist die Trennung von Bildmodell- und Decoderabweichung. Build 563 nicht nochmals testen.

Erst nach geklärter und bestandener Rechenprüfung einen echten Fotoeingang vorbereiten. Keine weitere Feld-für-Feld-Bewertung nötig. Tahlia-Körperstandsgruppen 1/2/6 vs. übrige vorläufig unverändert; weiterhin keine Zentimeter-/Cupschätzungen oder automatischen Profiländerungen.

## Historische Übergabe: Android-3D-Test vorbereitet, Downloadpaket erneuert

Berücksichtigtes main: `d68b4fce48bfd725d74bb6d2ab9b6d2fe7b9d632`. Änderungen seit der früheren Referenz wurden geprüft: separater Diagnose-Workflow Revision 3, STATUS und zuletzt `native/Body3DPlugin.java`. App-Index und APK-Workflow sind noch unverändert. Die vorbereiteten Ersatzdateien sind nur lokal ausgeliefert; es wurde noch keine neue 3D-APK gebaut oder auf dem S25 getestet.

### Aktueller Uploadstand und Downloads

- `native/Body3DPlugin.java` ist auf main angekommen und bytegenau mit der geprüften Datei identisch. Kein erneuter Upload nötig.
- Die übrigen alten Downloadlinks waren wegen nicht mehr vorhandener Arbeitsdateien ungültig. Die Programmdateien wurden wiederhergestellt und ihre SHA-256-Fingerprints stimmen mit der ursprünglichen geprüften Lieferung überein. Keine funktionale Änderung bei dieser Wiederbereitstellung.
- Das neue flache ZIP `3D-Testdateien.zip` enthält alle fünf vollständigen Dateien mit ihren exakten Repository-Dateinamen. Keine versteckten ZIP-Unterordner und keine Modellgewichte. ZIP auf dem Handy entpacken und die enthaltenen Einzeldateien hochladen, nicht die ZIP ins Repository.
- Noch hochzuladen, in dieser Reihenfolge: `index.html` nach `www/`; `body-3d-preflight.yml` nach `.github/workflows/`; `STATUS.md` ins Hauptverzeichnis; **`android-debug-apk.yml` zuletzt nach `.github/workflows/`**.
- Zwischenzeitliche automatische Builds nicht installieren. Erst der letzte APK-Build integriert alle Teile. Der separate manuelle „3D-Techniktest (keine APK)“ muss dafür nicht noch einmal gestartet werden; seine Prüfungen sind im neuen APK-Build eingebunden.

### Bereits tatsächlich geprüfter Modellstand

- GitHub-Preflight Revision 3, Lauf 39862837019 (#3), Commit `25e2f307e8d301637996b482c35b10d913f46480`: alle acht Stufen erfolgreich, vom Benutzer bestätigt. Keine Archivmedien verwendet.
- Kompakter MHR-Decoder überspringt ausschließlich exakt nullbesetzte Blöcke der Posekorrekturmatrix. Alle Formparameter, Gesichtsausdrücke, Gelenke, Skinning und Posekorrekturen bleiben enthalten. Keine Quantisierung und kein Abschneiden kleiner Nichtnullwerte.
- ONNX-Dateigröße jetzt **59.587.507 Bytes** statt 703.789.981 Bytes, rund 92 Prozent kleiner. Dies ist ausdrücklich keine gemessene Verringerung des Android-Arbeitsspeichers.
- Revision 4 vor dem Ausfall der Arbeitsumgebung vollständig lokal ausgeführt: beide öffentlichen Downloads mit Fingerprints, synthetisches Bildmodell, offizieller MHR-Decoder, zwölf Vergleiche der kompakten Nachbildung, ONNX-Export/Graphprüfung, zwölf Runtime-Vergleiche, gesamte Bildmodell→Decoder-Kette gegen Original sowie Android-Assetbereitstellung. Alle neun Stufen bestanden.
- Maximale ONNX-Abweichung der zwölf Vergleichsfälle 0,0000611 in MHR-Zentimetereinheiten. Das belegt ausschließlich Rechenparität, keine Genauigkeit echter Körpermaße.
- Umgebung dieser Prüfung: Python 3.12.14, Torch 2.7.1+cpu, NumPy 2.2.6, ONNX 1.18.0 und ONNX Runtime 1.31.0. Kein S25-Benchmark.

### Android-Integration und Telemetriesperre

- Neue Aktion unter Zahnrad → Einstellungen → **3D-Techniktest · Android** → **3D-Techniktest starten**.
- Verwendet ausschließlich einen künstlichen Null-Bildeingabetensor; prüft anschließend alle 18.439 Netzpunkte gegen die in CI aus dem Originalmodell erzeugte Referenz. Erfolgsmeldung: **„Bestanden: Bildmodell und 3D-Decoder laufen auf diesem Handy.“**, dazu Netzpunktzahl und Dauer. Noch kein echtes Foto, keine Mehrbildrekonstruktion und keine Maßprüfung.
- Ein Auftrag gleichzeitig, separater Worker, zwei CPU-Threads und ein Interop-Thread. Bildmodell-Sitzung wird vor Decoder-Sitzung geschlossen. Modelle werden unkomprimiert aus APK-Assets per mmap geladen, ohne vollständige Java-Bytearray-Kopie. Keine Modellinitialisierung beim normalen App-Start.
- Beim Verlassen der App wird nach der gerade laufenden nativen Stufe abgebrochen. Eine synchrone Inferenz wird nicht mitten in der Berechnung beendet. Ressourcen werden danach geschlossen; der Executor beim Zerstören beendet.
- Zwei frühere weitere Modellläufe wurden von automatischer Sicherheitsprüfung wegen möglicher Microsoft-Telemetrie angehalten. Die spätere API-Abschaltung allein war unzureichend, da ONNX Runtime 1.31.0 den 1DS-Provider schon beim Laden initialisieren kann.
- Daher `ORT_DISABLE_TELEMETRY=1` im Python-Test vor dem Import und auf Android per `android.system.Os.setenv` vor dem ersten `OrtEnvironment`-Zugriff; zusätzlich API-Abschaltung. Fehler bei der Umgebungsvariablen brechen vor ORT ab. Der offizielle Quellcode überspringt mit diesem frühen Opt-out die Telemetrieinitialisierung dauerhaft für den Prozess. Der vollständige lokale Lauf mit früher Sperre bestand.
- Das tatsächliche Android-AAR enthält außerdem den automatisch startenden ContentProvider `ai.onnxruntime.TelemetryInitializer`. Der APK-Workflow entfernt ihn mit einer Manifest-Merge-Regel `tools:node="remove"`. Nach Gradle wird sein Fehlen in den zusammengeführten Manifesten verpflichtend geprüft, sonst wird nicht signiert.
- Geprüfte Quellen: https://github.com/microsoft/onnxruntime/blob/v1.31.0/onnxruntime/core/platform/telemetry_environment.h und https://github.com/microsoft/onnxruntime/blob/v1.31.0/onnxruntime/core/platform/telemetry_1ds.cc . Keine Freigabe für Telemetrie oder Medien-Uploads erteilt.
- Der APK-Build erzeugt und prüft die Gewichte selbst. Ca. 81 MB Bildmodell plus ca. 60 MB Decoder und Runtime vergrößern die APK deutlich. Keine Modelle manuell ins Repository laden. Laufzeit, Spitzenspeicher, Wärme und echter Fotoeingang auf dem S25 bleiben offen. Die an zwei Punkten erhobenen Prozess-PSS-Werte sind keine Spitzenmessung und umfassen nicht alle WebView-Prozesse.
- `native/MainActivity.java` im Repository bleibt unverändert. Der Workflow kopiert sie und ergänzt die Pluginregistrierung nur in der generierten Android-Plattform; der eindeutige Einfügepunkt wird geprüft. Die neue Plugin-Java-Datei wird dort ebenfalls kopiert.
- Vollständige SAM- und MHR/Apache-Lizenzen sowie Änderungshinweise liegen in den APK-Assets; Textkopien sind aus den Einstellungen verlinkt. Modelle behalten ihre jeweiligen Bedingungen.
- Nach dem Build werden Modell- und Referenzdateien direkt in der APK auf unkomprimierte Ablage, Länge und SHA-256 geprüft; Lizenzdateien müssen nativ und im Webbereich vorhanden sein.
- Keine Archivschreibzugriffe, keine Profiländerung, kein Schema-Bump, keine Migration. Permanente Signier-, Umbenennungs- und APK-Uploadschritte unverändert. APK ausschließlich als **Update** installieren, niemals deinstallieren.

### Abgeschlossene Codeprüfungen und Grenzen

- Java-Typ-/Syntaxprüfung vor dem Ausfall gegen echte ORT-Android-1.31.0-Klassen und Android-/Capacitor-Stubs bestanden; Capacitor-8-Schnittstellen mit veröffentlichtem Quellcode abgeglichen. Dies ist kein Gradle-/Android-Build.
- YAML, Shell und eingebettete Python-Blöcke geprüft. Alle drei Inline-JavaScript-Blöcke geprüft; bestehende App-Skripte identisch. Neue Schaltfläche mit Erfolg, Fehler, fehlender Bridge und ungültigem Ergebnis getestet; Knopf danach wieder bedienbar. Keine visuelle Geräteprüfung.
- Generierte Gradle-, Activity- und Manifeständerungen an separaten Dateivorlagen ausgeführt. Nachgelagerte Verpackungsprüfung mit ZIP-Vorlage und negativen Fällen geprüft: komprimierte Modelle oder verbliebener Telemetrie-Provider führen zum Fehler. Das ist kein tatsächlicher Manifest-Merge-/APK-Test.
- Bei dieser Wiederbereitstellung: Programmdatei-Fingerprints gegen die ursprüngliche geprüfte Lieferung abgeglichen, YAML/Shell/Python und JavaScript erneut syntaktisch geprüft, ZIP-Inhalte auf unveränderte Bytes geprüft. Keine erneute Modellinferenz erforderlich oder behauptet.

### Verbindlicher nächster Schritt

Die vier noch fehlenden Dateien in der oben genannten Reihenfolge hochladen. Anschließend main und APK-Build zum letzten Upload prüfen. Nach grünem Build APK als Update installieren und den synthetischen Test einmal bei geöffneter App starten. Erst nach Bestanden-Meldung folgt ein echter Fotoeingang mit korrektem Ausschnitt und räumlicher Darstellung; danach gemeinsame Formanpassung innerhalb bestätigter Körperstände.

Tahlia Paris Shootings 1/2/6 bleiben frühere Ausgangsgruppe; übrige nur vorläufig spätere Gruppe. Keine gruppenübergreifenden Mittelwerte und keine automatischen Profilübernahmen. Es werden bislang keine Zentimeter- oder Cupschätzungen ausgegeben.

## Historische Übergabe: Messquellen-UI und lokaler 3D-Pilot

Berücksichtigtes main: `4fd1e8b9d1aae457171026944571ef6bdbd16137`. App-Code: `aec046c1e2317c75a9a06396f0d5bcce9d82cbb1`, www/index.html Git-Blob `981086297535a76e4b9b3f9651cbda841753c2fa`. Danach wurden nur die separaten Diagnose-Workflows ergänzt/geändert. Die weiter unten stehenden stabilen Funktionsbeschreibungen sind historischer Stand vor diesem UI-Umbau.

### Bestätigter Verlauf

- Speichern nur tatsächlich bearbeiteter Foto-/Videokarten und geänderter Messlinien; unberührte ältere Rollen anderer Fotos erzwingen keine Nachbearbeitung. Gespeicherte Felder auberginefarben, ungespeicherte Änderungen ockerfarben, offene Felder unmarkiert. Detailfelder nur bei Messrollen sichtbar; „Messstelle am Körper“ ersetzt im Formular „Messhöhe“.
- Uploadverzögerung beziehungsweise ältere Builds verursachten zwischenzeitlich Verwirrung. Richtige Index-Datei ist im Repository angekommen; Benutzer bestätigt inzwischen, dass die neue Version funktioniert. Ein vollständiger gesonderter Wiederöffnungs-/Persistenztest wurde nicht ausdrücklich dokumentiert.
- Der Benutzer pausiert weitere mühsame Einzelbewertung und beauftragt einen gemeinsamen messorientierten 3D-Ansatz je Körperstand.
- Tahlia Paris: Shootings 1, 2 und 6 bilden die vom Benutzer vorgegebene frühere Ausgangsgruppe. Alle übrigen sollen wahrscheinlich zur späteren Gruppe gehören; diese bleibt vorläufig. Nummern sind keine Chronologie; keine unbekannten Archiv-IDs erraten oder Gruppen automatisch in vorhandene Daten schreiben.
- Verbindliche Zielplattform: ausschließlich Samsung Galaxy S25 Ultra SM-S938B/DS; kein PC beim Benutzer. Lokale Verarbeitung auf Android, kein Cloud-Upload von Archivfotos freigegeben.
- MeshPose nur statisch untersucht; erforderliche Gewichte laut Projekt erst nach Zugriffsanfrage. Keine Anfrage gesendet. Nicht als fertige Android-Lösung darstellen.
- Konkreter aktueller Kandidat: veröffentlichter InstantHMR-ONNX-Regressor plus vollständiges MHR-Körpermodell. Mobile Ausführung und Maßgenauigkeit bleiben ungeprüft.

### Zwei ausgewertete GitHub-Prüfläufe

1. Lauf 39859798994 (#1), Commit bc9a8f581ee2543f83d18bef58700ad91bccc9d0: Downloads, Bildmodell auf CPU und vollständiger MHR-Decoder bestanden. Körpernetz: 18439 Punkte. Direkter ONNX-Export scheitert an interner Parameterzählung von PyTorch.
2. Lauf 39860927270 (#2), Commit 4fd1e8b9d1aae457171026944571ef6bdbd16137: gleiche Vorstufen bestanden; die neue Exporthülle scheitert am typisierten gefrorenen Untermodul. Diese zweite Hülle war eine fehlerhafte Anpassung des Assistenten.

Keine dieser Fehler ist ein Nachweis einer ungeeigneten Handyhardware. Alle Eingaben waren künstlich; keine Archivbilder wurden verwendet.

### Revision 3: tatsächlich lokal mit Modellen getestet

Vollständige Ersatzdatei: `.github/workflows/body-3d-preflight.yml` aus techniktest-3d-geprueft. Basis ist der aktuelle Workflow auf main, keine App-Datei wird geändert.

- Statt geschachtelter gefrorener ScriptModules verwendet die Revision eine quellcodebasierte Tensor-Nachbildung der offiziellen MHR-v1.0.1-Berechnung. Form, Gesichtsausdruck, vollständige Gelenkhierarchie, Skinning und Posekorrekturen bleiben enthalten. Sparse-Korrekturmatrix wird einmalig in eine äquivalente dichte Matrix umgesetzt.
- Tatsächlich lokal ausgeführt: beide öffentlichen Downloads mit verifizierten SHA-256, synthetische InstantHMR-Inferenz, Original-MHR-Ausführung, Nachbildung gegen Original in zwölf Testfällen, ONNX-Export, ONNX-Graphprüfung, ONNX-Runtime-Ausführung und zwölf numerische Vergleiche sowie Bildmodell→Decoder gegen Original.
- Alle Stufen dieser lokalen Gesamtausführung bestanden. ONNX-Datei: 703789981 Bytes (rund 704 MB dezimal). Dies ist eine relevante noch offene Speicherhürde für Android.
- Maximale absolute Abweichung exportierter Vertices in den zwölf Vergleichsfällen rund 0,0000611 in den MHR-Zentimetereinheiten. Das ist ausschließlich Rechen-/Exportparität mit dem Originalmodell, ausdrücklich keine Genauigkeitsangabe für reale Körpermaße.
- Python-Prüfung mit Python 3.12.14, Torch 2.7.1+cpu, NumPy 2.2.6, ONNX 1.18.0 und ONNX Runtime 1.31.0. Python 3.12 und diese Paketversionen sind auch im Workflow festgelegt. Alte ORT-Versionen brachen in der eingeschränkten lokalen CPU-Erkennung ab; ORT 1.31.0 lief. Das ist kein S25-Benchmark.
- YAML und eingebetteter Python-Code geprüft. Nur manueller Trigger, Repository-Berechtigung contents:read, kein Checkout, keine Archivfotos/Signiersecrets. Nur JSON-/Textbericht als Artifact, keine Modellgewichte. Vollständiger Apache-2.0-Hinweis zum MHR-Adapter ist im Workflow enthalten. InstantHMR-Gewichte behalten ihre separaten Bedingungen.
- Revision 3 wurde noch nicht im Benutzer-Repository gestartet. Dort liegt noch Revision 2. Keine neue Test-APK erstellt; Android-/GPU-/NPU-Ausführung, Wärme, Arbeitsspeicher, echte Bilder, gemeinsame Mehrbild-Form und Umfangsschätzungen sind weiterhin offen. Keine Profiländerung, kein Schema-Bump.

### Historischer nächster Schritt (durch aktuelle Übergabe oben ersetzt)

Benutzer ersetzt die Workflowdatei in `.github/workflows`; danach main erneut prüfen und einen neuen „Run workflow“ auf main auslösen, nicht einen alten Lauf wiederholen. Wenn Revision 3 auch dort besteht, die Modellgröße reduzieren beziehungsweise einen Android-tauglichen Decoderpfad und eine begrenzte Einfoto-Diagnose vorbereiten. Vor jedem Gerätetest Ziel, sichtbare Stelle und Erfolgskriterium erklären. Keine Maße ausgeben, bevor der Oberflächen- und Mehrbildtest überzeugt. Benutzer braucht weder PC noch weitere Feld-für-Feld-Eingabe.

## Historischer gerätetest-bestätigter Stand vor Messlinien-Umbau

- App-Paket: `de.playboy.archiv`
- Schema: `6`
- Aktuell berücksichtigter Code: `355f8714313ce46c748a1ed5de8696136aa49a94`
- Verifizierter SHA-256 von `www/index.html`: `4f31133b885ad6e4020d437d2bf0cc8d53d45827f1ff668c0b3dd7d79e195b96`.
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
- Der vorübergehend ergänzte Verwaltungsfilter `Alle Bereiche` wurde am 13.10.2026 wieder entfernt, weil er die Verwaltungsarbeit unnötig komplizierte. Der Model-Mehrfachfilter und die bereichseigenen Tabs bleiben erhalten.
- Reine Videoinhalte besitzen jetzt automatisch erzeugte und lokal zwischengespeicherte 2:3-Vorschauen. Vorhandene Fotovorschauen haben immer Vorrang und werden nicht ersetzt.
- Die automatische Videovorschau prüft mehrere Zeitpunkte, verwirft dunkle beziehungsweise inhaltsarme Kandidaten und richtet den 2:3-Ausschnitt anhand verfügbarer Gesichts- oder Personenflächen-Indizien aus. `Bed Time` und `Beach Comber` wurden dadurch auf dem Android-Gerät deutlich aussagekräftiger dargestellt.
- Reine Videoinhalte besitzen zusätzlich die gerätetest-bestätigte manuelle Aktion `Vorschau festlegen`. Videozeitpunkt und horizontaler 2:3-Ausschnitt können bewusst gewählt und dauerhaft gespeichert werden; die automatische Vorschau überschreibt diese Auswahl nicht.
- `Nicht zugeordnet` wird in Übersicht und Modelprofil als virtueller Rubrikenbereich angezeigt, sofern tatsächlich kontextlose Beiträge vorhanden sind. Es wird dabei kein synthetisches Rubrikobjekt gespeichert.
- Körpermaße werden im Modelprofil kompakt in der Reihenfolge `metrische Körpermaße • ursprüngliche Angabe • europäische BH-Größe` dargestellt. Bestätigtes Beispiel: `94-58-86 • US 32DD-23-34 • EU 70E-58-86`.
- Die ursprüngliche Maßangabe bleibt unverändert gespeichert. Das BH-Größensystem kann als US, UK oder EU hinterlegt werden; Bandmaß, Cup und Körpermaße bleiben logisch unterscheidbar. Cupangaben bis mindestens `DDDD` werden verarbeitet, ohne sichtbare Klammern zu benötigen.
- Der erste metrische Wert ist bei vorhandener BH-Angabe ein gerundeter Brustumfang-Mittelwert; die spätere Analyse darf intern weiterhin einen Bereich samt Unsicherheit führen. Bestehende Profilwerte werden nicht automatisch überschrieben.
- Die erste shootingweise On-Device-Körperprüfung ist auf dem Android-Gerät bestätigt. Sie verwendet ML Kit Pose Detection lokal und überträgt weder Fotos noch Analysedaten an einen Server.
- Pro Shooting werden Ganzkörpererkennbarkeit, vollständige Bildfassung, Oberkörperfotos und ungeeignete Fotos gezählt. Die Ergebnisse bleiben am Shooting gespeichert und werden nur verwendet, solange dessen Fotobestand unverändert ist.
- Perspektiven und ähnliche Posen werden heuristisch gruppiert. Die Zusammenfassung führt die Shootings eines einzelnen Models zusammen, ohne Shootings oder Mediendaten zu vermischen.
- Die lokale Konturprüfung mit Selfie-Segmentierung ist technisch funktionsfähig. Direkte automatische Umfangswerte aus Silhouetten erwiesen sich jedoch trotz Koordinaten- und Armüberlagerungskorrektur als unplausibel und werden nicht als Körpermaße angezeigt.
- Für die bewusste manuelle Auswahl stehen alle 96 Geometriefotos bereit. Der aktuelle Stand auf `main` kann Fotos bereits getrennt Brust, Taille und Hüfte zuordnen und hebt perspektivisch ausgewogene Vorschläge hervor. Diese modellweite Auswahl ist noch nicht der endgültige Workflow und soll vor umfangreicher manueller Arbeit durch benennbare Körperstände ersetzt werden.
- Seit Analysestand 6 werden normalisierte Vergleichsgeometrien aus Kopf-, Schulter-, Arm-, Hüft-, Knie-, Knöchel-, Fersen- und Fußpunkten gespeichert. Dadurch können auch Oberkörper- und Nahaufnahmen über gemeinsame Körperpunkte mit Torso- und Ganzkörperaufnahmen verbunden werden.
- Die Vergleichsgeometrien werden ohne Zentimeterwerte zunächst perspektivweise auf Konsistenz geprüft. Ganzkörperanker, Torso-Brücken und Oberkörper-Verknüpfungen verwenden robuste Median-/Abweichungsgrenzen; nicht passend vernetzte Fotos werden zurückgestellt statt durch Ersatzwerte ergänzt.
- Gerätetest Tiffany Ryan: vier Shootings mit 96 Fotos; 96 Geometriefotos, 91 Torsofotos und 36 automatisch erkannte Ganzkörperanker. Die strengere Konsistenzprüfung ergab 24 von 34 stabile Ganzkörperanker, 89 von 91 stabile Torso-Brücken, 69 von 96 verknüpfte Oberkörperaufnahmen, insgesamt 94 vernetzte Fotos und zwei zurückgestellte Ausreißer. Die Grundlage wird als `Stabile Vergleichsgeometrie` ausgewiesen.
- Die bekannte Körpergröße von Tiffany Ryan (155 cm) wurde noch nicht auf die Geometrien übertragen. Es werden weiterhin weder Zentimeterbereiche noch Gewicht oder Cupgröße aus Fotos behauptet und keine vorhandenen Profilangaben verändert.
- Der lokale Videoanalyse-Pilot ist auf dem Android-Gerät bestätigt. Pro Video werden zunächst 24 gleichmäßig verteilte Frames ausschließlich lokal auf Pose, Ganzkörpererkennbarkeit, Perspektive und Vergleichsgeometrie geprüft. Gespeichert werden nur Zeitpunkte und Geometrien, keine zusätzlichen Videokopien und noch keine Maße.
- Die 24 Video-Stichproben sind mit Zeitpunkt und automatischer Einordnung sichtbar. Einzelne Zeitbereiche können bewusst mit jeweils neun zusätzlichen Frames feiner geprüft werden; Ergebnisse bleiben shootingweise gespeichert.
- Gerätetest Tahlia Paris, Shooting Nr. 1: 40 Fotos und das Video `Rustic Charmer.mp4` mit 7:32 Minuten bei 1920×1080. Der Grobscan erkannte in 15 von 24 Frames eine Pose, darunter sechs Ganzkörperansichten; automatisch eingeordnet wurden null frontal, zwei schräg und zehn seitlich.
- Fünf bewusst ausgewählte Video-Feinscans bestätigten viele seitliche und schräge Ansichten, aber keine belastbare frontale Ansicht. Die manuelle Sichtung des Videos bestätigt diese Quellencharakteristik; weitere Feinscans dieses Videos sind vorerst nicht erforderlich.
- Die Fotoanalyse desselben Shootings ergab 19 geeignete Ganzkörperfotos, ein angeschnittenes Ganzkörperfoto, 18 Oberkörperfotos und zwei ungeeignete Fotos. Die automatische Perspektive ordnete null frontal, 16 schräg, 22 seitlich und zwei unklar ein, obwohl die Galerie visuell frontale beziehungsweise nahezu frontale Aufnahmen enthält. Die Perspektivheuristik ist damit ausdrücklich nur ein korrigierbarer Vorschlag.
- Die shootingweise Quellenbilanz ist gerätetest-bestätigt. Für Tahlia Shooting Nr. 1 wurden die Fotos bewusst als `Breite + Ganzkörperskalierung` und das Video als `Tiefe + Schrägansichten` bestätigt. Die App weist dies als `Ergänzende Quellen bestätigt` aus; fehlende Perspektiven werden nicht durch Ersatzwerte aufgefüllt.
- Die shootingweise Messquellen-Beurteilung ist auf dem Android-Gerät bestätigt. Die Automatik zeigt nur noch eine kleine geometrische Vorauswahl und bezeichnet diese ausdrücklich nicht als Direktquelle. Ansicht und Haltung beginnen unbestätigt; eine direkte Messrolle wird erst nach manueller Bestätigung einer aufrechten Haltung und der passenden Ansicht freigeschaltet.
- Die derzeit implementierten Direktrollen sind noch streng an Perspektive und insgesamt aufrechte Haltung gebunden: Vorder-/Rückansicht liefert Breite, Seitenansicht Tiefe und Schrägansicht die räumliche Ergänzung; Rückansichten sind im aktuellen Code für Ober- und Unterbrust gesperrt. Der reale Bildvergleich hat gezeigt, dass diese Sperren fachlich zu pauschal sind und im nächsten Arbeitsblock maßbezogen ersetzt werden müssen.
- Beim ersten Gerätetest wurde `332079_full.jpg` als aufrechte Schrägansicht und die freie Taille zunächst als direkte Schrägquelle bestätigt; Oberbrust und Unterbrust wurden wegen Kleidung und die Hüfte ebenfalls wegen Kleidung ausgeschlossen. Die Taille wurde anschließend wegen erhobener Arme und Rumpfdrehung bewusst auf `nur Vergleich` korrigiert; der erste gespeicherte Zwischenstand `Taille 1` ist daher nicht mehr die aktuelle Beurteilung.
- Nach Korrektur der zu optimistischen Direktrolle von `332079_full.jpg` auf Vergleich und zusätzlicher Bestätigung von `332053_full.jpg` weist die shootingweise Zusammenfassung `Oberbrust 1 Breite`, `Unterbrust 1 Breite`, `Taille 0 Breite · 0 Tiefe · 0 Schräg · 2 Vergleich` sowie `Hüfte 0` aus. Die feste vierzeilige Darstellung je Maß ist auf dem Android-Gerät bestätigt und deutlich übersichtlicher als die frühere Fließdarstellung.
- Feingescannte Videoframes können im aktuellen Stand als eigene Messquellen geöffnet werden. Die App zeigt höchstens acht seitliche oder schräge Kandidaten, erzeugt ihre Vorschaubilder nur temporär aus dem Originalvideo und speichert weder zusätzliche Bilder noch Videokopien. Ansicht, Haltung und Rolle werden wie bei Fotos bewusst manuell bestätigt; gespeicherte Foto- und Videoquellen werden getrennt geführt und in der Shooting-Zusammenfassung gemeinsam gezählt.
- Gerätetest Tahlia Paris, `Rustic Charmer.mp4`: Acht Framevorschauen wurden korrekt geladen. Die technische Auswahl funktioniert, die fachliche Vorauswahl ist aber noch zu großzügig: ähnliche Frames derselben Sequenz, sitzende/kniende Posen und ein starker Detailausschnitt bei 4:19 wurden angeboten; 4:19 wurde dabei fälschlich als Ganzkörper erkannt. Der Frame bei 6:18 erscheint als aussichtsreichste Seitenquelle. Es wurden bewusst noch keine Video-Messrollen gespeichert.
- Keine automatische Massenmigration; kein Schema-Bump.

## Körperangaben und Analyse -- festgelegte Zielstruktur

- Verbindliche Kette: `Model → Körperstand/Messgruppe → ausgewählte Shootings → beurteilte Messfotos → interne Analyse → bestätigte kompakte Profilzeile`.
- Ein Körperstand fasst ausschließlich bewusst ausgewählte Shootings zusammen. Mehrere Körperstände desselben Models werden nicht gemittelt; dies ist insbesondere für Lisa und Tahlia wegen erkennbarer körperlicher Veränderungen erforderlich.
- Körperstände sind individuell benennbar, beispielsweise `Frühe Shootings`, `Spätere Shootings`, `Vor 2008` oder `Shootings 1–3`. Reihenfolge und Sichtbarkeit im Profil sollen manuell steuerbar sein.
- Da Shootingdaten häufig fehlen, sollen Körperstände standardmäßig neutral und automatisch anhand der bestätigten relativen Abfolge benannt werden, etwa `Früherer`, `Mittlerer` und `Späterer Körperstand`; dies ist keine behauptete Datierung. Bei unklarer Reihenfolge dienen `Körperstand A/B/C` als Rückfall. Manuelles Umbenennen bleibt für Ausnahmefälle möglich.
- Jeder Körperstand besitzt eigene Messfoto-Beurteilungen und eigene Ergebnisse. Ein Shooting darf bei fachlichem Bedarf auch allein ausgewertet werden.
- Die automatische Perspektive ist nur ein Vorschlag. Pro Foto müssen Ansicht (`vorne`, `hinten`, `seitlich`, `schräg`, `unklar`) und Haltung (`aufrecht`, `liegend`, `kniend`, `vorgebeugt`, `verdreht`, `andere`) korrigierbar sein.
- Pro Messbereich wird die Rolle eines Fotos ausdrücklich festgelegt: `Breite`, `Tiefe`, `Breite + Tiefe/schräg`, `nur Vergleich`, `nicht verwenden` oder Ausschluss wegen Kleidung, Armen/Händen, Haltung beziehungsweise anderem Grund.
- Die Eignung wird künftig pro Maß und sichtbarer Messlinie beurteilt, nicht pauschal pro Foto oder Gesamthaltung. Entscheidend sind eine zuverlässig bestimmbare Messhöhe und zwei sichtbare äußere Endpunkte; Verdeckungen innerhalb der Linie sind unschädlich, solange sie Endpunkte und Messhöhe nicht verfälschen.
- Stehen, Sitzen oder Knien sind keine automatischen Ausschlussgründe. Für jedes Maß wird zusätzlich festgehalten, ob die sichtbare Linie neutral, gestreckt, gestaucht, seitlich gebeugt, verdreht oder belastet ist. Daraus folgt die Einstufung `direkte Messlinie`, `korrigierbare Messlinie`, `nur Vergleich` oder `nicht nutzbar`.
- Rückansichten können direkte Breiteninformation liefern. Unterbrust, Taille und Hüfte sind bei sichtbaren Endpunkten grundsätzlich nutzbar; auch die Oberbrust kann aus einer exakt ausgerichteten Rückansicht Breite liefern, wenn ihre seitlichen Konturen sichtbar über die Unterbrust hinausragen. Eine leichte Schrägstellung macht die Quelle nicht wertlos, sondern querprüfungsbedürftig.
- Haltungseinflüsse werden nicht mit pauschalen Standardkorrekturen geschätzt. Neutrale und beeinflusste Fotos beziehungsweise benachbarte Videoframes desselben Körperstands werden über gemeinsame Geometrien verglichen, um Richtung und Größenordnung von Stauchung, Streckung oder perspektivischer Verzerrung zu bestimmen.
- Relevante Beispiele: Sitzen/Hocken kann die Hüfte verbreitern und ihre Tiefe verzerren; Strecken beziehungsweise erhobene Arme können Oberbrust, Unterbrust und Taille verändern; seitliches Beugen staucht eine Taillenseite und streckt die andere; Drehung vermischt sichtbare Breite und Tiefe. Die tatsächliche Nutzbarkeit wird durch Quercheck entschieden.
- Brust wird intern in Oberbrust und Unterbrust getrennt. Erst aus beiden Bereichen darf zusammen mit dem gewählten Größensystem ein möglicher Band-/Cupbereich abgeleitet werden. Eine veröffentlichte Angabe wie `32DD` gilt nicht als sicherer Messwert.
- Interne Analysewerte bleiben differenziert: Bereiche, Mittel-/Bestätigungswert, Differenzen, Unsicherheit, verwendete Fotos, Perspektiven, Kleidung/Verdeckungen, Schwierigkeiten und ursprüngliche Quellenangabe.
- Das Profil zeigt keine Formulierungen wie `möglicherweise` oder breite Analysebereiche. Sichtbar sind ausschließlich ausdrücklich bestätigte eindeutige Archivwerte in der kompakten Form, beispielsweise `94-58-86 • US 32DD-23-34 • EU 70E-58-86`.
- Bei mehreren bestätigten Körperständen erscheinen mehrere kompakte Maßzeilen mit ihrer individuellen Bezeichnung. Bei genau einem Körperstand kann die Bezeichnung entfallen.
- Analysen überschreiben Profilangaben niemals automatisch. Die App schlägt aus dem internen Bereich einen eindeutigen Wert vor; erst die manuelle Bestätigung übernimmt ihn als sichtbaren Archivwert.
- Eine vorhandene Maßangabe wird nur dann zur Korrektur vorgeschlagen, wenn der neue Wert auf ausreichend belastbarer Grundlage plausibler erscheint. Bei unzureichender oder widersprüchlicher Grundlage bleibt die vorhandene Angabe bestehen und wird intern als ungeklärt vermerkt.
- Intern bleiben ursprüngliche Quellenangabe, ermittelter Bereich, vorgeschlagener Archivwert, Abweichung, Qualität und Schwierigkeiten der Datengrundlage sowie die abschließende Entscheidung erhalten.
- Ziel des späteren Standardworkflows ist `Shootings vorsortieren → Körperstände bestätigen → vorhandene Maße prüfen → eindeutige Archivwerte freigeben`. Die aufwendige Einzelprüfung dient jetzt dem Referenzfall und darf nicht zur dauerhaften Hauptaufgabe werden.
- Eindeutige Fälle sollen später automatisch vorbereitet und gesammelt bestätigt werden. Manuelle Detailarbeit ist auf unsichere Perspektiven, widersprüchliche Quellen und wahrscheinliche Körperstandsänderungen zu begrenzen; Profilwerte werden trotzdem nie automatisch übernommen.
- Exakte körperliche Schneidermaße können aus Fotos nicht garantiert werden. Ziel ist eine dokumentierte, möglichst belastbare Plausibilitätsprüfung und ein bewusst bestätigter Archivwert, keine scheinexakte automatische Wahrheit.

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

In der Verwaltung steht der Model-Filter über den Haupttabs. Der zusätzliche globale Auswahlfilter `Alle Bereiche` wurde vorerst wieder entfernt. Archivbereiche bleiben an den jeweiligen Objekten gespeichert und in ihren Bearbeitungsabläufen nutzbar.

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
- Reine Videobeiträge verwenden auch dort die automatisch erzeugte 2:3-Videovorschau und behalten zusätzlich ihre einzelnen ▶︎-Aktionen. Sobald Fotos vorhanden sind, hat die Fotovorschau Vorrang.
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

## Übersicht -- auf die neue Beitragslogik umgebaut und gerätetest-bestätigt

- Die Hauptbereiche der Übersicht sind `Models`, `Titel`, `Rubriken` und `Print`; der frühere sichtbare Bereich `Individuals` ist entfernt.
- `Models` bleibt die fachliche Grundansicht und wird auch bei aktiver Filterung immer als Bereich angeboten. Innerhalb der Ansicht erscheinen nur die Models, die der aktuellen Filterauswahl entsprechen.
- Titel, Rubriken und Printausgaben werden ausschließlich aus den bestätigten Beziehungen `Veröffentlichungskontext → Beitrag → Galerie/Video → Shooting` abgeleitet. Frühere direkte Shooting-Zuordnungen und die alte Individuals-Logik werden nicht erneut als Übersichtsdaten verwendet.
- Titel- und Rubrikenansicht übernehmen die bestätigte Karten- und Modelgruppierung aus der Verwaltung, sind jedoch reine Leseansichten ohne Bearbeiten-, Lösen-, Löschen- oder Hinzufügen-Aktionen.
- Kontextlose Beiträge erscheinen unter `Rubriken → Nicht zugeordnet`; im Modelprofil werden dort ausschließlich die zum geöffneten Model gehörenden Beiträge gezeigt. Der virtuelle Bereich wird nur eingeblendet, wenn passende Beiträge existieren.
- Beitragsvorschauen öffnen weiterhin die reine Fotokarte beziehungsweise einzelne Videos über ihre separaten ▶︎-Aktionen.
- Die Printübersicht zeigt nur tatsächlich vorhandene Reihen und Ausgaben. Leere Reihen werden nicht ausgegeben; Ausgabekarten und die geöffnete Detailansicht sind read-only, während Cover, Artikelvorschauen, Galerien und Videos weiterhin geöffnet werden können.
- Der gemeinsame Archivfilter lässt sich auf Android zuverlässig über `Filter anwenden` bestätigen und schließt anschließend den Filterdialog.
- Freitext wird kontextgenau ausgewertet: Eine Rubrik wie `Busty Babes` zeigt nur die zugehörigen Models und ausschließlich Beiträge dieser Rubrik; fachlich fremde Titel, Rubriken und Printausgaben desselben Models werden nicht mit ausgegeben.
- `Models` bleibt im Navigationsstreifen immer sichtbar. `Titel`, `Rubriken` und `Print` erscheinen bei aktiver Filterung nur, wenn der jeweilige Bereich eigene Treffer enthält; fällt der aktuell geöffnete Bereich weg, wechselt die Übersicht automatisch in einen weiterhin passenden Bereich.
- Die Kombination aus Model-Grundansicht, dynamischer Bereichsauswahl und kontextgenauer Filterung ist auf dem Android-Gerät bestätigt.

## Weitere stabile Funktionen

SAF-Restore; permanente Signierung/Update-Installation; Foto-Pinch-Zoom; Video-Wiedergabe; Models, Titel, Rubriken, Shootings, Galerien, Videos, Beiträge, Medienverwaltung; Archivfilter/Suche/Bewertung/Profilbildfilter; Research mit `careerFacts`, `bioFacts`, `archiveFacts`, Korrekturlogik, Profilfakten, Bio-Generator, Import und Erschließungsgrad.

Research-Löschschutz implementiert, separater Gerätetest noch nicht dokumentiert.

## Risiken / offene Punkte

- Research-Löschschutz separat noch nicht gerätetest-dokumentiert.
- Die automatische 2:3-Videovorschau bleibt ein lokaler heuristischer Best-Effort-Ansatz. Schwierige Videos werden über die inzwischen bestätigte manuelle Aktion `Vorschau festlegen` korrigiert; eine aggressivere Automatik ist nicht vorgesehen.
- Die kompakte metrische Maßdarstellung und die lokale Analysegrundlage sind gerätetest-bestätigt. Die eigentliche Skalierung in Zentimeter und die daraus folgende Plausibilitätsprüfung vorhandener Werte sind noch offen.
- Auch mit 96 Fotos bleiben Körperumfänge aus zweidimensionalen Bildern Schätzbereiche. Perspektive, Pose, Kleidung, Verdeckung und Objektivwirkung müssen als Unsicherheit berücksichtigt werden; keine scheinexakten Körpermaße oder Gewichte ausgeben.
- Die Perspektivklassifikation und Körperpunkte stammen aus lokaler Heuristik beziehungsweise ML Kit und können einzelne Fotos falsch einordnen. Deshalb bleiben robuste Ausreißerbehandlung und manuelle Bestätigung verbindlich.
- Der reale Tahlia-Test zeigt, dass die Heuristik auch eine ganze Galerie ohne frontale Treffer einordnen kann, obwohl visuell frontale beziehungsweise nahezu frontale Fotos vorhanden sind. Quellenrollen und spätere Messrollen dürfen deshalb nie allein aus der automatischen Perspektive abgeleitet werden.
- Bildorientierung und Körperhaltung dürfen nicht verwechselt werden: liegende, kniende, vorgebeugte, verdrehte und rückseitige Posen können trotz automatischer Kennzeichnung `frontal` oder `seitlich` für eine andere Messrolle geeignet oder ungeeignet sein.
- Brustbreite beziehungsweise Brustumfang ist wegen Kleidung, Armen, Dekolleté und unterbrochener Vorderkontur besonders fehleranfällig. Nahaufnahmen dürfen nur über stabile Vergleichsketten skaliert und nicht isoliert vermessen werden.
- Ober- und Unterbrust benötigen getrennte Foto- und Messlinien. BH, Kleidung, Stützwirkung und weiches Gewebe können beide Bereiche unterschiedlich verändern; eine Cupgröße darf nur als systemabhängige Ableitung aus bestätigten Bereichen entstehen.
- Neue Messfoto-Beurteilungen werden shootingweise gespeichert. Die frühere modellweite Auswahl bleibt nur als Altbestand lesbar und darf für Models mit mehreren Körperständen nicht als gemeinsame Messgrundlage verwendet werden. Shootings werden erst nach Einzelprüfung bewusst zu benennbaren Messgruppen beziehungsweise Körperständen zusammengeführt.
- Video-Frames derselben zusammenhängenden Sequenz sind keine voneinander unabhängigen Quellen. Viele ähnliche Frames erhöhen nicht künstlich die Sicherheit; Video dient vor allem zur räumlichen Ergänzung, Bewegungsbeobachtung und Auswahl weniger belastbarer Referenzzeitpunkte.
- Die aktuelle Video-Vorauswahl bewertet Ganzkörpererkennbarkeit und Gesamthaltung noch zu grob. Vor weiterer manueller Zuordnung muss sie starke Ausschnitte, zeitlich nahezu identische Frames und maßbezogen ungeeignete Körperhaltungen besser erkennen beziehungsweise gruppieren; ein einzelnes globales Haltungsurteil darf nicht alle vier Maße sperren.
- Sichtbare Endpunkte sind notwendig, aber nicht allein ausreichend. Pose, Belastung, Perspektive und Objektivwirkung können eine klar sichtbare Linie trotzdem verformen. Solche Quellen bleiben als korrigierbare Linie oder Vergleich erhalten und werden erst nach Quercheck direkt verwendet oder verworfen.
- Der spätere Normalbetrieb darf keine vollständige manuelle Sichtung jeder Galerie und jedes Videos verlangen. Die App muss eindeutige Fälle vorsortieren, wenige geeignete Quellen vorschlagen und dem Benutzer primär Gruppierungs-, Plausibilitäts- und Ausnahmeentscheidungen vorlegen.
- Die Analyse bewahrt Originalangaben. Spätere Ergebnisse erscheinen ausschließlich als nachvollziehbare Bereiche mit Unsicherheit; Profiländerungen erfolgen nur nach ausdrücklicher manueller Bestätigung.
- Die erstmalige automatische Analyse benötigt pro reinem Videoinhalt kurzzeitig mehr Rechenleistung; anschließend wird ausschließlich die gespeicherte Vorschau verwendet. Akkuverhalten bei normaler Nutzung weiter beobachten.
- Print besitzt die gerätetest-bestätigte Grundstruktur **Ausgabe → Artikel → Shooting** einschließlich der fachlichen Artikeldaten und unabhängiger Cover-Fotografen.
- Der Umbau der Übersicht auf die neue Beitrags-, Print- und Bereichsstruktur ist abgeschlossen und gerätetest-bestätigt.
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
- automatische direkte Umfangsberechnung aus frontaler und seitlicher Silhouettenbreite per Ellipsenformel; die realen Tests lieferten trotz Korrekturen unplausible Werte deutlich über dem sinnvollen Messbereich.
- Ganzkörperfotos als zwingende Voraussetzung für jede Teilmessung; gute Oberkörper- und Nahaufnahmen werden stattdessen über gemeinsame Vergleichsgeometrien mit Torso- und Ganzkörperankern verbunden.
- ungeprüfte Skalierung jedes einzelnen Fotos unmittelbar über die Körpergröße; zuerst müssen stabile dimensionslose Vergleichsketten und Ausreißer bestimmt werden.
- gemeinsame Maßauswertung aller Shootings eines Models; bei erkennbar unterschiedlichen Körperständen entstünde ein fachlich falscher Mittelwert.
- automatische Bindung `frontal = Breite` und `seitlich = Tiefe` ohne Korrektur von Ansicht und Haltung.
- sichtbare unsichere Bereiche und Möglichkeitsformulierungen im kompakten Modelprofil; Unsicherheiten bleiben intern, sichtbare Werte werden ausdrücklich bestätigt.

## Historischer nächster Schritt vor dem 3D-Pilot

**Verwaltung, Datenüberführung, Leistungsoptimierung, Übersicht, manuelle Videovorschau, kompakte Maßdarstellung, lokale Foto-/Videoanalyse, shootingweise Quellenbilanz, zweistufige Fotoquellen-Bestätigung und die technische Bereitstellung feingescannter Videoframes sind auf dem Android-Gerät bestätigt. Tiffany Ryans 96 Fotos bilden mit 94 vernetzten Fotos eine stabile Foto-Testgrundlage. Tahlia Paris Shooting Nr. 1 bestätigt zusätzlich lokalen Video-Grobscan, gezielte Feinscans, getrennte Foto-/Videoquellen sowie temporär erzeugte Vorschauen von acht Videokandidaten. Noch wurde kein Videoframe als Messquelle gespeichert.**

Der folgende Arbeitsblock ist mit der oben beschriebenen Ersatzdatei vorbereitet und benötigt noch Upload und Gerätetest: Die bisher pauschalen Direktquellen-Sperren durch eine maßbezogene Bewertung ersetzen. Je Oberbrust, Unterbrust, Taille und Hüfte werden sichtbare Endpunkte, Messlinienart und Haltungseinfluss getrennt erfasst. Rückansichten sowie sitzende oder kniende Posen bleiben nutzbar, wenn die betreffende Linie erkennbar ist; leichte Schrägstellung, Streckung, Stauchung oder Beugung führen zunächst zu einer korrigierbaren Quelle statt zum automatischen Ausschluss. Die Video-Vorauswahl muss zugleich starke Ausschnitte und redundante Frames zurückstellen und pro Sequenz nur wenige fachlich unterschiedliche Kandidaten anbieten.

Anschließend wird Tahlia Paris Shooting Nr. 1 mit wenigen bewusst bestätigten Foto- und Videoquellen ergänzt. Haltungseinflüsse werden über neutrale und beeinflusste Fotos beziehungsweise benachbarte Videoframes desselben Körperstands quergeprüft; es werden keine pauschalen Korrekturprozente angenommen. Aus den bestätigten Quellen entsteht zunächst ausschließlich eine dimensionslose Shooting-Signatur, noch keine Zentimeterwerte.

Danach werden weitere Tahlia-Shootings einzeln analysiert und ihre Shooting-Signaturen paarweise als `wahrscheinlich gleicher Körperstand`, `wahrscheinlich unterschiedlicher Körperstand` oder `unklar` vorsortiert. Der Benutzer bestätigt `zusammenführen`, `getrennt halten` oder `vorläufig unklar`. Erst innerhalb bestätigter Körperstände folgt die Kalibrierung über Tahlia Paris' bekannte Körpergröße 5′4″ beziehungsweise intern 162,6 cm, anschließend die Plausibilitätsprüfung vorhandener Maße. Korrekturen werden nur vorgeschlagen, wenn sie auf belastbarer Grundlage plausibler erscheinen; bis dahin keine sichtbare Zentimeter- oder Cupschätzung und keine automatische Profiländerung.

