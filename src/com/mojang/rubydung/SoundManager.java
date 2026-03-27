package com.mojang.rubydung;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, byte[]> soundData = new HashMap<>();
    private Map<String, AudioFormat> soundFormats = new HashMap<>();
    private Clip currentMusicClip;
    private String currentMusicName = "";
    private float masterVolume = 0.7f;
    private float musicVolume = 0.4f;

    private SoundManager() {
        loadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSounds() {
        try {
            loadSound("/xodba.wav");
            loadSound("/derevolomat.wav");
            loadSound("/travalomat.wav");
            loadSound("/kamenlomat.wav");
            System.out.println("Sounds LOADED sycksesfyli");
        } catch (Exception e) {
            System.err.println("FAILED to load sounds: " + e.getMessage());
        }
    }

    private void loadSound(String resourceName) {
        try {
            InputStream audioSrc = SoundManager.class.getResourceAsStream(resourceName);
            if (audioSrc == null) {
                System.err.println("Sound file NOT FOUND: " + resourceName);
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = audioSrc.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] audioBytes = baos.toByteArray();
            audioSrc.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            BufferedInputStream bis = new BufferedInputStream(bais);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);

            AudioFormat format = audioStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
            );

            AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);

            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            byte[] dataBuffer = new byte[4096];
            while ((bytesRead = decodedStream.read(dataBuffer)) != -1) {
                dataStream.write(dataBuffer, 0, bytesRead);
            }

            soundData.put(resourceName, dataStream.toByteArray());
            soundFormats.put(resourceName, decodedFormat);

            decodedStream.close();
            audioStream.close();
            bis.close();
            bais.close();

            System.out.println("LOADED: " + resourceName);
        } catch (Exception e) {
            System.err.println("ERROR loading sound " + resourceName + ": " + e.getMessage());
        }
    }

    public void playStepSound(float x, float y, float z) {
        playSound("/xodba.wav", 0.5f);
    }

    public void playBreakSound(int blockType, float x, float y, float z) {
        float volume = 0.7f;
        String soundFile = null;

        switch (blockType) {
            case 1:
                soundFile = "/travalomat.wav";
                volume = 0.6f;
                break;
            case 2:
                soundFile = "/kamenlomat.wav";
                volume = 0.9f;
                break;
            case 3:
            case 4:
                soundFile = "/derevolomat.wav";
                volume = 0.7f;
                break;
            default:
                return;
        }

        if (soundFile != null) {
            playSound(soundFile, volume);
        }
    }

    private void playSound(String resourceName, float volume) {
        try {
            byte[] audioData = soundData.get(resourceName);
            AudioFormat format = soundFormats.get(resourceName);

            if (audioData == null || format == null) {
                return;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume * masterVolume) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            } catch (IllegalArgumentException e) {}

            clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        audioStream.close();
                        bais.close();
                    } catch (Exception e) {}
                }
            });

        } catch (Exception e) {
            System.err.println("ERROR playing sound: " + e.getMessage());
        }
    }

    public void playMusic(String musicFile) {
        if (currentMusicName.equals(musicFile) && currentMusicClip != null && currentMusicClip.isRunning()) {
            return;
        }

        try {
            if (currentMusicClip != null && currentMusicClip.isOpen()) {
                currentMusicClip.stop();
                currentMusicClip.close();
            }

            InputStream audioSrc = SoundManager.class.getResourceAsStream(musicFile);
            if (audioSrc == null) {
                System.err.println("Music file NOT FOUND: " + musicFile);
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = audioSrc.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] audioBytes = baos.toByteArray();
            audioSrc.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            BufferedInputStream bis = new BufferedInputStream(bais);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);

            AudioFormat format = audioStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
            );

            AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);

            currentMusicClip = AudioSystem.getClip();
            currentMusicClip.open(decodedStream);

            try {
                FloatControl gainControl = (FloatControl) currentMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(musicVolume * masterVolume) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            } catch (IllegalArgumentException e) {}

            currentMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            currentMusicClip.start();

            currentMusicName = musicFile;
            System.out.println("Music started: " + musicFile);

            decodedStream.close();
            audioStream.close();
            bis.close();
            bais.close();

        } catch (Exception e) {
            System.err.println("FAILED to play music: " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (currentMusicClip != null && currentMusicClip.isOpen()) {
            currentMusicClip.stop();
            currentMusicClip.close();
            currentMusicName = "";
        }
    }

    public void updateListener(float x, float y, float z, float yRot, float xRot) {
        //zzaglushka
    }

    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
    }

    public void cleanup() {
        if (currentMusicClip != null && currentMusicClip.isOpen()) {
            currentMusicClip.stop();
            currentMusicClip.close();
        }
        soundData.clear();
        soundFormats.clear();
    }
}