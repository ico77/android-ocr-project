package hr.ivica.android.ocr.graphics;

public class ScaleSize {
    public int calculate(int inWidth, int inHeight, int reqWidth, int reqHeight) {

        int inSampleSize = 1;

        if (inHeight > reqHeight || inWidth > reqWidth) {

            final int halfHeight = inHeight / 2;
            final int halfWidth = inWidth / 2;

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
