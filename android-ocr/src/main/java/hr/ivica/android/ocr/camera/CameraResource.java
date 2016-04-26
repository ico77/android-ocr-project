package hr.ivica.android.ocr.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

import hr.ivica.android.ocr.SettingsActivity;
import hr.ivica.android.ocr.graphics.MatTransform;

public class CameraResource {
    private static final String TAG = "CameraResource";
    private static final int CAMERA_FACING_BACK_ID = 0;
    private static final int MIN_PREVIEW_WIDTH = 600;
    private static final int MIN_PREVIEW_HEIGHT = 400;

    private Camera mCamera;
    private MatTransform.RotateDegrees mCurrentRotation;
    private Context mContext;

    public CameraResource (Context context) {
        this.mContext = context;
    }

    public MatTransform.RotateDegrees getCurrentRotation() {
        return mCurrentRotation;
    }

    public void setCameraDisplayOrientation() {
        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

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

    public void setCameraParameters(int w, int h) {
        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String focusModePref = sharedPref.getString(SettingsActivity.KEY_PREF_CAMERA_FOCUS_MODE, Camera.Parameters.FOCUS_MODE_MACRO);

        Camera.Parameters params = mCamera.getParameters();
        if (params.getSupportedFocusModes().contains(focusModePref)) {
            params.setFocusMode(focusModePref);
        } else {
            Log.w(TAG, "Camera does not support " + focusModePref + " focus mode, using default focus");
        }

        Camera.Size size = getOptimalPreviewSize(w, h);
        Log.d(TAG, "Camera optimal preview size is width: " + size.width + ", height: " + size.height );

        params.setPreviewSize(size.width, size.height);
        mCamera.setParameters(params);
    }

    public void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

        mCamera.setPreviewDisplay(holder);
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
        mCamera = Camera.open(0);
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

    public void stopPreview() {
        if (mCamera == null) {
            throw new IllegalStateException("camera is null!");
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.d(TAG, "stopPreview:" + e);
        }

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

        mCamera.takePicture(null, null, callback);
    }

    private Camera.Size getOptimalPreviewSize(int w, int h) {
        double targetRatio = (double) w / h;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        List<Camera.Size> supportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

        for (Camera.Size size : supportedPreviewSizes) {
            if (size.width < MIN_PREVIEW_WIDTH || size.height < MIN_PREVIEW_HEIGHT) {
                continue;
            }
            double ratio = (double) size.width / size.height;
            double ratioDiff = Math.abs(ratio - targetRatio);
            if ( ratioDiff < minDiff) {
                optimalSize = size;
                minDiff = ratioDiff;
            }
        }

        if (optimalSize == null) {
            optimalSize = supportedPreviewSizes.get(0);
        }
        
        return optimalSize;
    }
}
