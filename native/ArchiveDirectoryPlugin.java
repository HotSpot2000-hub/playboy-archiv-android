package de.playboy.archiv;

import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONException;

@CapacitorPlugin(name = "ArchiveDirectory")
public class ArchiveDirectoryPlugin extends Plugin {

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
