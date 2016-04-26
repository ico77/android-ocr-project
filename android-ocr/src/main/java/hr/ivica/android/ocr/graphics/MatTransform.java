package hr.ivica.android.ocr.graphics;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MatTransform {
    private static final String TAG = "MatTransform";
    private Mat workingCopy;

    public MatTransform(Mat mat) {
        this.workingCopy = mat;
    }

    public MatTransform rotate(RotateDegrees flag) {
        switch (flag) {
            case ROTATE_0:
                break;
            case ROTATE_90:
                Core.transpose(workingCopy, workingCopy);
                Core.flip(workingCopy, workingCopy, 1);
                break;
            case ROTATE_270:
                Core.transpose(workingCopy, workingCopy);
                Core.flip(workingCopy, workingCopy, 0);
                break;
            case ROTATE_180:
                Core.flip(workingCopy, workingCopy, -1);
                break;
            default:
                throw new IllegalArgumentException("flag value is not handled");
        }

        return this;
    }

    public MatTransform grayscale() {
        Imgproc.cvtColor(workingCopy, workingCopy, Imgproc.COLOR_RGB2GRAY);
        return this;
    }

    public MatTransform morphologicalGradient() {
        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7, 7));
        Imgproc.morphologyEx(workingCopy, workingCopy, Imgproc.MORPH_GRADIENT, morphKernel);
        return this;
    }

    public MatTransform morphologyClose() {
        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 17));
        Imgproc.morphologyEx(workingCopy, workingCopy, Imgproc.MORPH_CLOSE, morphKernel);
        return this;
    }

    public MatTransform binarize() {
        Imgproc.threshold(workingCopy, workingCopy, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        return this;
    }

    public MatTransform adaptiveThreshold() {
        Imgproc.adaptiveThreshold(workingCopy, workingCopy, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        return this;
    }

    public MatTransform gaussianBlur() {
        Mat clone = null;
        try {
            clone = workingCopy.clone();
            Imgproc.GaussianBlur(clone, workingCopy, new Size(5, 5), 0, 0);
        } finally {
            if (clone != null) clone.release();
        }
        return this;
    }

    public MatTransform bilateralFilter() {
        Mat clone = null;
        try {
            clone = workingCopy.clone();
            Imgproc.bilateralFilter(clone, workingCopy, 9, 75, 75);
        } finally {
            if (clone != null) clone.release();
        }
        return this;
    }

    public MatTransform unsharpMask() {
        Mat gaussian = null;
        try {
            gaussian = workingCopy.clone();
            Imgproc.GaussianBlur(workingCopy, gaussian, new Size(5, 5), 0, 0);
            Core.addWeighted(workingCopy, 1.5, gaussian, -0.5, 0, workingCopy);
        } finally {
            if (gaussian != null) gaussian.release();
        }
        return this;
    }

    public Mat getMat() {
        return workingCopy;
    }

    public enum RotateDegrees {
        ROTATE_0(0),
        ROTATE_90(90),
        ROTATE_180(180),
        ROTATE_270(270);

        private final int degrees;

        RotateDegrees(int degrees) {
            this.degrees = degrees;
        }

        public int intValue() {
            return degrees;
        }
    }
}
