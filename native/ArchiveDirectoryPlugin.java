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

import java.nio.ByteBuffer;

import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

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
        boolean includePhotoMask = call.getBoolean("includePhotoMask", false);
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
                    SelfieSegmenterOptions segmentationOptions = new SelfieSegmenterOptions.Builder()
                        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                        .build();
                    Segmenter segmenter = Segmentation.getClient(segmentationOptions);
                    segmenter.process(image)
                        .addOnSuccessListener(mask -> {
                            addContourMetrics(result, pose, mask, width, height);
                            if (includePhotoMask) addPhotoMask(result, mask, width, height);
                            segmenter.close(); detector.close(); bitmap.recycle(); call.resolve(result);
                        })
                        .addOnFailureListener(error -> {
                            result.put("contourAvailable", false);
                            segmenter.close(); detector.close(); bitmap.recycle(); call.resolve(result);
                        });
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
        out.put("geometry", poseGeometry(pose, width, height));
        return out;
    }

    private JSObject poseGeometry(Pose pose, int width, int height) {
        JSObject geometry = new JSObject();
        int[][] landmarks = new int[][] {
            {PoseLandmark.NOSE}, {PoseLandmark.LEFT_EYE}, {PoseLandmark.RIGHT_EYE},
            {PoseLandmark.LEFT_EAR}, {PoseLandmark.RIGHT_EAR},
            {PoseLandmark.LEFT_SHOULDER}, {PoseLandmark.RIGHT_SHOULDER},
            {PoseLandmark.LEFT_ELBOW}, {PoseLandmark.RIGHT_ELBOW},
            {PoseLandmark.LEFT_WRIST}, {PoseLandmark.RIGHT_WRIST},
            {PoseLandmark.LEFT_HIP}, {PoseLandmark.RIGHT_HIP},
            {PoseLandmark.LEFT_KNEE}, {PoseLandmark.RIGHT_KNEE},
            {PoseLandmark.LEFT_ANKLE}, {PoseLandmark.RIGHT_ANKLE},
            {PoseLandmark.LEFT_HEEL}, {PoseLandmark.RIGHT_HEEL},
            {PoseLandmark.LEFT_FOOT_INDEX}, {PoseLandmark.RIGHT_FOOT_INDEX}
        };
        String[] names = new String[] {
            "nose", "leftEye", "rightEye", "leftEar", "rightEar",
            "leftShoulder", "rightShoulder", "leftElbow", "rightElbow",
            "leftWrist", "rightWrist", "leftHip", "rightHip",
            "leftKnee", "rightKnee", "leftAnkle", "rightAnkle",
            "leftHeel", "rightHeel", "leftFoot", "rightFoot"
        };
        for (int index = 0; index < landmarks.length; index++) {
            PoseLandmark landmark = pose.getPoseLandmark(landmarks[index][0]);
            if (!landmarkVisible(landmark)) continue;
            PointF point = landmark.getPosition();
            JSObject value = new JSObject();
            value.put("x", width > 0 ? point.x / width : 0f);
            value.put("y", height > 0 ? point.y / height : 0f);
            value.put("confidence", landmark.getInFrameLikelihood());
            geometry.put(names[index], value);
        }
        return geometry;
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

    // Optional compact confidence grid for transient visual diagnostics, never persisted.
    private void addPhotoMask(JSObject out, SegmentationMask mask, int imageWidth, int imageHeight) {
        try {
            int width = mask.getWidth(), height = mask.getHeight();
            byte[] packed = photoMaskBytes(mask.getBuffer(), width, height);
            double scale = Math.min(1.0, 256.0 / Math.max(width, height));
            int targetWidth = Math.max(1, (int) Math.round(width * scale));
            int targetHeight = Math.max(1, (int) Math.round(height * scale));
            JSObject grid = new JSObject();
            grid.put("revision", 1); grid.put("source", "mlkit_person_confidence");
            grid.put("width", targetWidth); grid.put("height", targetHeight);
            grid.put("imageWidth", imageWidth); grid.put("imageHeight", imageHeight);
            grid.put("confidence", Base64.encodeToString(packed, Base64.NO_WRAP));
            out.put("photoMask", grid);
        } catch (RuntimeException error) {
            out.put("photoMaskUnavailable", true);
        }
    }

    private static byte[] photoMaskBytes(ByteBuffer source, int width, int height) {
        if (width < 1 || height < 1 || width > 4096 || height > 4096
                || (long) width * height * 4 > source.limit()) throw new IllegalArgumentException("Ungültige Personenmaske.");
        ByteBuffer buffer = source.duplicate().order(source.order()); buffer.rewind();
        double scale = Math.min(1.0, 256.0 / Math.max(width, height));
        int tw = Math.max(1, (int) Math.round(width * scale)), th = Math.max(1, (int) Math.round(height * scale));
        byte[] result = new byte[tw * th];
        for (int y = 0; y < th; y++) for (int x = 0; x < tw; x++) {
            int sx = Math.min(width - 1, (int) ((x + 0.5) * width / tw));
            int sy = Math.min(height - 1, (int) ((y + 0.5) * height / th));
            float value = buffer.getFloat(4 * (sy * width + sx));
            if (!Float.isFinite(value) || value < 0 || value > 1) throw new IllegalArgumentException("Ungültige Maskenkonfidenz.");
            result[y * tw + x] = (byte) Math.round(value * 255f);
        }
        return result;
    }

    private void addContourMetrics(JSObject out, Pose pose, SegmentationMask mask, int imageWidth, int imageHeight) {
        int width = mask.getWidth(), height = mask.getHeight();
        ByteBuffer buffer = mask.getBuffer(); buffer.rewind();
        float[] confidence = new float[width * height];
        int minY = height, maxY = -1;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            float value = buffer.getFloat(); confidence[y * width + x] = value;
            if (value >= 0.55f) { minY = Math.min(minY, y); maxY = Math.max(maxY, y); }
        }
        PoseLandmark ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER), rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark lh = pose.getPoseLandmark(PoseLandmark.LEFT_HIP), rh = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        if (!landmarkVisible(ls) || !landmarkVisible(rs) || !landmarkVisible(lh) || !landmarkVisible(rh) || maxY <= minY) {
            out.put("contourAvailable", false); return;
        }
        float scaleX = imageWidth > 0 ? width / (float) imageWidth : 1f;
        float scaleY = imageHeight > 0 ? height / (float) imageHeight : 1f;
        float shoulderY = (ls.getPosition().y + rs.getPosition().y) * 0.5f * scaleY;
        float hipY = (lh.getPosition().y + rh.getPosition().y) * 0.5f * scaleY;
        float centerX = (ls.getPosition().x + rs.getPosition().x + lh.getPosition().x + rh.getPosition().x) * 0.25f * scaleX;
        float bodyHeight = maxY - minY + 1f;
        float bustY = shoulderY + (hipY - shoulderY) * 0.34f;
        float waistY = shoulderY + (hipY - shoulderY) * 0.70f;
        float bust = armCrossesTorsoRow(pose, bustY, centerX, bodyHeight, scaleX, scaleY)
            ? 0f : contourWidth(confidence, width, height, centerX, bustY) / bodyHeight;
        float waist = armCrossesTorsoRow(pose, waistY, centerX, bodyHeight, scaleX, scaleY)
            ? 0f : contourWidth(confidence, width, height, centerX, waistY) / bodyHeight;
        float hips = armCrossesTorsoRow(pose, hipY, centerX, bodyHeight, scaleX, scaleY)
            ? 0f : contourWidth(confidence, width, height, centerX, hipY) / bodyHeight;
        boolean available = bust > 0f || waist > 0f || hips > 0f;
        out.put("contourAvailable", available);
        if (available) { out.put("bustWidthRatio", bust); out.put("waistWidthRatio", waist); out.put("hipWidthRatio", hips); }
    }

    private boolean armCrossesTorsoRow(Pose pose, float rowY, float centerX, float bodyHeight, float scaleX, float scaleY) {
        int[][] arms = new int[][] {
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW},
            {PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW},
            {PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST}
        };
        float torsoRadius = bodyHeight * 0.34f;
        for (int[] segment : arms) {
            PoseLandmark first = pose.getPoseLandmark(segment[0]);
            PoseLandmark second = pose.getPoseLandmark(segment[1]);
            if (!landmarkVisible(first) || !landmarkVisible(second)) continue;
            float y1 = first.getPosition().y * scaleY, y2 = second.getPosition().y * scaleY;
            if (rowY < Math.min(y1, y2) || rowY > Math.max(y1, y2) || Math.abs(y2 - y1) < 0.5f) continue;
            float progress = (rowY - y1) / (y2 - y1);
            float x = (first.getPosition().x + (second.getPosition().x - first.getPosition().x) * progress) * scaleX;
            if (Math.abs(x - centerX) <= torsoRadius) return true;
        }
        return false;
    }

    private float contourWidth(float[] mask, int width, int height, float centerX, float rowY) {
        int y = Math.max(0, Math.min(height - 1, Math.round(rowY)));
        int center = Math.max(0, Math.min(width - 1, Math.round(centerX)));
        int seed = -1;
        for (int offset = 0; offset < width / 3 && seed < 0; offset++) {
            int left = center - offset, right = center + offset;
            if (left >= 0 && mask[y * width + left] >= 0.55f) seed = left;
            else if (right < width && mask[y * width + right] >= 0.55f) seed = right;
        }
        if (seed < 0) return 0f;
        int left = seed, right = seed;
        while (left > 0 && mask[y * width + left - 1] >= 0.55f) left--;
        while (right < width - 1 && mask[y * width + right + 1] >= 0.55f) right++;
        return right - left + 1f;
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
