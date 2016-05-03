package hr.ivica.android.ocr.ocr;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;

import hr.ivica.android.ocr.R;
import hr.ivica.android.ocr.util.OnErrorCallback;
import hr.ivica.android.ocr.util.OnSuccessCallback;

public class RecognizeTextAsync extends AsyncTaskWithCallbacks<RecognizeTextAsync.Param, Integer, String> {
    private static final String TAG = "RecognizeTextAsync";
    private Ocr mOcrEngine;

    public RecognizeTextAsync (Ocr ocrEngine, OnSuccessCallback<String> onSuccessCallback, OnErrorCallback onErrorCallback) {
        super(onSuccessCallback, onErrorCallback);
        this.mOcrEngine = ocrEngine;
    }

    @Override
    protected String doInBackground(Param... params) {
        StringBuilder recognizedText = new StringBuilder();
        List<Rect> textRegions = params[0].getTextRegions();
        Mat image = params[0].getImage();

        for (Rect textRegion : textRegions) {
            if(isCancelled()) {
                return null;
            }
            try {
                recognizedText.append(mOcrEngine.recognizeText(image, textRegion));
                recognizedText.append(System.getProperty("line.separator"));
            } catch (OcrException e) {
                Log.e(TAG, e.getMessage(), e);
                mThrowable = e;
                break;
            }
        }

        return recognizedText.toString();
    }

    @Override
    public int getErrorMessageResourceId() {
        return R.string.error_recognize_text;
    }

    public static class Param {
        private List<Rect> textRegions;
        private Mat image;

        public Param(List<Rect> textRegions, Mat image) {
            this.textRegions = textRegions;
            this.image = image;
        }

        public List<Rect> getTextRegions() {
            return textRegions;
        }

        public Mat getImage() {
            return image;
        }
    }
}
