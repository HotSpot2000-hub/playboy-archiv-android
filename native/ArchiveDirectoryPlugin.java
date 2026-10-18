package de.playboy.archiv;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Base64;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import org.json.JSONException;

@CapacitorPlugin(name = "ArchiveDirectory")
public class ArchiveDirectoryPlugin extends Plugin {

    private static final float LANDMARK_CONFIDENCE = 0.55f;

    @PluginMethod
    public void resolvePath(PluginCall call) {
        String rootUriString = call.getString("rootUri");
        JSArray path = call.getArray("path");

        if (rootUriString == null || rootUriString.isEmpty()) {
            call.reject("rootUri fehlt");
            return;
        }

        try {
            Uri treeUri = Uri.parse(rootUriString);
            String rootDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
            Uri current = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootDocumentId);

            if (path != null) {
                for (int i = 0; i < path.length(); i++) {
                    String segment = path.getString(i);
                    if (segment == null || segment.isEmpty()) continue;
                    current = findChild(current, segment);
                    if (current == null) {
                        call.reject("Nicht gefunden: " + segment);
                        return;
                    }
                }
            }

            JSObject ret = new JSObject();
            ret.put("uri", current.toString());
            call.resolve(ret);
        } catch (JSONException e) {
            call.reject("Ungültiger Pfad", e);
        } catch (Exception e) {
            call.reject("Ordner konnte nicht gelesen werden: " + e.getMessage(), e);
        }
    }

    @PluginMethod
public void setVideoImmersive(PluginCall call) {
    boolean enabled = call.getBoolean("enabled", false);

    if (getActivity() instanceof MainActivity) {
        ((MainActivity) getActivity()).setVideoImmersive(enabled);
        call.resolve();
    } else {
        call.reject("MainActivity nicht verfügbar");
    }
}

    @PluginMethod
    public void analyzePose(PluginCall call) {
        String dataUrl = call.getString("dataUrl");
        if (dataUrl == null || dataUrl.isEmpty()) {
            call.reject("Bilddaten fehlen");
            return;
        }

        try {
            int comma = dataUrl.indexOf(',');
            String encoded = comma >= 0 ? dataUrl.substring(comma + 1) : dataUrl;
            byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                call.reject("Bild konnte nicht gelesen werden");
                return;
            }

            AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build();
            PoseDetector detector = PoseDetection.getClient(options);
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            detector.process(image)
                .addOnSuccessListener(pose -> {
                    JSObject result = poseQuality(pose, width, height);
                    detector.close();
                    bitmap.recycle();
                    call.resolve(result);
                })
                .addOnFailureListener(error -> {
                    detector.close();
                    bitmap.recycle();
                    call.reject("Pose konnte nicht analysiert werden: " + error.getMessage(), error);
                });
        } catch (Exception error) {
            call.reject("Poseanalyse fehlgeschlagen: " + error.getMessage(), error);
        }
    }

    private JSObject poseQuality(Pose pose, int width, int height) {
        JSObject out = new JSObject();
        int visible = 0;
        float confidenceSum = 0f;
        float minX = width;
        float minY = height;
        float maxX = 0f;
        float maxY = 0f;

        for (PoseLandmark landmark : pose.getAllPoseLandmarks()) {
            float confidence = landmark.getInFrameLikelihood();
            if (confidence < LANDMARK_CONFIDENCE) continue;
            visible++;
            confidenceSum += confidence;
            PointF point = landmark.getPosition();
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
        }

        boolean shoulders = pairVisible(pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER);
        boolean hips = pairVisible(pose, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP);
        boolean knees = pairVisible(pose, PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE);
        boolean ankles = pairVisible(pose, PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE);
        boolean upperBody = shoulders && hips;
        boolean fullBody = upperBody && knees && ankles;
        boolean safelyFramed = fullBody && minX >= width * 0.01f && maxX <= width * 0.99f
            && minY >= height * 0.01f && maxY <= height * 0.99f;

        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        float shoulderWidth = pairDistance(leftShoulder, rightShoulder);
        float hipWidth = pairDistance(leftHip, rightHip);
        float torsoLength = midpointDistance(leftShoulder, rightShoulder, leftHip, rightHip);
        float viewRatio = torsoLength > 0f ? ((shoulderWidth + hipWidth) * 0.5f) / torsoLength : 0f;
        String view = viewRatio >= 0.72f ? "frontal" : viewRatio >= 0.48f ? "angled" : "side";

        out.put("poseDetected", visible >= 8);
        out.put("visibleLandmarks", visible);
        out.put("averageConfidence", visible > 0 ? confidenceSum / visible : 0f);
        out.put("upperBody", upperBody);
        out.put("fullBody", fullBody);
        out.put("safelyFramed", safelyFramed);
        out.put("view", upperBody ? view : "unknown");
        out.put("viewRatio", viewRatio);
        out.put("poseSignature", poseSignature(pose, torsoLength));
        return out;
    }

    private JSArray poseSignature(Pose pose, float torsoLength) {
        JSArray values = new JSArray();
        if (torsoLength <= 0f) return values;
        int[][] pairs = new int[][] {
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER},
            {PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP},
            {PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST},
            {PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE},
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_WRIST},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_WRIST},
            {PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_ANKLE},
            {PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_ANKLE}
        };
        for (int[] pair : pairs) {
            PoseLandmark first = pose.getPoseLandmark(pair[0]);
            PoseLandmark second = pose.getPoseLandmark(pair[1]);
            float value = landmarkVisible(first) && landmarkVisible(second)
                ? pairDistance(first, second) / torsoLength
                : -1f;
            try {
                values.put(value);
            } catch (JSONException ignored) {
                values.put(-1);
            }
        }
        return values;
    }

    private float pairDistance(PoseLandmark first, PoseLandmark second) {
        if (first == null || second == null) return 0f;
        PointF a = first.getPosition();
        PointF b = second.getPosition();
        return (float) Math.hypot(a.x - b.x, a.y - b.y);
    }

    private float midpointDistance(PoseLandmark a1, PoseLandmark a2, PoseLandmark b1, PoseLandmark b2) {
        if (!landmarkVisible(a1) || !landmarkVisible(a2) || !landmarkVisible(b1) || !landmarkVisible(b2)) return 0f;
        PointF p1 = a1.getPosition(), p2 = a2.getPosition(), p3 = b1.getPosition(), p4 = b2.getPosition();
        return (float) Math.hypot((p1.x + p2.x - p3.x - p4.x) * 0.5f, (p1.y + p2.y - p3.y - p4.y) * 0.5f);
    }

    private boolean pairVisible(Pose pose, int leftType, int rightType) {
        return landmarkVisible(pose.getPoseLandmark(leftType)) && landmarkVisible(pose.getPoseLandmark(rightType));
    }

    private boolean landmarkVisible(PoseLandmark landmark) {
        return landmark != null && landmark.getInFrameLikelihood() >= LANDMARK_CONFIDENCE;
    }

      private Uri findChild(Uri parentUri, String wantedName) {
        String parentDocumentId = DocumentsContract.getDocumentId(parentUri);
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, parentDocumentId);

        String[] projection = new String[] {
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        };

        try (Cursor cursor = getContext().getContentResolver().query(childrenUri, projection, null, null, null)) {
            if (cursor == null) return null;
            int idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                if (wantedName.equals(name)) {
                    String documentId = cursor.getString(idIndex);
                    return DocumentsContract.buildDocumentUriUsingTree(parentUri, documentId);
                }
            }
        }
        return null;
    }
}
