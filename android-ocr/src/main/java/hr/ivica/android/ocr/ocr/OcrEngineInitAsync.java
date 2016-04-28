package hr.ivica.android.ocr.ocr;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import hr.ivica.android.ocr.util.OnErrorCallback;

public class OcrEngineInitAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "OcrEngineInitAsync";

    private TesseractTrainingData mTrainingData;
    private Ocr mOcrEngine;
    private Throwable mThrowable;
    private OnErrorCallback mOnErrorCallback;

    public OcrEngineInitAsync (TesseractTrainingData trainingData, Ocr ocrEngine, OnErrorCallback onErrorCallback) {
        this.mTrainingData = trainingData;
        this.mOcrEngine = ocrEngine;
        this.mOnErrorCallback = onErrorCallback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
            String destDir = mTrainingData.getExternalStorageDataPath();
            String[] files = mTrainingData.getTrainingDataFileList();

            for (String file : files) {
                if (isCancelled()) {
                    Log.d(TAG, "task cancelled");
                    return null;
                }

                mTrainingData.copyTrainingDataFile(file, destDir);
            }

            mOcrEngine.init(mTrainingData.getBaseTessPath());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            mThrowable = e;
        } catch (OcrException oe) {
            Log.e(TAG, oe.getMessage(), oe);
            mThrowable = oe;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mThrowable != null) {
            mOnErrorCallback.execute(mThrowable);
        }
    }
}
