package hr.ivica.android.ocr.ocr;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TesseractTrainingData {
    private static final String TAG = "TesseractTrainingData";
    public static final String TESSBASE_DIR_NAME = "tesseract";
    public static final String TESSDATA_DIR_NAME = "tessdata";

    private final AssetManager assetManager;

    public TesseractTrainingData(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void copyTrainingDataToExternalStorage() throws IOException {
        String mDestDir = getDataPath();

        String[] files = null;
        try {
            files = assetManager.list(TESSDATA_DIR_NAME);
            for (String file : files) {
                byte[] buffer = new byte[1024];

                InputStream in = null;
                OutputStream out = null;

                File mDestFile = new File(mDestDir + File.separator + file);
                if (mDestFile.exists()) continue;

                try {
                    in = assetManager.open(TESSDATA_DIR_NAME + File.separator + file);
                    out = new FileOutputStream(mDestFile);
                    int length;

                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                } finally {
                    if (in != null) in.close();
                    if (out != null) out.close();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw e;
        }
    }

    private String getDataPath() throws IOException {
        String mTessDataDirName = Environment.getExternalStorageDirectory()
                + File.separator + TESSBASE_DIR_NAME
                + File.separator + TESSDATA_DIR_NAME;

        File mTessDataDir = new File(mTessDataDirName);
        if (!mTessDataDir.exists()) {
            boolean success = mTessDataDir.mkdirs();
            if (!success) {
                throw new IOException("Error creating tesseract training data directory");
            }
        }

        return mTessDataDirName;
    }
}
