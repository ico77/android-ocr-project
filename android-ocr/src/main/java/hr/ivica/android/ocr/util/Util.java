package hr.ivica.android.ocr.util;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class Util {
    private static final String TAG = "Util";

    public static void saveToMediaStore(Bitmap regionBmp, Activity activity) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Debug image");
        values.put(MediaStore.Images.Media.BUCKET_ID, "test");
        values.put(MediaStore.Images.Media.DESCRIPTION, "test Image taken");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        OutputStream outstream = null;
        try {
            outstream = activity.getContentResolver().openOutputStream(uri);
            regionBmp.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
        } catch (IOException e) {
            Log.e(TAG, "Error saving image:" + e.getMessage(), e);
        } finally {
            try {
                if (outstream != null) outstream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing outstream:" + e.getMessage(), e);
            }
        }
    }
}
