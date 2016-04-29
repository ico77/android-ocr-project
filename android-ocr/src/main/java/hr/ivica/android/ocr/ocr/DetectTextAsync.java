package hr.ivica.android.ocr.ocr;

import android.os.AsyncTask;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.lang.ref.WeakReference;
import java.util.List;

import hr.ivica.android.ocr.R;
import hr.ivica.android.ocr.util.OnErrorCallback;
import hr.ivica.android.ocr.util.OnSuccessCallback;

public class DetectTextAsync extends AsyncTask<Mat, Integer, List<Rect>> {
    private static final String TAG = "DetectTextAsync";
    private Ocr mOcrEngine;
    private Throwable mThrowable;
    private WeakReference<OnSuccessCallback<List<Rect>>> mOnSuccessCallback;
    private WeakReference<OnErrorCallback> mOnErrorCallback;

    public DetectTextAsync (Ocr ocrEngine, OnSuccessCallback<List<Rect>> onSuccessCallback, OnErrorCallback onErrorCallback) {
        this.mOcrEngine = ocrEngine;
        this.mOnSuccessCallback = new WeakReference<>(onSuccessCallback);
        this.mOnErrorCallback = new WeakReference<>(onErrorCallback);
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
    protected void onPostExecute(List<Rect> result) {
        if (mThrowable != null) {
            OnErrorCallback onErrorCallback = mOnErrorCallback.get();
            if (onErrorCallback != null) {
                onErrorCallback.execute(mThrowable, R.string.error_detect_text);
            }
            return;
        }

        OnSuccessCallback<List<Rect>> onSuccessCallback = mOnSuccessCallback.get();
        if (onSuccessCallback != null) {
            onSuccessCallback.execute(result);
        }
    }
}
