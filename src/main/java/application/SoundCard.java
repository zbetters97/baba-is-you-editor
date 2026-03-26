package application;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SoundCard {

    /* CLIP HOLDERS */
    public Clip clip;
    private final String[][] sounds = new String[5][];
    private final int[][] loopStarts = new int[2][];
    public int maxSongs = 0;

    /* VOLUME SLIDER */
    private FloatControl gainControl;
    public int volumeScale = 3;
    public float volume;
    private volatile boolean isLooping = false;

    public SoundCard() {
        sounds[0] = getSounds("00_music");
        maxSongs = sounds[0].length - 1;
        sounds[1] = getSounds("01_level");
        sounds[2] = getSounds("02_entities");
        sounds[3] = getSounds("03_rules");
        sounds[4] = getSounds("04_actions");

        // Loop start times for each music file in milliseconds
        loopStarts[0] = new int[]{7133, 1119, 3869, 7592};
    }

    private String[] getSounds(String library) {

        List<String> sounds = new ArrayList<>();

        try {
            boolean runningFromJar =
                    Driver.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .getPath()
                            .endsWith(".jar");

            if (runningFromJar) {
                String jarPath = new File(
                        Driver.class.getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .toURI()
                ).getPath();

                JarFile jarFile = new JarFile(jarPath);
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();

                    if (entry.getName().startsWith("sound/" + library + "/") && !entry.isDirectory()) {
                        sounds.add("/" + entry.getName());
                    }
                }

                jarFile.close();
            }
            else {
                File folder = new File(
                        Objects.requireNonNull(
                                getClass().getClassLoader()
                                        .getResource("sound/" + library)
                        ).toURI()
                );

                for (File f : Objects.requireNonNull(folder.listFiles())) {
                    sounds.add("/sound/" + library + "/" + f.getName().toLowerCase());
                }
            }

        }
        catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }

        return sounds.toArray(new String[0]);
    }

    public void setFile(int category, int record) {

        try {
            String path = sounds[category][record].substring(1);

            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream(path);

            if (is == null) {
                throw new RuntimeException("Sound not found: " + path);
            }

            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);

            clip = AudioSystem.getClip();
            clip.open(ais);

            gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            checkVolume();
        }
        catch (Exception e) {
            System.out.println("Error loading sound: " + e.getMessage());
        }
    }

    public int getLoopStart(int category, int record) {

        // Invalid category or record
        if (category < 0 || category > 9 || record < 0) {
            return 0;
        }

        // PC / Multi Battle Music
        if (category == 9) {
            // Invalid record
            if (record >= loopStarts[2].length) {
                return 0;
            }
            else {
                return loopStarts[2][record];
            }
        }
        // Single Battle / World Music
        else {
            // Invalid record
            if (record >= loopStarts[category].length) {
                return 0;
            }
            else {
                return loopStarts[category][record];
            }
        }
    }

    public void loop(int startTime) {

        if (clip == null) {
            return;
        }

        // Set looping flag to true
        isLooping = true;

        // Get audio format and calculate total frames
        AudioFormat format = clip.getFormat();
        float frameRate = format.getFrameRate();
        int totalFrames = clip.getFrameLength();

        // Convert start time to frames and ensure it's within bounds
        int startFrame = Math.max(0, (int) (startTime * frameRate / 1000));

        // Check for invalid start frame
        if (startFrame >= totalFrames || startTime <= 0) {
            clip.start();
            return;
        }

        // Set up a line listener to handle the looping
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                synchronized (clip) {
                    // Only loop if still in looping mode and clip is open
                    if (isLooping && clip.isOpen()) {
                        clip.setFramePosition(startFrame);
                        clip.start();
                    }
                }
            }
        });

        // Start playing from the beginning
        clip.setFramePosition(0);
        clip.start();
    }
    public void play() {
        clip.start();
    }
    public void stop() {
        isLooping = false;

        if (clip != null) {
            clip.stop();
        }
    }

    public void checkVolume() {

        switch (volumeScale) {
            case 0:
                volume = -80f;
                break;
            case 1:
                volume = -20f;
                break;
            case 2:
                volume = -12f;
                break;
            case 3:
                volume = -5f;
                break;
            case 4:
                volume = 1f;
                break;
            case 5:
                volume = 6f;
                break;
        }

        setGain(volume);
    }

    private void setGain(float gain) {
        gainControl.setValue(gain);
    }
}