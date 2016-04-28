package hr.ivica.android.ocr.ocr;

public class OcrException extends Exception {
    public OcrException (String message) {
        super (message);
    }

    public OcrException (Throwable cause) {
        super (cause);
    }

    public OcrException (String message, Throwable cause) {
        super (message, cause);
    }
}
