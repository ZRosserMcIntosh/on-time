package com.ontime.narrative;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ontime.i18n.LocaleManager;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Loads chapters from JSON resource files.
 * Respects the current locale — tries locale-specific chapter first,
 * then falls back to the default (English) chapter.
 */
public class ChapterManager {

    private final Gson gson;

    public ChapterManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Chapter loadChapter(String chapterId) {
        // Try locale-specific chapter first (e.g. /chapters/pt-BR/chapter_1.json)
        String path = LocaleManager.getInstance().getChapterResourcePath(chapterId);
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                // Fall back to default English path
                String fallback = "/chapters/" + chapterId + ".json";
                if (!fallback.equals(path)) {
                    System.out.println("[i18n] Localized chapter not found: " + path + " → falling back to " + fallback);
                    try (InputStream fallbackIs = getClass().getResourceAsStream(fallback)) {
                        if (fallbackIs != null) {
                            String json = new String(fallbackIs.readAllBytes(), StandardCharsets.UTF_8);
                            return gson.fromJson(json, Chapter.class);
                        }
                    }
                }
                System.err.println("Chapter file not found: " + path);
                return createPlaceholder(chapterId);
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(json, Chapter.class);
        } catch (Exception e) {
            System.err.println("Failed to load chapter " + chapterId + ": " + e.getMessage());
            return createPlaceholder(chapterId);
        }
    }

    private Chapter createPlaceholder(String chapterId) {
        Chapter chapter = new Chapter(chapterId, 0, "Chapter: " + chapterId);

        Scene scene = new Scene();
        scene.setId("start");
        scene.setBackground(null);

        String placeholderJson = """
                {
                    "speaker": null,
                    "text": "This chapter is still being written. The story continues...",
                    "emotion": "neutral",
                    "choices": [],
                    "nextScene": null
                }
                """;
        DialogueNode node = new Gson().fromJson(placeholderJson, DialogueNode.class);
        scene.addDialogue(node);

        chapter.addScene("start", scene);
        return chapter;
    }
}
