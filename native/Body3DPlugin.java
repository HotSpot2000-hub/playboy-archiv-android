package de.playboy.archiv;

import android.content.res.AssetFileDescriptor;
import android.os.Debug;
import android.os.SystemClock;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

/** Local synthetic and single-photo tests. No network, archive writes or saved results. */
@CapacitorPlugin(name = "Body3D")
public class Body3DPlugin extends Plugin {
    private static final int VERTICES = 18439;
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean photoJob = new AtomicBoolean(false);
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    @PluginMethod
    public void analyzePhoto(PluginCall call) {
        String encoded = call.getString("tensor");
        JSObject input = call.getObject("input");
        if (encoded == null || encoded.length() > 850000 || input == null) {
            call.reject("Die lokalen Bilddaten fehlen oder sind zu groß."); return;
        }
        if (!busy.compareAndSet(false, true)) {
            call.reject("Ein 3D-Test läuft bereits."); return;
        }
        cancelled.set(false);
        photoJob.set(true);
        try {
            worker.execute(() -> {
                long start = SystemClock.elapsedRealtime();
                try {
                    checkActive();
                    float[] cliffValues = photoCondition(input);
                    byte[] raw = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT);
                    if (raw.length != 3 * 224 * 224 * 4) throw new IllegalStateException("Ungültige Bildtensorgröße.");
                    float[] pixels = new float[3 * 224 * 224];
                    ByteBuffer.wrap(raw).order(java.nio.ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(pixels);
                    for (float value : pixels) if (!Float.isFinite(value) || value < -3 || value > 3) {
                        throw new IllegalStateException("Ungültige normalisierte Bilddaten.");
                    }
                    JSONObject manifest = readModelManifest();
                    if (manifest.optInt("photo_revision", 0) != 1 || manifest.optInt("decoder_points_revision", 0) != 1 || manifest.optInt("diagnostic_revision", 0) != 3
                            || !"float32".equals(manifest.optString("image_compute", ""))) {
                        throw new IllegalStateException("Für den Fototest fehlen die neuen Modellinformationen in dieser APK.");
                    }
                    android.system.Os.setenv("ORT_DISABLE_TELEMETRY", "1", true);
                    OrtEnvironment env = OrtEnvironment.getEnvironment();
                    env.setTelemetry(false);
                    float[] shape, pose, camera;
                    JSONArray joints = new JSONArray();
                    JSONArray joints3d = new JSONArray();
                    ByteBuffer imageModel = mapAsset("instanthmr.onnx");
                    try (OrtSession.SessionOptions opts = options();
                         OrtSession session = env.createSession(imageModel, opts);
                         OnnxTensor image = OnnxTensor.createTensor(env, FloatBuffer.wrap(pixels), new long[]{1, 3, 224, 224});
                         OnnxTensor cliff = OnnxTensor.createTensor(env, FloatBuffer.wrap(cliffValues), new long[]{1, 3})) {
                        Map<String, OnnxTensor> feeds = new HashMap<>(); feeds.put("image", image); feeds.put("cliff_cond", cliff);
                        checkActive();
                        try (OrtSession.Result result = session.run(feeds)) {
                            shape = vector(result, "shape_params", 45); pose = vector(result, "mhr_params", 204);
                            camera = vector(result, "cam_trans", 3);
                            Object value = result.get("joints_2d").orElseThrow(() -> new IllegalStateException("Bildpunkte fehlen.")).getValue();
                            if (!(value instanceof float[][][])) throw new IllegalStateException("Ungültige Bildpunkte.");
                            float[][][] points = (float[][][]) value;
                            if (points.length != 1 || points[0].length != 70) throw new IllegalStateException("Ungültige Bildpunktzahl.");
                            for (float[] point : points[0]) {
                                if (point.length != 2 || !Float.isFinite(point[0]) || !Float.isFinite(point[1])) throw new IllegalStateException("Ungültiger Bildpunkt.");
                                joints.put(new JSONArray().put((double) point[0]).put((double) point[1]));
                            }

                        }
                    }
                    imageModel = null;
                    checkActive();
                    if (camera[2] <= .01f) throw new IllegalStateException("Die Kamera-Schätzung ist ungültig. Bitte den Personenrahmen prüfen.");
                    ByteBuffer meshModel = mapAsset("mhr_decoder.onnx");
                    ByteBuffer vertices = ByteBuffer.allocate(VERTICES * 3 * 4).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    try (OrtSession.SessionOptions opts = options(); OrtSession session = env.createSession(meshModel, opts)) {
                        checkActive();
                        float[][][] mesh = decode(env, session, shape, pose, joints3d);
                        if (mesh.length != 1 || mesh[0].length != VERTICES) throw new IllegalStateException("Ungültige Netzgröße.");
                        for (float[] vertex : mesh[0]) {
                            if (vertex.length != 3) throw new IllegalStateException("Ungültiger Netzpunkt.");
                            for (float v : vertex) if (!Float.isFinite(v)) throw new IllegalStateException("Ungültiger Netzpunkt.");
                            // Official MHR frame: cm, Y-up/Z-back -> metres, Y-down/Z-forward.
                            vertices.putFloat(vertex[0] / 100f); vertices.putFloat(-vertex[1] / 100f); vertices.putFloat(-vertex[2] / 100f);
                        }
                    }
                    checkActive();
                    byte[] faces = verifiedFaceBytes(manifest);
                    JSObject result = new JSObject();
                    result.put("vertices", android.util.Base64.encodeToString(vertices.array(), android.util.Base64.NO_WRAP));
                    result.put("faces", android.util.Base64.encodeToString(faces, android.util.Base64.NO_WRAP));
                    result.put("vertexCount", VERTICES); result.put("faceCount", faces.length / 6);
                    result.put("camera", new JSONArray().put((double) camera[0]).put((double) camera[1]).put((double) camera[2]));
                    result.put("joints2d", joints); result.put("joints3d", joints3d); result.put("pointDiagnosticRevision", 1); result.put("pointSource", "decoder_skeleton"); result.put("coordinateFrame", "vision_metres");
                    result.put("elapsedSeconds", (SystemClock.elapsedRealtime() - start) / 1000.0);
                    checkActive(); call.resolve(result);
                } catch (OutOfMemoryError error) {
                    call.reject("Für den Einfoto-Test war zu wenig freier Arbeitsspeicher verfügbar.");
                } catch (Exception error) {
                    String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
                    call.reject("3D-Foto: " + message.substring(0, Math.min(550, message.length())));
                } finally { photoJob.set(false); busy.set(false); }
            });
        } catch (RuntimeException error) {
            photoJob.set(false); busy.set(false); call.reject("Der Einfoto-Test konnte nicht gestartet werden.");
        }
    }

    @PluginMethod
    public void cancelPhoto(PluginCall call) {
        if (photoJob.get()) cancelled.set(true);
        call.resolve(new JSObject());
    }

    private static JSONArray photoPoints3d(float[][][] points) throws Exception {
        if (points.length != 1 || points[0] == null || points[0].length != 70) throw new IllegalStateException("Ungültige 3D-Körperpunktzahl.");
        JSONArray result = new JSONArray();
        for (float[] point : points[0]) {
            if (point == null || point.length != 3) throw new IllegalStateException("Ungültiger 3D-Körperpunkt.");
            JSONArray row = new JSONArray();
            for (float v : point) {
                if (!Float.isFinite(v)) throw new IllegalStateException("Ungültiger 3D-Körperpunkt.");
                row.put((double) v);
            }
            result.put(row);
        }
        return result;
    }

    private static float[] photoCondition(JSONObject input) throws Exception {
        int width = input.getInt("width"), height = input.getInt("height");
        if (width < 16 || height < 16 || width > 1600 || height > 1600) throw new IllegalStateException("Ungültige Fotogröße.");
        JSONArray box = input.getJSONArray("box");
        if (box.length() != 4) throw new IllegalStateException("Personenrahmen fehlt.");
        double x1 = box.getDouble(0), y1 = box.getDouble(1), x2 = box.getDouble(2), y2 = box.getDouble(3);
        if (!Double.isFinite(x1) || !Double.isFinite(y1) || !Double.isFinite(x2) || !Double.isFinite(y2)
                || x1 < 0 || y1 < 0 || x2 > width || y2 > height || x2 - x1 < 8 || y2 - y1 < 8
                || Math.max(x2 - x1, y2 - y1) < 64) throw new IllegalStateException("Bitte eine einzelne Person mit ausreichend großem Rahmen wählen.");
        return new float[]{(float) ((x1 + x2) / width - 1), (float) ((y1 + y2) / height - 1),
            (float) (Math.max(x2 - x1, y2 - y1) / Math.max(width, height))};
    }

    private byte[] verifiedFaceBytes(JSONObject manifest) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream input = getContext().getAssets().open("body3d/mesh-faces.u16")) {
            byte[] chunk = new byte[8192]; int count;
            while ((count = input.read(chunk)) != -1) {
                if (bytes.size() + count > 300000) throw new IllegalStateException("Ungültige Netzflächengröße.");
                bytes.write(chunk, 0, count);
            }
        }
        byte[] result = bytes.toByteArray();
        JSONObject expected = manifest.getJSONObject("mesh-faces.u16");
        byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(result);
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) hex.append(String.format(Locale.ROOT, "%02x", b & 255));
        if (result.length != 36874 * 3 * 2 || result.length != expected.getInt("bytes")
                || !hex.toString().equals(expected.getString("sha256"))) throw new IllegalStateException("Netzflächen stimmen nicht mit dem Körpermodell überein.");
        java.nio.ShortBuffer indices = ByteBuffer.wrap(result).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        while (indices.hasRemaining()) if ((indices.get() & 65535) >= VERTICES) throw new IllegalStateException("Ungültiger Netzflächenindex.");
        return result;
    }

    @PluginMethod
    public void runSyntheticTest(PluginCall call) {
        if (!busy.compareAndSet(false, true)) {
            call.reject("Ein 3D-Test läuft bereits.");
            return;
        }
        cancelled.set(false);
        try {
            worker.execute(() -> {
                long start = SystemClock.elapsedRealtime();
                String stage = "Start";
                try {
                    checkActive();
                    // Full opt-out must precede ORT class/environment initialization.
                    android.system.Os.setenv("ORT_DISABLE_TELEMETRY", "1", true);
                    OrtEnvironment env = OrtEnvironment.getEnvironment();
                    env.setTelemetry(false);
                    float[] shape;
                    float[] pose;
                    long sampledPssKb;
                    // Close the image session before opening the mesh decoder.
                    stage = "Bildmodell";
                    ByteBuffer imageModel = mapAsset("instanthmr.onnx");
                    try (OrtSession.SessionOptions options = options();
                         OrtSession session = env.createSession(imageModel, options);
                         OnnxTensor image = OnnxTensor.createTensor(env,
                             FloatBuffer.wrap(new float[3 * 224 * 224]), new long[]{1, 3, 224, 224});
                         OnnxTensor cliff = OnnxTensor.createTensor(env,
                             FloatBuffer.wrap(new float[]{0, 0, .8f}), new long[]{1, 3})) {
                        Map<String, OnnxTensor> inputs = new HashMap<>();
                        inputs.put("image", image);
                        inputs.put("cliff_cond", cliff);
                        checkActive();
                        try (OrtSession.Result result = session.run(inputs)) {
                            shape = vector(result, "shape_params", 45);
                            pose = vector(result, "mhr_params", 204);
                            sampledPssKb = Debug.getPss();
                        }
                    }
                    imageModel = null;
                    checkActive();
                    stage = "Referenzdaten";
                    float[][] fixedInputs = readFixedInputs();
                    float[] expectedMesh = readReference();
                    double shapeDelta = maxDifference(shape, fixedInputs[0]);
                    double poseDelta = maxDifference(pose, fixedInputs[1]);
                    Comparison chain;
                    Comparison fixed;
                    ByteBuffer meshModel = mapAsset("mhr_decoder.onnx");
                    try (OrtSession.SessionOptions options = options();
                         OrtSession session = env.createSession(meshModel, options)) {
                        stage = "Gesamtkette";
                        checkActive();
                        chain = compareMesh(decode(env, session, shape, pose), expectedMesh);
                        sampledPssKb = Math.max(sampledPssKb, Debug.getPss());
                        stage = "Decoder mit festen Eingaben";
                        checkActive();
                        fixed = compareMesh(decode(env, session, fixedInputs[0], fixedInputs[1]), expectedMesh);
                        sampledPssKb = Math.max(sampledPssKb, Debug.getPss());
                    }
                    // Keep the original tolerance and both comparisons. Never turn a mismatch
                    // into success merely because the isolated decoder comparison passes.
                    String diagnostic = "Prüfung 3: Decoder/feste Eingaben " + fixed.describe()
                        + "; Bildparameter Form-Delta " + number(shapeDelta)
                        + ", Pose-Delta " + number(poseDelta)
                        + "; Gesamtkette " + chain.describe()
                        + ". Rechenvergleich, keine Körpermaße.";
                    if (fixed.mismatches != 0 || chain.mismatches != 0) {
                        throw new IllegalStateException(diagnostic);
                    }
                    checkActive();
                    JSObject result = new JSObject();
                    result.put("passed", true);
                    result.put("vertices", VERTICES);
                    result.put("elapsedSeconds", (SystemClock.elapsedRealtime() - start) / 1000.0);
                    result.put("sampledProcessPssKb", sampledPssKb);
                    result.put("maxExportDifferenceNativeUnits", fixed.maxError);
                    result.put("chainMaxDifferenceNativeUnits", chain.maxError);
                    result.put("shapeParameterMaxDifference", shapeDelta);
                    result.put("poseParameterMaxDifference", poseDelta);
                    result.put("diagnosticRevision", 2);
                    result.put("scope", "synthetic_cpu_only");
                    call.resolve(result);
                } catch (OutOfMemoryError error) {
                    call.reject("Für den 3D-Test war zu wenig freier Arbeitsspeicher verfügbar.");
                } catch (Exception error) {
                    String message = error.getMessage();
                    if (message == null) message = error.getClass().getSimpleName();
                    if (!message.startsWith("Prüfung 3:")) message = "Prüfung 3 / " + stage + ": " + message;
                    call.reject(message.substring(0, Math.min(message.length(), 600)));
                } finally {
                    busy.set(false);
                }
            });
        } catch (RuntimeException error) {
            busy.set(false);
            call.reject("Der 3D-Test konnte nicht gestartet werden.");
        }
    }


    private JSONObject readModelManifest() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream input = getContext().getAssets().open("body3d/manifest.json")) {
            byte[] chunk = new byte[4096];
            int count;
            while ((count = input.read(chunk)) != -1) {
                if (bytes.size() + count > 32768) throw new IllegalStateException("Referenzmetadaten sind zu groß.");
                bytes.write(chunk, 0, count);
            }
        }
        JSONObject manifest = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        return manifest;
    }

    private float[][] readFixedInputs() throws Exception {
        JSONObject manifest = readModelManifest();
        if (manifest.optInt("diagnostic_revision", 0) != 3
                || !"float32".equals(manifest.optString("image_compute", ""))) {
            throw new IllegalStateException("Für Prüfung 3 fehlt das neue FP32-Bildmodell im APK-Build.");
        }
        JSONObject inputs = manifest.getJSONObject("synthetic_inputs");
        return new float[][]{readVector(inputs.getJSONArray("identity"), 45), readVector(inputs.getJSONArray("pose"), 204)};
    }

    private static float[] readVector(JSONArray values, int length) throws Exception {
        if (values.length() != length) throw new IllegalStateException("Ungültige Referenzparameterzahl.");
        float[] result = new float[length];
        for (int i = 0; i < length; i++) {
            result[i] = (float) values.getDouble(i);
            if (!Float.isFinite(result[i])) throw new IllegalStateException("Ungültiger Referenzparameter.");
        }
        return result;
    }

    private float[] readReference() throws Exception {
        float[] values = new float[VERTICES * 3];
        try (DataInputStream input = new DataInputStream(getContext().getAssets().open("body3d/synthetic-reference.f32"))) {
            for (int i = 0; i < values.length; i++) {
                values[i] = Float.intBitsToFloat(Integer.reverseBytes(input.readInt()));
                if (!Float.isFinite(values[i])) throw new IllegalStateException("Ungültiger Referenznetzpunkt.");
            }
            if (input.read() != -1) throw new IllegalStateException("Ungültige Referenzgröße.");
        }
        return values;
    }

    private static double maxDifference(float[] actual, float[] expected) {
        double maximum = 0;
        for (int i = 0; i < actual.length; i++) maximum = Math.max(maximum, Math.abs((double) actual[i] - expected[i]));
        return maximum;
    }

    private static float[][][] decode(OrtEnvironment env, OrtSession session, float[] shape, float[] pose) throws Exception {
        return decode(env, session, shape, pose, null);
    }

    private static float[][][] decode(OrtEnvironment env, OrtSession session, float[] shape, float[] pose, JSONArray decoderPoints) throws Exception {
        try (OnnxTensor identity = OnnxTensor.createTensor(env, FloatBuffer.wrap(shape), new long[]{1, 45});
             OnnxTensor bodyPose = OnnxTensor.createTensor(env, FloatBuffer.wrap(pose), new long[]{1, 204});
             OnnxTensor expression = OnnxTensor.createTensor(env, FloatBuffer.wrap(new float[72]), new long[]{1, 72})) {
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("identity", identity);
            inputs.put("pose", bodyPose);
            inputs.put("expression", expression);
            try (OrtSession.Result result = session.run(inputs)) {
                if (decoderPoints != null) {
                    Object points = result.get("decoder_joints").orElseThrow(() -> new IllegalStateException("Decoder-Körperpunkte fehlen.")).getValue();
                    if (!(points instanceof float[][][])) throw new IllegalStateException("Ungültige Decoder-Körperpunkte.");
                    JSONArray checked = photoPoints3d((float[][][]) points);
                    for (int i = 0; i < checked.length(); i++) decoderPoints.put(checked.getJSONArray(i));
                }
                Object value = result.get("vertices").orElseThrow(
                    () -> new IllegalStateException("Decoder-Ausgabe fehlt.")).getValue();
                if (!(value instanceof float[][][])) throw new IllegalStateException("Unerwarteter Decoder-Datentyp.");
                return (float[][][]) value;
            }
        }
    }

    private static Comparison compareMesh(float[][][] mesh, float[] expected) {
        if (mesh.length != 1 || mesh[0].length != VERTICES) throw new IllegalStateException("Unerwartete Netzgröße.");
        Comparison result = new Comparison();
        int index = 0;
        for (float[] vertex : mesh[0]) {
            if (vertex.length != 3) throw new IllegalStateException("Ungültiger Netzpunkt.");
            for (float actual : vertex) {
                if (!Float.isFinite(actual)) throw new IllegalStateException("Nichtendlicher Decoder-Netzpunkt.");
                double error = Math.abs((double) actual - expected[index]);
                result.maxError = Math.max(result.maxError, error);
                if (error > .001 + .0001 * Math.abs(expected[index])) {
                    if (result.mismatches == 0) {
                        result.firstIndex = index;
                        result.firstActual = actual;
                        result.firstExpected = expected[index];
                    }
                    result.mismatches++;
                }
                index++;
            }
        }
        return result;
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.6g", value);
    }

    private static final class Comparison {
        int mismatches;
        int firstIndex;
        float firstActual;
        float firstExpected;
        double maxError;

        String describe() {
            if (mismatches == 0) return "OK (max. " + number(maxError) + ")";
            return mismatches + "/55317 abweichend (max. " + number(maxError)
                + ", erster Punkt " + (firstIndex / 3 + 1) + "/Achse " + (firstIndex % 3 + 1)
                + ": " + number(firstActual) + " statt " + number(firstExpected) + ")";
        }
    }

    private OrtSession.SessionOptions options() throws Exception {
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        try {
            options.addConfigEntry("mlas.enable_gemm_fastmath_arm64_bfloat16", "0");
            options.addConfigEntry("optimization.enable_gelu_approximation", "0");
            options.setIntraOpNumThreads(2);
            options.setInterOpNumThreads(1);
            options.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL);
            options.setMemoryPatternOptimization(false);
            options.setCPUArenaAllocator(false);
            return options;
        } catch (Exception error) {
            options.close();
            throw error;
        }
    }

    private ByteBuffer mapAsset(String name) throws Exception {
        checkActive();
        AssetFileDescriptor asset = getContext().getAssets().openFd("body3d/" + name);
        try (FileInputStream input = asset.createInputStream()) {
            long length = asset.getDeclaredLength();
            if (length < 1024 || length > 200L * 1024 * 1024) {
                throw new IllegalStateException("Ungültige 3D-Modelldatei.");
            }
            // APK assets are uncompressed; mmap avoids a second full Java-heap copy.
            return input.getChannel().map(FileChannel.MapMode.READ_ONLY, asset.getStartOffset(), length);
        } finally {
            asset.close();
        }
    }

    private static float[] vector(OrtSession.Result result, String name, int length) throws Exception {
        Object value = result.get(name).orElseThrow(
            () -> new IllegalStateException("Modellausgabe fehlt: " + name)).getValue();
        if (!(value instanceof float[][])) throw new IllegalStateException("Ungültige Modellausgabe.");
        float[][] values = (float[][]) value;
        if (values.length != 1 || values[0].length != length) {
            throw new IllegalStateException("Unerwartete Parameterzahl.");
        }
        for (float v : values[0]) {
            if (!Float.isFinite(v)) throw new IllegalStateException("Ungültiger Modellparameter.");
        }
        return values[0];
    }

    private void checkActive() {
        if (cancelled.get()) throw new IllegalStateException("Test beim Verlassen der App unterbrochen.");
    }

    @Override
    protected void handleOnPause() {
        cancelled.set(true);
    }

    @Override
    protected void handleOnDestroy() {
        cancelled.set(true);
        worker.shutdown();
    }
}
