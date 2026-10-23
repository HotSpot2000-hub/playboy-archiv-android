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

/** Isolated CPU self-test. No media access, network, archive writes or saved results. */
@CapacitorPlugin(name = "Body3D")
public class Body3DPlugin extends Plugin {
    private static final int VERTICES = 18439;
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

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
                    ByteBuffer meshModel = mapAsset("mhr_decoder.onnx");
                    double maxError = 0;
                    try (OrtSession.SessionOptions options = options();
                         OrtSession session = env.createSession(meshModel, options);
                         OnnxTensor identity = OnnxTensor.createTensor(env,
                             FloatBuffer.wrap(shape), new long[]{1, 45});
                         OnnxTensor bodyPose = OnnxTensor.createTensor(env,
                             FloatBuffer.wrap(pose), new long[]{1, 204});
                         OnnxTensor expression = OnnxTensor.createTensor(env,
                             FloatBuffer.wrap(new float[72]), new long[]{1, 72})) {
                        Map<String, OnnxTensor> inputs = new HashMap<>();
                        inputs.put("identity", identity);
                        inputs.put("pose", bodyPose);
                        inputs.put("expression", expression);
                        checkActive();
                        try (OrtSession.Result result = session.run(inputs)) {
                            Object value = result.get("vertices").orElseThrow(
                                () -> new IllegalStateException("Decoder-Ausgabe fehlt.")).getValue();
                            if (!(value instanceof float[][][])) {
                                throw new IllegalStateException("Unerwarteter Decoder-Datentyp.");
                            }
                            float[][][] mesh = (float[][][]) value;
                            if (mesh.length != 1 || mesh[0].length != VERTICES) {
                                throw new IllegalStateException("Unerwartete Netzgröße.");
                            }
                            // Compare every coordinate against the original MHR checkpoint,
                            // prepared in CI from the same synthetic regressor input.
                            try (DataInputStream reference = new DataInputStream(
                                    getContext().getAssets().open("body3d/synthetic-reference.f32"))) {
                                for (float[] vertex : mesh[0]) {
                                    if (vertex.length != 3) throw new IllegalStateException("Ungültiger Netzpunkt.");
                                    for (float actual : vertex) {
                                        float expected = Float.intBitsToFloat(Integer.reverseBytes(reference.readInt()));
                                        double error = Math.abs((double) actual - expected);
                                        if (!Float.isFinite(actual) || !Float.isFinite(expected)
                                                || error > .001 + .0001 * Math.abs(expected)) {
                                            throw new IllegalStateException("Der 3D-Vergleich weicht von der Referenz ab.");
                                        }
                                        maxError = Math.max(maxError, error);
                                    }
                                }
                                if (reference.read() != -1) throw new IllegalStateException("Ungültige Referenzgröße.");
                            }
                            sampledPssKb = Math.max(sampledPssKb, Debug.getPss());
                        }
                    }
                    checkActive();
                    JSObject result = new JSObject();
                    result.put("passed", true);
                    result.put("vertices", VERTICES);
                    result.put("elapsedSeconds", (SystemClock.elapsedRealtime() - start) / 1000.0);
                    result.put("sampledProcessPssKb", sampledPssKb);
                    result.put("maxExportDifferenceNativeUnits", maxError);
                    result.put("scope", "synthetic_cpu_only");
                    call.resolve(result);
                } catch (OutOfMemoryError error) {
                    call.reject("Für den 3D-Test war zu wenig freier Arbeitsspeicher verfügbar.");
                } catch (Exception error) {
                    String message = error.getMessage();
                    if (message == null) message = error.getClass().getSimpleName();
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

    private OrtSession.SessionOptions options() throws Exception {
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        try {
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
            if (length < 1024 || length > 120L * 1024 * 1024) {
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
