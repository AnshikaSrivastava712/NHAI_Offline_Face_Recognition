package com.nhai.hackathon.facedetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.google.mlkit.vision.face.Face;
import java.util.List;

public class FaceOverlayView extends View {
    private List<Face> faces;
    private int blinkCount = 0;
    private boolean livenessConfirmed = false;
    private Paint boxPaint;
    private Paint textPaint;
    private Paint livenessPaint;
    private String recognizedUser = null;
    private boolean recognitionSuccess = false;
    private Paint namePaint;
    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);

        textPaint = new Paint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(40f);
        textPaint.setStyle(Paint.Style.FILL);

        livenessPaint = new Paint();
        livenessPaint.setColor(Color.CYAN);
        livenessPaint.setTextSize(50f);
        livenessPaint.setStyle(Paint.Style.FILL);
        namePaint = new Paint();
        namePaint.setColor(Color.GREEN);
        namePaint.setTextSize(60f);
        namePaint.setStyle(Paint.Style.FILL);
    }

    public void setFaces(List<Face> faces) {
        this.faces = faces;
        invalidate();
    }

    public void setBlinkCount(int count) {
        this.blinkCount = count;
        invalidate();
    }

    public void setLivenessConfirmed(boolean confirmed) {
        this.livenessConfirmed = confirmed;
        invalidate();
    }
    public void setRecognizedUser(String name) {
        this.recognizedUser = name;
        invalidate();
    }

    public void setRecognitionStatus(boolean success) {
        this.recognitionSuccess = success;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faces != null) {
            for (Face face : faces) {
                RectF box = new RectF(face.getBoundingBox());
                canvas.drawRect(box, boxPaint);
                canvas.drawText("Face", box.left, box.top - 10, textPaint);
            }
        }
        if (recognitionSuccess && recognizedUser != null) {
            canvas.drawText(recognizedUser, getWidth() / 2f - 100, getHeight() - 100, namePaint);
        }

        // Draw liveness status at top-left
        String status = livenessConfirmed ? "✅ LIVENESS CONFIRMED" : "😉 Blink " + blinkCount + "/2";
        canvas.drawText(status, 50, 100, livenessPaint);
    }
}