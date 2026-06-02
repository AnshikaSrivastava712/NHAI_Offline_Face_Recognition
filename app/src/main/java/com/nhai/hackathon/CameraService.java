package com.nhai.hackathon.camera;
import androidx.camera.core.AspectRatio;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.util.Size;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraService {
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private OnFrameAnalyzer currentAnalyzer;

    // Performance optimization: frame skipping
    private long lastProcessTime = 0;
    private static final long MIN_FRAME_INTERVAL_MS = 100; // Process every 100ms (10 FPS)

    public interface OnFrameAnalyzer {
        void analyze(ImageProxy imageProxy);
    }

    public CameraService(Context context, LifecycleOwner lifecycleOwner, PreviewView previewView) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.previewView = previewView;
    }

    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void startCamera() {
        if (!hasCameraPermission()) throw new SecurityException("Camera permission not granted");
        ProcessCameraProvider.getInstance(context).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(context).get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        // Calculate best aspect ratio for full screen
        DisplayMetrics metrics = new DisplayMetrics();
        previewView.getDisplay().getRealMetrics(metrics);
        int screenAspectRatio = (metrics.widthPixels > metrics.heightPixels)
                ? AspectRatio.RATIO_16_9
                : AspectRatio.RATIO_16_9; // You can also try RATIO_4_3 if 16:9 doesn't fit well

        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = null;
        if (currentAnalyzer != null) {
            imageAnalysis = new ImageAnalysis.Builder()
                    .setTargetResolution(new Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();
            imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                // Frame skipping: process only every 100ms
                long now = System.currentTimeMillis();
                if (now - lastProcessTime >= MIN_FRAME_INTERVAL_MS) {
                    lastProcessTime = now;
                    currentAnalyzer.analyze(imageProxy);
                } else {
                    imageProxy.close(); // skip this frame
                }
            });
        }

        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        cameraProvider.unbindAll();
        if (imageAnalysis != null) {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis);
        } else {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview);
        }
    }

    public void stopCamera() {
        if (cameraProvider != null) cameraProvider.unbindAll();
        cameraExecutor.shutdown();
    }

    public void setFrameAnalyzer(OnFrameAnalyzer analyzer) {
        this.currentAnalyzer = analyzer;
        if (hasCameraPermission() && cameraProvider != null) {
            bindCameraUseCases();
        }
    }

    public void clearFrameAnalyzer() {
        this.currentAnalyzer = null;
        if (hasCameraPermission() && cameraProvider != null) {
            bindCameraUseCases();
        }
    }
}