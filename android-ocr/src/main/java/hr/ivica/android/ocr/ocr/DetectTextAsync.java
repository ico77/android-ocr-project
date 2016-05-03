package hr.ivica.android.ocr.ocr;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;

import hr.ivica.android.ocr.R;
import hr.ivica.android.ocr.util.OnErrorCallback;
import hr.ivica.android.ocr.util.OnSuccessCallback;

public class DetectTextAsync extends AsyncTaskWithCallbacks<Mat, Integer, List<Rect>> {
    private static final String TAG = "DetectTextAsync";
    private Ocr mOcrEngine;

    public DetectTextAsync (Ocr ocrEngine, OnSuccessCallback<List<Rect>> onSuccessCallback, OnErrorCallback onErrorCallback) {
        super(onSuccessCallback, onErrorCallback);
        this.mOcrEngine = ocrEngine;
    }

    @Override
    protected List<Rect> doInBackground(Mat... params) {
        List<Rect> rects = null;
        try {
            Mat image = params[0];
            rects = mOcrEngine.detectText(image);
        } catch (Exception e){
            mThrowable = e;
        }

        return rects;
    }

    @Override
    public int getErrorMessageResourceId() {
        return R.string.error_detect_text;
    }
}
