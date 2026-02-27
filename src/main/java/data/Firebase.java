package data;

import application.GamePanel;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

import com.google.cloud.storage.Blob;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public record Firebase(GamePanel gp) {

    public boolean init() {
        try {
            // Load in Firebase storage key
            InputStream serviceAccount = Firebase.class.getClassLoader().getResourceAsStream("db/firebase-key.json");

            if (serviceAccount == null) {
                throw new IllegalStateException("firebase-key.json not found in resources");
            }

            // Connect to Storage app using key credentials
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("babacreator-d122e.firebasestorage.app")
                    .build();

            // Boot up storage app
            FirebaseApp.initializeApp(options);

            return true;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void uploadLevel(Path tempFile) {
        try {
            // Open the storage bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // Save path to save file
            String remotePath = gp.levelPath + tempFile;

            // Copy provided file into the Storage bucket
            byte[] data = Files.readAllBytes(tempFile);
            bucket.create(remotePath, data);

            // Delete old file from memory
            Files.delete(tempFile);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public byte[] downloadLevel(String fileName) {
        try {
            // Open the storage bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // Attempt to open the file
            Blob file = bucket.get(gp.levelPath + fileName);
            if (file == null) {
                throw new IllegalStateException("LOAD ERROR: File not found in Firebase Storage");
            }

            // Return file contents
            return file.getContent();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public boolean deleteLevel(String fileName) {
        try {
            // Open the storage bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // Attempt to open the file
            Blob file = bucket.get(gp.levelPath + fileName);
            if (file == null) {
                throw new IllegalStateException("LOAD ERROR: File not found in Firebase Storage");
            }

            // Attempt to delete the file from Storage
            return file.delete();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Map<String, String> getSaveFileNames() {
        try {
            // String map to store save file names and dates
            Map<String, String> fileNames = new LinkedHashMap<>();

            // Open the storage bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // Parse over each file that has the same path as default
            List<Blob> files = new ArrayList<>();
            for (Blob file : bucket.list(Storage.BlobListOption.prefix(gp.levelPath)).iterateAll()) {

                // Ignore folders and non .dat files
                if (file.isDirectory()) continue;
                if (!file.getName().endsWith(".dat")) continue;

                // Add to list
                files.add(file);
            }

            // Sort by created time (DESC)
            files.sort((b1, b2) -> b2.getCreateTimeOffsetDateTime().compareTo(b1.getCreateTimeOffsetDateTime()));

            // Parse over each found save file
            for (Blob file : files) {

                // No content, continue
                byte[] data = file.getContent();
                if (data == null) continue;

                // Read data
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                DataStorage ds = (DataStorage) ois.readObject();

                // Retrieve stored file_date value
                String date = ds.file_date;

                // Format file name
                String fileName = file.getName().replace(gp.levelPath, "");

                // Add to Map (K: file ID, V: created date)
                fileNames.put(fileName, date);
            }

            return fileNames;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}