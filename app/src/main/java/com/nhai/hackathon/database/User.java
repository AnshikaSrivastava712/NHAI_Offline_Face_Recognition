package com.nhai.hackathon.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import java.nio.ByteBuffer;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public byte[] embedding;

    // Required empty constructor for Room
    public User() {}

    // Constructor for byte[] (used by Room)
    public User(String name, byte[] embedding) {
        this.name = name;
        this.embedding = embedding;
    }

    // Helper to create from float[] - ignored by Room
    @Ignore
    public static User fromFloatArray(String name, float[] embedding) {
        return new User(name, floatArrayToByteArray(embedding));
    }

    private static byte[] floatArrayToByteArray(float[] floats) {
        byte[] bytes = new byte[floats.length * 4];
        ByteBuffer.wrap(bytes).asFloatBuffer().put(floats);
        return bytes;
    }

    public float[] getEmbeddingAsFloatArray() {
        ByteBuffer buffer = ByteBuffer.wrap(embedding);
        float[] result = new float[buffer.remaining() / 4];
        buffer.asFloatBuffer().get(result);
        return result;
    }
}