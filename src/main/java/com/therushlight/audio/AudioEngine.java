package com.therushlight.audio;

import org.lwjgl.openal.*;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

/**
 * OpenAL-based audio engine with buses for music, SFX, voice, and UI.
 * Music ducks under voice automatically.
 */
public class AudioEngine {

    private long device;
    private long context;

    // Bus volumes
    private float masterVolume = 1.0f;
    private float musicVolume = 0.7f;
    private float sfxVolume = 1.0f;
    private float voiceVolume = 1.0f;

    // Music source
    private int musicSource = -1;
    private String currentMusic = null;

    // Voice ducking
    private boolean voicePlaying = false;
    private float musicDuckTarget = 1.0f;
    private float musicDuckCurrent = 1.0f;

    public AudioEngine() {
        try {
            // Initialize OpenAL
            String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
            device = alcOpenDevice(defaultDevice);
            if (device == 0) {
                System.err.println("[Audio] Failed to open device. Audio disabled.");
                return;
            }

            int[] attributes = {0};
            context = alcCreateContext(device, attributes);
            alcMakeContextCurrent(context);
            AL.createCapabilities(ALC.createCapabilities(device));

            alListener3f(AL_POSITION, 0, 0, 0);
            alListener3f(AL_VELOCITY, 0, 0, 0);

            // Create music source
            musicSource = alGenSources();
            alSourcef(musicSource, AL_GAIN, musicVolume);
            alSourcei(musicSource, AL_LOOPING, AL_TRUE);

            System.out.println("[Audio] OpenAL initialized.");
        } catch (Exception e) {
            System.err.println("[Audio] Init failed: " + e.getMessage());
            device = 0;
        }
    }

    public void playMusic(String path) {
        if (device == 0 || musicSource == -1) return;
        if (path.equals(currentMusic)) return;

        // Stop current music
        alSourceStop(musicSource);

        // TODO: Load OGG/WAV and attach to source
        // For now, just track what should be playing
        currentMusic = path;
        System.out.println("[Audio] Music: " + path);
    }

    public void stopMusic() {
        if (musicSource != -1) {
            alSourceStop(musicSource);
            currentMusic = null;
        }
    }

    public void playSound(String path) {
        if (device == 0) return;
        // TODO: Pool of one-shot sources
        System.out.println("[Audio] SFX: " + path);
    }

    public void playVoice(String path) {
        if (device == 0) return;
        voicePlaying = true;
        musicDuckTarget = 0.3f; // Duck music
        // TODO: Load and play voice clip
        System.out.println("[Audio] Voice: " + path);
    }

    public void stopVoice() {
        voicePlaying = false;
        musicDuckTarget = 1.0f;
    }

    public void update() {
        if (device == 0) return;

        // Smooth music ducking
        if (musicDuckCurrent != musicDuckTarget) {
            float speed = 3.0f; // Duck speed
            if (musicDuckCurrent < musicDuckTarget) {
                musicDuckCurrent = Math.min(musicDuckTarget, musicDuckCurrent + speed * 0.016f);
            } else {
                musicDuckCurrent = Math.max(musicDuckTarget, musicDuckCurrent - speed * 0.016f);
            }
            if (musicSource != -1) {
                alSourcef(musicSource, AL_GAIN, musicVolume * masterVolume * musicDuckCurrent);
            }
        }
    }

    public void setMasterVolume(float v) { masterVolume = Math.max(0, Math.min(1, v)); }
    public void setMusicVolume(float v) { musicVolume = Math.max(0, Math.min(1, v)); }
    public void setSfxVolume(float v) { sfxVolume = Math.max(0, Math.min(1, v)); }
    public void setVoiceVolume(float v) { voiceVolume = Math.max(0, Math.min(1, v)); }

    public void cleanup() {
        if (musicSource != -1) {
            alDeleteSources(musicSource);
        }
        if (context != 0) {
            alcDestroyContext(context);
        }
        if (device != 0) {
            alcCloseDevice(device);
        }
    }
}
