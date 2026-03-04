package com.therushlight.narrative;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;

/**
 * Handles saving and loading game progress.
 * Saves go to a local directory, not inside the JAR.
 */
public class SaveManager {

    private static final String SAVE_DIR = "saves";
    private static final String SAVE_FILE = "story.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static StoryState loadOrCreate() {
        Path savePath = getSavePath();
        if (Files.exists(savePath)) {
            try {
                String json = Files.readString(savePath);
                StoryState state = gson.fromJson(json, StoryState.class);
                System.out.println("[Save] Loaded save — Chapter: " + state.getCurrentChapterId()
                        + ", Chapters completed: " + state.getChaptersCompleted());
                return state;
            } catch (Exception e) {
                System.err.println("[Save] Failed to load save, starting fresh: " + e.getMessage());
            }
        }
        System.out.println("[Save] No save found. Starting new story.");
        return new StoryState();
    }

    public static void save(StoryState state) {
        try {
            Path savePath = getSavePath();
            Files.createDirectories(savePath.getParent());
            String json = gson.toJson(state);
            Files.writeString(savePath, json);
            System.out.println("[Save] Progress saved.");
        } catch (Exception e) {
            System.err.println("[Save] Failed to save: " + e.getMessage());
        }
    }

    public static void deleteSave() {
        try {
            Files.deleteIfExists(getSavePath());
            System.out.println("[Save] Save deleted.");
        } catch (Exception e) {
            System.err.println("[Save] Failed to delete: " + e.getMessage());
        }
    }

    private static Path getSavePath() {
        // Save next to the JAR/executable
        String userDir = System.getProperty("user.dir");
        return Path.of(userDir, SAVE_DIR, SAVE_FILE);
    }
}
