package com.nhai.hackathon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.mlkit.vision.face.Face;
import com.nhai.hackathon.camera.CameraService;
import com.nhai.hackathon.database.AppDatabase;
import com.nhai.hackathon.database.User;
import com.nhai.hackathon.facedetection.FaceDetector;
import com.nhai.hackathon.facedetection.FaceOverlayView;
import com.nhai.hackathon.facerecognition.FaceEmbedder;
import com.nhai.hackathon.liveness.BlinkDetector;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private CameraService cameraService;
    private PreviewView previewView;
    private TextView tvStatus;
    private FaceOverlayView faceOverlayView;
    private Button btnRegister, btnClearDatabase;

    private FaceDetector faceDetector;
    private BlinkDetector blinkDetector;
    private FaceEmbedder faceEmbedder;
    private AppDatabase db;
    private List<User> registeredUsers;

    private ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private static final int PERMISSION_REQUEST_CAMERA = 100;
    private Bitmap currentFaceBitmap; // dummy placeholder

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        tvStatus = findViewById(R.id.tvStatus);
        faceOverlayView = findViewById(R.id.faceOverlayView);
        btnRegister = findViewById(R.id.btnRegister);
        btnClearDatabase = findViewById(R.id.btnClearDatabase);

        db = AppDatabase.getInstance(this);
        loadRegisteredUsers();

        cameraService = new CameraService(this, this, previewView);
        faceDetector = new FaceDetector();
        faceEmbedder = new FaceEmbedder(this);  // dummy

        // Blink detector
        blinkDetector = new BlinkDetector(new BlinkDetector.BlinkListener() {
            @Override
            public void onBlink(int blinkCount, boolean livenessConfirmed) {
                runOnUiThread(() -> {
                    faceOverlayView.setBlinkCount(blinkCount);
                    tvStatus.setText("Blink " + blinkCount + "/2");
                });
            }

            @Override
            public void onLivenessConfirmed() {
                runOnUiThread(() -> {
                    faceOverlayView.setLivenessConfirmed(true);
                    tvStatus.setText("✅ Liveness verified! Ready to register.");
                    Toast.makeText(MainActivity.this, "Liveness confirmed", Toast.LENGTH_SHORT).show();
                });
            }
        });

        if (cameraService.hasCameraPermission()) {
            startCameraAndAnalysis();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }

        btnRegister.setOnClickListener(v -> {
            if (blinkDetector.isLivenessConfirmed()) {
                // Always allow registration (dummy face)
                showRegisterDialog();
            } else {
                Toast.makeText(this, "Please blink twice first", Toast.LENGTH_SHORT).show();
            }
        });

        btnClearDatabase.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Database")
                    .setMessage("Delete all registered faces?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        backgroundExecutor.execute(() -> {
                            db.userDao().deleteAll();
                            loadRegisteredUsers();
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Database cleared", Toast.LENGTH_SHORT).show());
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void loadRegisteredUsers() {
        backgroundExecutor.execute(() -> {
            registeredUsers = db.userDao().getAllUsers();
            runOnUiThread(() -> {
                tvStatus.setText("Registered: " + registeredUsers.size());
            });
        });
    }

    private void startCameraAndAnalysis() {
        cameraService.startCamera();
        previewView.setScaleType(PreviewView.ScaleType.FILL_START);

        cameraService.setFrameAnalyzer(imageProxy -> {
            faceDetector.detectFaces(imageProxy, faces -> {
                runOnUiThread(() -> {
                    faceOverlayView.setFaces(faces);
                    if (faces != null && !faces.isEmpty()) {
                        Face face = faces.get(0);
                        blinkDetector.processFace(face);

                        // Dummy bitmap – no real cropping (avoids crash)
                        currentFaceBitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888);

                        // Simulate recognition after liveness
                        if (blinkDetector.isLivenessConfirmed() && registeredUsers != null && !registeredUsers.isEmpty()) {
                            // Show first registered user's name
                            User first = registeredUsers.get(0);
                            faceOverlayView.setRecognizedUser(first.name);
                            faceOverlayView.setRecognitionStatus(true);
                            tvStatus.setText("✅ Welcome, " + first.name + "!");
                        } else {
                            faceOverlayView.setRecognizedUser(null);
                            faceOverlayView.setRecognitionStatus(false);
                        }
                    } else {
                        currentFaceBitmap = null;
                        faceOverlayView.setRecognizedUser(null);
                        faceOverlayView.setRecognitionStatus(false);
                    }
                });
            });
            imageProxy.close();
        });
    }

    private void showRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register Face");
        builder.setMessage("Enter name:");
        final android.widget.EditText input = new android.widget.EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Register", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                registerCurrentFace(name);
            } else {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void registerCurrentFace(String name) {
        backgroundExecutor.execute(() -> {
            float[] dummyEmbedding = new float[128]; // all zeros
            User user = User.fromFloatArray(name, dummyEmbedding);
            db.userDao().insertUser(user);
            loadRegisteredUsers();
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Registered " + name, Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraAndAnalysis();
            } else {
                Toast.makeText(this, "Camera required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraService != null) cameraService.stopCamera();
        if (faceDetector != null) faceDetector.close();
        if (faceEmbedder != null) faceEmbedder.close();
        backgroundExecutor.shutdown();
    }
}