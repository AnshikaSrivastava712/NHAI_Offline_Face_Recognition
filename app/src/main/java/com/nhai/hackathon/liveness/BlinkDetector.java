package com.nhai.hackathon.liveness;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.mlkit.vision.face.Face;

public class BlinkDetector {
    private static final String TAG = "BlinkDetector";
    private static final int AUTO_CONFIRM_DELAY_MS = 3000; // 3 seconds

    private boolean livenessConfirmed = false;
    private BlinkListener listener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean facePresent = false;
    private Runnable autoConfirmRunnable = () -> {
        if (!livenessConfirmed && facePresent) {
            livenessConfirmed = true;
            Log.d(TAG, "Auto liveness confirmed after 3 seconds");
            if (listener != null) listener.onLivenessConfirmed();
        }
    };

    public interface BlinkListener {
        void onBlink(int blinkCount, boolean livenessConfirmed);
        void onLivenessConfirmed();
    }

    public BlinkDetector(BlinkListener listener) {
        this.listener = listener;
    }

    public void processFace(Face face) {
        if (face == null) {
            if (facePresent) {
                facePresent = false;
                handler.removeCallbacks(autoConfirmRunnable);
            }
            return;
        }

        if (!facePresent) {
            facePresent = true;
            handler.postDelayed(autoConfirmRunnable, AUTO_CONFIRM_DELAY_MS);
        }
    }

    public boolean isLivenessConfirmed() {
        return livenessConfirmed;
    }

    public void reset() {
        livenessConfirmed = false;
        facePresent = false;
        handler.removeCallbacks(autoConfirmRunnable);
    }
}