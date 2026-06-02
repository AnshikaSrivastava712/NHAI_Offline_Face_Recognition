package com.nhai.hackathon.facerecognition;

import android.content.Context;
import android.graphics.Bitmap;
import java.util.Random;

public class FaceEmbedder {
    private static final int EMBEDDING_SIZE = 128;
    private final Random random = new Random();

    public FaceEmbedder(Context context) {
        // No model loading needed for dummy version
    }

    public float[] generateEmbedding(Bitmap faceBitmap) {
        float[] embedding = new float[EMBEDDING_SIZE];
        for (int i = 0; i < EMBEDDING_SIZE; i++) {
            embedding[i] = random.nextFloat();
        }
        return embedding;
    }

    public void close() {
        // Nothing to close
    }
}