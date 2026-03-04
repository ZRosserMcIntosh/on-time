package com.therushlight.narrative;

import java.util.*;

/**
 * A scene within a chapter. A location with characters, dialogue, and interactable objects.
 */
public class Scene {

    private String id;
    private String background;      // Path to background image
    private String music;           // Path to music file (null = continue previous)
    private String ambience;        // Path to ambient sound loop
    private List<String> characters = new ArrayList<>();  // Character IDs present
    private List<DialogueNode> dialogue = new ArrayList<>();
    private Map<String, String> characterPositions = new HashMap<>(); // "rush" -> "left", "lu" -> "right"
    private String mood;            // "warm", "tense", "grief", "hope" — affects post-processing

    public Scene() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBackground() { return background; }
    public void setBackground(String bg) { this.background = bg; }

    public String getMusic() { return music; }
    public void setMusic(String m) { this.music = m; }

    public String getAmbience() { return ambience; }
    public List<String> getCharacters() { return characters; }
    public List<DialogueNode> getDialogue() { return dialogue; }
    public String getMood() { return mood; }
    public Map<String, String> getCharacterPositions() { return characterPositions; }

    public void addDialogue(DialogueNode node) { dialogue.add(node); }
}
