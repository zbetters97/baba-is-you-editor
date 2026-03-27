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
            System.out.println("Error connecting to Firebase: " + e.getMessage());
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
            System.out.println("Error uploading file: " + e.getMessage());
        }
    }

    public byte[] downloadLevel(String fileName) {
        try {
            // Open the storage bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // Attempt to open the file
            Blob file = bucket.get(fileName);
            if (file == null) {
                throw new IllegalStateException("File not found in Firebase Storage");
            }

            // Return file contents
            return file.getContent();
        }
        catch (Exception e) {
            System.out.println("Error downloading level: " + e.getMessage());
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
                throw new IllegalStateException("File not found in Firebase Storage");
            }

            // Attempt to delete the file from Storage
            return file.delete();
        }
        catch (Exception e) {
            System.out.println("Error deleting level: " + e.getMessage());
            return false;
        }
    }

    public Map<String, String> getUserLevels(String userId) {
        try {
            // String map to store save file names and dates
            Map<String, String> fileNames = new LinkedHashMap<>();

            // Open the storage bucket
            Bucket bucket = StorageClient.getInstance().bucket();

            // Parse over each file that has the same path as default
            List<Blob> files = new ArrayList<>();
            for (Blob file : bucket.list(Storage.BlobListOption.prefix("levels/" + userId + "/")).iterateAll()) {

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

                // Retrieve stored file_name value
                String name = ds.toString();

                // Add to Map (K: file ID, V: level name)
                fileNames.put(file.getName(), name);
            }

            return fileNames;
        } catch (Exception e) {
            System.out.println("Error getting saved levels: " + e.getMessage());
            return null;
        }
    }

    public Map<String, String> getAllUsers() {
        try {
            Map<String, String> folders = new LinkedHashMap<>();
            Bucket bucket = StorageClient.getInstance().bucket();

            Set<String> topLevelFolders = new LinkedHashSet<>();

            // Iterate over all blobs with prefix "levels/"
            for (Blob blob : bucket.list(Storage.BlobListOption.prefix("levels/")).iterateAll()) {
                String name = blob.getName();

                // Remove "levels/" prefix
                String remaining = name.substring("levels/".length());
                if (remaining.isEmpty()) continue;

                // Get the first part = top-level folder (userId)
                String[] parts = remaining.split("/", 2);
                String userId = parts[0];

                // Collect unique top-level folders
                topLevelFolders.add(userId);
            }

            // Convert userId to email
            for (String userId : topLevelFolders) {

                // Skip logged-in user
                if (userId.equals(gp.auth.getUserId())) continue;

                String email = gp.auth.getEmailByUserID(userId);
                folders.put(userId, email);
            }

            return folders;
        }
        catch (Exception e) {
            System.out.println("Error getting saved levels: " + e.getMessage());
            return null;
        }
    }
}