package hr.ivica.android.ocr.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import hr.ivica.android.ocr.SettingsActivity;
import hr.ivica.android.ocr.graphics.MatTransform;

public class CameraResource implements SurfaceHolder.Callback {
    private static final String TAG = "CameraResource";
    private static final int CAMERA_FACING_BACK_ID = 0;

    private Camera mCamera;
    private MatTransform.RotateDegrees mCurrentRotation;
    private Context mContext;

    public CameraResource (Context context) {
        this.mContext = context;
    }

    public MatTransform.RotateDegrees getCurrentRotation() {
        return mCurrentRotation;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "surfaceChanged " + Thread.currentThread().getName());
        if (holder.getSurface() == null) {
            // previewFrame surface does not exist
            return;
        }

        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

        // stop previewFrame before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent previewFrame
        }

        setCameraDisplayOrientation();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String focusModePref = sharedPref.getString(SettingsActivity.KEY_PREF_CAMERA_FOCUS_MODE, "");

        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(focusModePref);
        mCamera.setParameters(params);

        // start previewFrame with new settings
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.e(TAG, "Error starting camera preview in surfaceChanged: " + e.getMessage(), e);
        }
    }

    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CAMERA_FACING_BACK_ID, info);
        int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();

        Log.d(TAG, "Default display rotation is:" + rotation);
        Log.d(TAG, "Camera orientation is:" + info.orientation);

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = (info.orientation - degrees + 360) % 360;
        Log.d(TAG, "Camera orientation result is:" + result);

        mCurrentRotation = getRotationEnum(result);
        mCamera.setDisplayOrientation(result);

    }

    private MatTransform.RotateDegrees getRotationEnum(int value) {
        for (MatTransform.RotateDegrees r : MatTransform.RotateDegrees.values()) {
            if (r.intValue() == value) {
                return r;
            }
        }

        throw new IllegalArgumentException("Rotation value is: " + value + ". Should be 0,90,180 or 270");
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public void aquire() {
        mCamera = Camera.open(0); // attempt to get a Camera instance
    }

    public void release() {
        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

        mCamera.stopPreview();
        mCamera.release();        // release the camera for other applications
        mCamera = null;
    }

    public void startPreview() {
        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

        mCamera.startPreview();
    }

    public void autoFocus(Camera.AutoFocusCallback callback) {
        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

        mCamera.autoFocus(callback);
    }

    public void takePicture(Camera.PictureCallback callback) {
        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

        // get an image from the camera
        mCamera.takePicture(null, null, callback);
    }
}
