package hr.ivica.android.ocr.util;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

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

    public static void logHeap(Class clazz) {
        Double allocated = (double) Debug.getNativeHeapAllocatedSize() / 1048576d;
        Double available = Debug.getNativeHeapSize() / 1048576d;
        Double free = Debug.getNativeHeapFreeSize() / 1048576d;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        Log.d(TAG, "debug. =================================");
        Log.d(TAG, "debug.heap native: allocated " + df.format(allocated) + "MB of "
                + df.format(available) + "MB (" + df.format(free)
                + "MB free) in [" + clazz.getName().replaceAll("com.myapp.android.","") + "]");
        Log.d(TAG, "debug.memory: allocated: " + df.format(Double.valueOf(Runtime.getRuntime().totalMemory()/1048576))
                + "MB of " + df.format(Double.valueOf(Runtime.getRuntime().maxMemory()/1048576))
                + "MB (" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory()/1048576))
                + "MB free)");
        System.gc();
        System.gc();
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
