package com.nhai.hackathon.liveness;

import android.util.Log;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

public class HeadPoseDetector {
    private static final String TAG = "HeadPoseDetector";
    private static final float REQUIRED_YAW_CHANGE = 15f; // degrees change required
    private float lastYaw = 0f;
    private boolean yawMovementDetected = false;

    public interface HeadMovementListener {
        void onHeadTurnedLeft();
        void onHeadTurnedRight();
        void onHeadMovementComplete();
    }

    private HeadMovementListener listener;

    public HeadPoseDetector(HeadMovementListener listener) {
        this.listener = listener;
    }

    public void processFace(Face face) {
        float currentYaw = face.getHeadEulerAngleY(); // Yaw: left/right rotation
        if (currentYaw != lastYaw && Math.abs(currentYaw - lastYaw) > REQUIRED_YAW_CHANGE) {
            if (currentYaw > lastYaw) {
                Log.d(TAG, "Head turned right: " + currentYaw);
                if (listener != null) listener.onHeadTurnedRight();
            } else {
                Log.d(TAG, "Head turned left: " + currentYaw);
                if (listener != null) listener.onHeadTurnedLeft();
            }
            yawMovementDetected = true;
            if (listener != null) listener.onHeadMovementComplete();
        }
        lastYaw = currentYaw;
    }

    public boolean isMovementDetected() {
        return yawMovementDetected;
    }

    public void reset() {
        yawMovementDetected = false;
        lastYaw = 0f;
    }
}