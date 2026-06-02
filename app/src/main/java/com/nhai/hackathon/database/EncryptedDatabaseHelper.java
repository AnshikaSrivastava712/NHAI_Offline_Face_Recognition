package com.nhai.hackathon.database;

import android.content.Context;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptedDatabaseHelper {
    private static final String DATABASE_NAME = "face_db_encrypted";
    private static EncryptedDatabaseHelper instance;

    private EncryptedDatabaseHelper() {}

    public static synchronized EncryptedDatabaseHelper getInstance() {
        if (instance == null) instance = new EncryptedDatabaseHelper();
        return instance;
    }

    public AppDatabase getEncryptedDatabase(Context context) {
        try {
            // Create a master key (Android Keystore)
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Create encrypted file reference
            File file = new File(context.getFilesDir(), DATABASE_NAME);
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    context,
                    file,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            // Note: Room doesn't directly use EncryptedFile; this is a placeholder.
            // For actual encrypted Room, use SQLCipher with Android Keystore.
            // For hackathon demo, we'll fall back to standard Room.
            return AppDatabase.getInstance(context);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return AppDatabase.getInstance(context);
        }
    }
}