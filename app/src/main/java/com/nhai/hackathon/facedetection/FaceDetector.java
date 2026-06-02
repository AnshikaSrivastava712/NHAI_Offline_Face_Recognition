package com.nhai.hackathon.facedetection;

import android.graphics.PointF;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import java.util.ArrayList;
import java.util.List;

public class FaceDetector {
    private com.google.mlkit.vision.face.FaceDetector detector;
    private List<Face> detectedFaces = new ArrayList<>();

    public interface FaceDetectionListener {
        void onFacesDetected(List<Face> faces);
    }

    public FaceDetector() {
        // Use ACCURATE mode to get full landmarks (required for EAR)
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)   // Needed for eye landmarks
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.2f)
                .enableTracking()
                .build();
        detector = FaceDetection.getClient(options);
    }

    public void detectFaces(ImageProxy imageProxy, FaceDetectionListener listener) {
        InputImage inputImage = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );
        detector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    detectedFaces = faces;
                    listener.onFacesDetected(faces);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    listener.onFacesDetected(new ArrayList<>());
                });
    }

    public List<Face> getDetectedFaces() {
        return detectedFaces;
    }

    /**
     * Returns eye landmark points for left and right eye.
     * Returns null if not available.
     */
    public static FaceLandmark[] getEyeLandmarks(Face face) {
        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
        if (leftEye == null || rightEye == null) return null;
        return new FaceLandmark[]{leftEye, rightEye};
    }

    public void close() {
        if (detector != null) detector.close();
    }
}