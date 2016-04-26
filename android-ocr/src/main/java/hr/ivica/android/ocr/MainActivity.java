package hr.ivica.android.ocr;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import hr.ivica.android.ocr.alerts.AlertDialogFactory;
import hr.ivica.android.ocr.camera.CameraResource;
import hr.ivica.android.ocr.graphics.MatTransform;
import hr.ivica.android.ocr.ocr.Ocr;
import hr.ivica.android.ocr.ocr.TesseractTrainingData;

public final class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "MainActivity";
    private static final String DEFAULT_LANGUAGE = "eng";

    private FrameLayout mPreviewFrame;
    private SurfaceView mPreviewView;
    private CameraResource mCameraResource;
    private AlertDialogFactory mAlertDialogFactory = new AlertDialogFactory();
    private ViewFlipper mViewFlipper;
    private ImageView mImgPreview;

    private TessBaseAPI baseApi;
    private Ocr ocrEngine;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    static {
        System.loadLibrary("pngt");
        System.loadLibrary("lept");
        System.loadLibrary("tess");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!isSdCardMounted()) {
            Log.e(TAG, "External storage is not mounted");
            AlertDialog alertDialog = mAlertDialogFactory.getAlertAndExitDialog(this, R.string.sd_card_missing);
            alertDialog.show();
        }

        TesseractTrainingData trainingData = new TesseractTrainingData(getAssets());
        try {
            trainingData.copyTrainingDataToExternalStorage();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            AlertDialog alertDialog = mAlertDialogFactory.getAlertAndExitDialog(this, R.string.error_creating_data_dir);
            alertDialog.show();
        }

        mCameraResource = new CameraResource(this);
        mPreviewView = new SurfaceView(this);
        mPreviewView.getHolder().addCallback(this);
        mPreviewView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        mImgPreview = (ImageView) findViewById(R.id.imgPreview);

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        if (captureButton == null) throw new AssertionError("captureButton cannot be null");

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraResource.autoFocus(new OcrAutoFocusCallback());
            }
        });

        mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper01);
    }

    @Override
    public void onBackPressed(){
        if (mViewFlipper.getDisplayedChild() == 0) {
            super.onBackPressed();
            return;
        }

        if (mViewFlipper.getDisplayedChild() == 1) {
            mCameraResource.startPreview();
            BitmapDrawable drawable = (BitmapDrawable) mImgPreview.getDrawable();
            if (drawable != null) {
                drawable.getBitmap().recycle();
            }
        }

        mViewFlipper.showPrevious();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_about:
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");

        baseApi = new TessBaseAPI();
        boolean success = baseApi.init(getBaseTessPath(), DEFAULT_LANGUAGE, 0);
        ocrEngine = new Ocr(baseApi);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        mCameraResource.aquire();
        mPreviewFrame.addView(mPreviewView);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        mPreviewFrame.removeView(mPreviewView);
        mCameraResource.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");

        baseApi.end();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
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
        Log.d(TAG, "surfaceChanged - format: " + format + " width: " + w + " height: " + h);
        if (holder.getSurface() == null) {
            // previewFrame surface does not exist
            return;
        }

        mCameraResource.stopPreview();
        mCameraResource.setCameraDisplayOrientation();
        mCameraResource.setCameraParameters(w, h);

        // start previewFrame with new settings
        try {
           mCameraResource.setPreviewDisplay(holder);
           mCameraResource.startPreview();

        } catch (Exception e) {
            Log.e(TAG, "Error starting camera preview in surfaceChanged: " + e.getMessage(), e);
        }
    }

    private String getBaseTessPath() {
        return Environment.getExternalStorageDirectory()
                + File.separator + TesseractTrainingData.TESSBASE_DIR_NAME;
    }

    private boolean isSdCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private class OcrAutoFocusCallback implements Camera.AutoFocusCallback {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                mCameraResource.takePicture(new OcrPictureCallback());
            }
        }
    }

    private class OcrPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            mViewFlipper.showNext();

            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            Bitmap bmpARGB = bmp.copy(Bitmap.Config.ARGB_8888, false);

            bmp.recycle();

            Mat mat = new Mat();
            Utils.bitmapToMat(bmpARGB, mat);

            bmpARGB.recycle();

            Mat matOriented = new MatTransform(mat)
                    .rotate(mCameraResource.getCurrentRotation())
                    .getMat();

            List<Rect> textRegions = ocrEngine.detectText(matOriented);
            String recognizedText = ocrEngine.recognizeText(matOriented, textRegions, MainActivity.this);

            Log.w(TAG, "recognizedText is:" + recognizedText);

            addRectsToBitmap(textRegions, matOriented);
            Bitmap result = Bitmap.createBitmap(matOriented.width(), matOriented.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matOriented, result);
            mImgPreview.setImageBitmap(result);

        }

        private void addRectsToBitmap(List<Rect> rects, Mat mat) {
            for (Rect rect : rects) {
                Point p1 = new Point(rect.x, rect.y);
                Point p2 = new Point(rect.x + rect.width, rect.y + rect.height);
                Imgproc.rectangle(mat, p1, p2, new Scalar(255, 255, 255, 255), 10);
            }
        }
    }
}

