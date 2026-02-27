package data;

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

public class Firebase {

    public void init() throws Exception {

        InputStream serviceAccount =
                Firebase.class
                        .getClassLoader()
                        .getResourceAsStream("db/firebase-key.json");

        if (serviceAccount == null) {
            throw new IllegalStateException("firebase-key.json not found in resources");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket("babacreator-d122e.firebasestorage.app")
                .build();

        FirebaseApp.initializeApp(options);
    }

    public void uploadLevel(Path tempFile) throws IOException {

        Bucket bucket = StorageClient.getInstance().bucket();
        String remotePath = "levels/" + tempFile;

        byte[] data = Files.readAllBytes(tempFile);
        bucket.create(remotePath, data);

        Files.delete(tempFile);
    }

    public byte[] downloadLevel(String fileName) {

        Bucket bucket = StorageClient.getInstance().bucket();

        Blob file = bucket.get("levels/" + fileName);
        if (file == null) {
            throw new IllegalStateException("LOAD ERROR: File not found in Firebase Storage");
        }

        return file.getContent();
    }

    public boolean deleteLevel(String fileName) {
        try {
            Bucket bucket = StorageClient.getInstance().bucket();

            Blob file = bucket.get("levels/" + fileName);
            if (file == null) {
                throw new IllegalStateException("LOAD ERROR: File not found in Firebase Storage");
            }

            file.delete();
            return true;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Map<String, String> getSaveFileNames() {

        try {
            Map<String, String> fileNames = new LinkedHashMap<>();
            Bucket bucket = StorageClient.getInstance().bucket();

            List<Blob> files = new ArrayList<>();
            for (Blob file : bucket.list(Storage.BlobListOption.prefix("levels/")).iterateAll()) {
                if (file.isDirectory()) continue;
                if (!file.getName().endsWith(".dat")) continue;
                files.add(file);
            }

            files.sort((b1, b2) -> b2.getCreateTimeOffsetDateTime().compareTo(b1.getCreateTimeOffsetDateTime()));

            for (Blob file : files) {
                byte[] data = file.getContent();
                if (data == null) continue;

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                DataStorage ds = (DataStorage) ois.readObject();
                String date = ds.file_date;
                String fileName = file.getName().replace("levels/", "");
                fileNames.put(fileName, date);
            }

            return fileNames;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}