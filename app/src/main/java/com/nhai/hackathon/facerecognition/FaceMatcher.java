package com.nhai.hackathon.facerecognition;

import com.nhai.hackathon.database.User;
import java.util.List;

public class FaceMatcher {
    private static final float SIMILARITY_THRESHOLD = 0.6f;

    public User findBestMatch(float[] queryEmbedding, List<User> users) {
        float bestSimilarity = -1;
        User bestMatch = null;

        for (User user : users) {
            float[] storedEmbedding = user.getEmbeddingAsFloatArray();
            float similarity = cosineSimilarity(queryEmbedding, storedEmbedding);
            if (similarity > bestSimilarity && similarity >= SIMILARITY_THRESHOLD) {
                bestSimilarity = similarity;
                bestMatch = user;
            }
        }
        return bestMatch;
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dotProduct = 0.0f, normA = 0.0f, normB = 0.0f;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}