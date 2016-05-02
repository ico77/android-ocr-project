package hr.ivica.android.ocr.graphics;

import org.opencv.core.Rect;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RectOperation {
    public boolean isIntersect(Rect firstRect, Rect secondRect) {
        if (firstRect == null) {
            throw new NullPointerException("Argument firstRect is null...");
        }

        if (secondRect == null) {
            throw new NullPointerException("Argument secondRect is null...");
        }

        return ! (firstRect.x > secondRect.x + secondRect.width
            || firstRect.x + firstRect.width < secondRect.x
            || firstRect.y > secondRect.y + secondRect.height
            || firstRect.y + firstRect.height < secondRect.y);
    }

    public boolean isDominantOverlapOnYAxis(Rect firstRect, Rect secondRect, double overlapFactor) {
        if (firstRect == null) {
            throw new NullPointerException("Argument firstRect is null...");
        }

        if (secondRect == null) {
            throw new NullPointerException("Argument secondRect is null...");
        }

        if (firstRect.y > secondRect.y + secondRect.height
                || firstRect.y + firstRect.height < secondRect.y) {
            return false;
        }

        int lowY = Math.min(firstRect.y, secondRect.y);
        int highY = Math.max(firstRect.y + firstRect.height, secondRect.y + secondRect.height);

        int combinedHeight = highY - lowY;
        if (combinedHeight < overlapFactor * (firstRect.height + secondRect.height))
            return true;
        else
            return false;
    }

    public void scale(Rect rect, double xScaleFactor, double yScaleFactor) {
        if (rect == null) {
            throw new NullPointerException("Argument firstRect is null...");
        }

        if (xScaleFactor == 0) {
            throw new IllegalArgumentException("xScaleFactor can not be 0...");
        }

        if (yScaleFactor == 0) {
            throw new IllegalArgumentException("yScaleFactor can not be 0...");
        }

        int newWidth = (int)((xScaleFactor) * rect.width);
        int newHeight = (int)((yScaleFactor) * rect.height);

        rect.x = rect.x - (int)((xScaleFactor - 1) * rect.width / 2);
        rect.y = rect.y - (int)((yScaleFactor - 1) * rect.height / 2);
        rect.width = newWidth;
        rect.height = newHeight;
    }

    public List<List<Integer>> findOverlappingRects(List<Rect> inputRects) {
        List<List<Integer>> groupedRects = new LinkedList<>();

        int size = inputRects.size();
        if (size == 0) {
            return groupedRects;
        }

        short[][] connectivityMatrix = new short[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (isIntersect(inputRects.get(i), inputRects.get(j))) {
                    connectivityMatrix[i][j] = 1;
                    connectivityMatrix[j][i] = 1;
                }
            }
        }

        Set<Integer> visitedRects = new HashSet<>();

        for (int i = 0; i < size; i++) {
            if (visitedRects.contains(i)) {
                continue;
            }

            visitedRects.add(i);
            List<Integer> connectedRects = new LinkedList<>();
            connectedRects.add(i);

            List<Integer> children = new LinkedList<>();

            for (int j = i + 1; j < size; j++) {
                if (connectivityMatrix[i][j] == 1) {
                    children.add(j);
                }
            }

            while (children.size() > 0) {
                Integer rectIndex = children.remove(0);

                visitedRects.add(rectIndex);
                connectedRects.add(rectIndex);

                for (int j = 0; j < size; j++) {
                    if (visitedRects.contains(j)) {
                        continue;
                    }
                    if (connectivityMatrix[rectIndex][j] == 1) {
                        children.add(j);
                    }
                }
            }

            groupedRects.add(connectedRects);
        }

        return groupedRects;
    }

    public Rect union(Rect firstRect, Rect secondRect) {
        if (firstRect == null) {
            throw new NullPointerException("Argument firstRect is null...");
        }

        if (secondRect == null) {
            throw new NullPointerException("Argument secondRect is null...");
        }

        int xMin = firstRect.x < secondRect.x ? firstRect.x : secondRect.x;
        int yMin = firstRect.y < secondRect.y ? firstRect.y : secondRect.y;
        int xMax = firstRect.x + firstRect.width > secondRect.x + secondRect.width
                ? firstRect.x + firstRect.width : secondRect.x + secondRect.width;
        int yMax = firstRect.y + firstRect.height > secondRect.y + secondRect.height
                ? firstRect.y + firstRect.height : secondRect.y + secondRect.height;

        return new Rect(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    public Rect union(List<Rect> rects) {
        if (rects == null) {
            throw new NullPointerException("Argument rects is null...");
        }

        if (rects.size() == 0) {
            throw new IllegalArgumentException("Rectangle list is empty...");
        }

        Rect result = rects.remove(0);
        for (Rect rect : rects) {
            result = union(result, rect);
        }

        return result;
    }
}
