package com.ontime.narrative;

import java.util.*;

/**
 * A single chapter in On Time. Contains scenes and metadata.
 * Chapters are loaded from JSON files in /resources/chapters/
 */
public class Chapter {

    private String id;
    private int number;
    private String title;
    private String subtitle;
    private String firstSceneId;
    private String eraId;            // Which historical era this chapter takes place in
    private String cognitivePrinciple; // Which logic principle this chapter teaches
    private Map<String, Scene> scenes = new LinkedHashMap<>();
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
    public String getEraId() { return eraId; }
    public String getCognitivePrinciple() { return cognitivePrinciple; }

    public void setFirstSceneId(String id) { this.firstSceneId = id; }
    public void setEraId(String eraId) { this.eraId = eraId; }

    public Scene getScene(String sceneId) { return scenes.get(sceneId); }

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
