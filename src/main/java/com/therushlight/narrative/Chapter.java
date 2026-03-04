package com.therushlight.narrative;

import java.util.*;

/**
 * A single chapter in the story. Contains scenes, and metadata.
 * Chapters are loaded from JSON files in /resources/chapters/
 */
public class Chapter {

    private String id;
    private int number;
    private String title;
    private String subtitle;        // e.g., "Three months before"
    private String firstSceneId;
    private Map<String, Scene> scenes = new LinkedHashMap<>();

    // Conditions that must be true for this chapter to be accessible
    private List<Condition> requires;

    public Chapter() {}

    public Chapter(String id, int number, String title) {
        this.id = id;
        this.number = number;
        this.title = title;
    }

    public String getId() { return id; }
    public int getNumber() { return number; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getFirstSceneId() { return firstSceneId; }

    public void setFirstSceneId(String id) { this.firstSceneId = id; }

    public Scene getScene(String sceneId) {
        return scenes.get(sceneId);
    }

    public void addScene(String id, Scene scene) {
        scenes.put(id, scene);
        if (firstSceneId == null) firstSceneId = id;
    }

    public Collection<Scene> getScenes() { return scenes.values(); }

    public boolean meetsRequirements(StoryState state) {
        if (requires == null) return true;
        return requires.stream().allMatch(c -> c.isMet(state));
    }
}
