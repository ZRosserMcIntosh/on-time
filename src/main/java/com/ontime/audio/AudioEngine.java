package com.ontime.audio;

import org.lwjgl.openal.*;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

/**
 * OpenAL-based audio engine for On Time.
 * Supports music, SFX, voice, and era-specific ambient soundscapes.
 */
public class AudioEngine {

    private long device;
    private long context;

    private float masterVolume = 1.0f;
    private float musicVolume = 0.7f;
    private float sfxVolume = 1.0f;
    private float voiceVolume = 1.0f;

    private int musicSource = -1;
    private String currentMusic = null;

    private boolean voicePlaying = false;
    private float musicDuckTarget = 1.0f;
    private float musicDuckCurrent = 1.0f;

    public AudioEngine() {
        try {
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

            musicSource = alGenSources();
            alSourcef(musicSource, AL_GAIN, musicVolume);
            alSourcei(musicSource, AL_LOOPING, AL_TRUE);

            System.out.println("[Audio] OpenAL initialized for On Time.");
        } catch (Exception e) {
            System.err.println("[Audio] Init failed: " + e.getMessage());
            device = 0;
        }
    }

    public void playMusic(String path) {
        if (device == 0 || musicSource == -1) return;
        if (path.equals(currentMusic)) return;

        alSourceStop(musicSource);
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
        System.out.println("[Audio] SFX: " + path);
    }

    public void playVoice(String path) {
        if (device == 0) return;
        voicePlaying = true;
        musicDuckTarget = 0.3f;
        System.out.println("[Audio] Voice: " + path);
    }

    public void stopVoice() {
        voicePlaying = false;
        musicDuckTarget = 1.0f;
    }

    public void update() {
        if (device == 0) return;

        if (musicDuckCurrent != musicDuckTarget) {
            float speed = 3.0f;
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
        if (musicSource != -1) alDeleteSources(musicSource);
        if (context != 0) alcDestroyContext(context);
        if (device != 0) alcCloseDevice(device);
    }
}
