package hr.ivica.android.ocr.ocr;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import hr.ivica.android.ocr.util.OnErrorCallback;
import hr.ivica.android.ocr.util.OnSuccessCallback;

public abstract class AsyncTaskWithCallbacks<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected Throwable mThrowable;
    private WeakReference<OnSuccessCallback<Result>> mOnSuccessCallback;
    private WeakReference<OnErrorCallback> mOnErrorCallback;

    public AsyncTaskWithCallbacks (OnSuccessCallback<Result> onSuccessCallback, OnErrorCallback onErrorCallback) {
        this.mOnSuccessCallback = new WeakReference<>(onSuccessCallback);
        this.mOnErrorCallback = new WeakReference<>(onErrorCallback);
    }

    public abstract int getErrorMessageResourceId();

    @Override
    protected void onPostExecute(Result result) {
        if (mThrowable != null) {
            OnErrorCallback onErrorCallback = mOnErrorCallback.get();
            if (onErrorCallback != null) {
                onErrorCallback.execute(mThrowable, getErrorMessageResourceId());
            }
            return;
        }

        OnSuccessCallback<Result> onSuccessCallback = mOnSuccessCallback.get();
        if (onSuccessCallback != null) {
            onSuccessCallback.execute(result);
        }
    }
}
