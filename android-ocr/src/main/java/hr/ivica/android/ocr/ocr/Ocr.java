package hr.ivica.android.ocr.ocr;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import hr.ivica.android.ocr.graphics.MatTransform;
import hr.ivica.android.ocr.graphics.RectOperation;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Ocr {
    private static final String TAG = "MatTransform";
    private static final String DEFAULT_LANGUAGE = "eng";
    private static final int MIN_CONTOUR_PIXEL_WIDTH = 12;
    private static final int MIN_CONTOUR_PIXEL_HEIGHT = 12;
    private static final double MIN_SOLIDITY_VALUE = 0.25;
    private static final double MIN_EXTENT_VALUE = 0.2;
    private static final double MAX_EXTENT_VALUE = 0.9;
    private static final float DEFAULT_X_SCALE_FACTOR = 1.1f;
    private static final float DEFAULT_Y_SCALE_FACTOR = 1.05f;
    private static final float OVERLAP_DETECTION_X_SCALE_FACTOR = 1.4f;
    private static final float OVERLAP_DETECTION_Y_SCALE_FACTOR = 1f;
    private static final Comparator<Rect> rectComparator = new Comparator<Rect>() {
        public int compare(Rect r1, Rect r2) {
            if (r1.y < r2.y) return -1;
            if (r1.y > r2.y) return 1;
            return 0;
        }
    };

    private TessBaseAPI baseApi;
    private Object baseApiLock = new Object();
    private BaseApiStatus baseApiStatus = BaseApiStatus.UNINITIALIZED;

    public Ocr(TessBaseAPI baseApi) {
        this.baseApi = baseApi;
    }

    public void init(String baseTessPath) throws OcrException {
        synchronized (baseApiLock) {
            if (!baseApi.init(baseTessPath, DEFAULT_LANGUAGE, 0)) {
                throw new OcrException("Ocr initialization failed");
            }

            baseApiStatus = BaseApiStatus.READY;
        }
    }

    public void end() {
        synchronized (baseApiLock) {
            baseApi.end();
            baseApiStatus = BaseApiStatus.DESTROYED;
        }
    }

    public List<Rect> detectText(Mat image) {
        Mat cloned = image.clone();

        try {
            preprocessImageForDetection(cloned);
            List<Rect> textRegions = getTextRegions(cloned);
            List<Rect> combinedTextRegions = combineTextRegions(textRegions, cloned.width(), cloned.height());
            Collections.sort(combinedTextRegions, rectComparator);
            return combinedTextRegions;
        } finally {
            cloned.release();
        }
    }

    public String recognizeText(Mat image, List<Rect> textRegions) throws OcrException {
        StringBuilder recognizedText = new StringBuilder();

        for (Rect textRegion : textRegions) {
            Mat regionImage = null;
            Bitmap regionBmp = null;

            try {
                regionImage = new Mat(textRegion.height, textRegion.width, CvType.CV_8U);

                image.submat(textRegion).copyTo(regionImage);
                preprocessImageForRecognition(regionImage);

                regionBmp = Bitmap.createBitmap(regionImage.width(), regionImage.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(regionImage, regionBmp);

                synchronized (baseApiLock) {
                    if (!(baseApiStatus == BaseApiStatus.READY)) {
                        throw new OcrException("Ocr not ready");
                    }
                    baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
                    baseApi.setImage(regionBmp);
                    recognizedText.append(baseApi.getUTF8Text()).append(System.getProperty("line.separator"));
                    baseApi.clear();
                }
             } finally {
                if (regionImage != null) regionImage.release();
                if (regionBmp != null) regionBmp.recycle();
            }

        }

        return recognizedText.toString();
    }

    private Mat preprocessImageForDetection(Mat image) {
        return new MatTransform(image)
                .grayscale()
                .bilateralFilter()
                .morphologicalGradient()
                .binarize()
                .morphologyClose()
                .getMat();
    }

    private Mat preprocessImageForRecognition(Mat image) {
        return new MatTransform(image)
                .grayscale()
                .gaussianBlur()
                .unsharpMask()
                .binarize()
                .getMat();
    }

    private List<Rect> getTextRegions(Mat image) {
        List<Rect> rects = new ArrayList<Rect>();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Mat cloned = null;
        try {
            cloned = image.clone();
            Imgproc.findContours(cloned, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        } finally {
            if (cloned != null) cloned.release();
        }

        if (hierarchy.empty()) {
            return rects;
        }

        int contourIndex = 0;

        // filter contours
        while (contourIndex >= 0) {
            double[] data = hierarchy.get(0, contourIndex);
            int nextContourIndex = (int)data[0];

            MatOfPoint contour = contours.get(contourIndex);
            MatOfPoint2f contour2f = new MatOfPoint2f();
            contour.convertTo(contour2f, CvType.CV_32FC2);

            MatOfPoint cPoly = new MatOfPoint();
            MatOfPoint2f cPoly2f = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, cPoly2f, 3, true);
            cPoly2f.convertTo(cPoly, CvType.CV_32S);

            Rect rect = Imgproc.boundingRect(cPoly);

            contourIndex = nextContourIndex;
            if (rect.height < MIN_CONTOUR_PIXEL_HEIGHT || rect.width < MIN_CONTOUR_PIXEL_WIDTH) {
                continue;
            }

            double solidity = getContourSolidity(contour);
            if (solidity < MIN_SOLIDITY_VALUE) continue;

            double extent = getContourExtent(contour);
            if (extent < MIN_EXTENT_VALUE || extent > MAX_EXTENT_VALUE) continue;

            rects.add(rect);
        }

        Log.d(TAG, "Found " + contours.size() + " and created " + rects.size() + " rectangles.");
        return rects;
    }

    private List<Rect> combineTextRegions(List<Rect> rects, int maxX, int maxY) {
        RectOperation operation = new RectOperation();

        List<Rect> rectsScaledToOverlap = new ArrayList<>();

        for (Rect rect : rects) {
            operation.scale(rect, DEFAULT_X_SCALE_FACTOR, DEFAULT_Y_SCALE_FACTOR);

            // adjust new boundaries if necessary
            if (rect.x < 0) rect.x = 0;
            if (rect.y < 0) rect.y = 0;
            if (rect.x + rect.width > maxX) rect.width = maxX - rect.x;
            if (rect.y + rect.height > maxY) rect.height = maxY - rect.y;

            // create new rect that will be over-scaled
            // these over-scaled rectangles will be used to find neighbouring rectangles
            Rect rectScaledToOverlap = new Rect(rect.x, rect.y, rect.width, rect.height);
            operation.scale(rectScaledToOverlap, OVERLAP_DETECTION_X_SCALE_FACTOR, OVERLAP_DETECTION_Y_SCALE_FACTOR);
            rectsScaledToOverlap.add(rectScaledToOverlap);
        }

        List<List<Integer>> indexGroups = operation.findOverlappingRects(rectsScaledToOverlap);
        List<Rect> newRects = new LinkedList<>();

        for (List<Integer> group : indexGroups) {
            List<Rect> groupedRects = new LinkedList<>();
            for (Integer index : group) {
                groupedRects.add(rects.get(index));
            }
            newRects.add(operation.union(groupedRects));
        }
        return newRects;
    }

    private double getContourSolidity(MatOfPoint contour) {
        double area = Imgproc.contourArea(contour);
        MatOfInt cHullIndices = new MatOfInt();
        Imgproc.convexHull(contour, cHullIndices);

        MatOfPoint mopOut = new MatOfPoint();
        mopOut.create((int)cHullIndices.size().height,1,CvType.CV_32SC2);

        for(int i = 0; i < cHullIndices.size().height ; i++)
        {
            int index = (int)cHullIndices.get(i, 0)[0];
            double[] point = new double[] {
                    contour.get(index, 0)[0], contour.get(index, 0)[1]
            };
            mopOut.put(i, 0, point);
        }

        double hullArea = Imgproc.contourArea(mopOut);
        return area/hullArea;
    }

    private double getContourExtent(MatOfPoint contour) {
        double area = Imgproc.contourArea(contour);
        Rect rect = Imgproc.boundingRect(contour);

        return area/rect.area();
    }

    private enum BaseApiStatus {
        UNINITIALIZED,
        READY,
        DESTROYED
    }
}