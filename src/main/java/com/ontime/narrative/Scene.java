package com.ontime.narrative;

import java.util.*;

/**
 * A scene within a chapter. A 3D location with characters, dialogue, and interactable objects.
 */
public class Scene {

    private String id;
    private String background;
    private String music;
    private String ambience;
    private List<String> characters = new ArrayList<>();
    private List<DialogueNode> dialogue = new ArrayList<>();
    private Map<String, String> characterPositions = new HashMap<>();
    private String mood;

    // 3D scene properties
    private String environmentModel;    // Path to 3D environment model
    private float[] cameraPosition;     // Starting camera position for this scene
    private float[] cameraTarget;       // Where the camera looks
    private String skybox;              // Skybox texture for outdoor scenes
    private String lightingPreset;      // "golden_hour", "moonlit", "torchlit", etc.

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

    public String getEnvironmentModel() { return environmentModel; }
    public float[] getCameraPosition() { return cameraPosition; }
    public float[] getCameraTarget() { return cameraTarget; }
    public String getSkybox() { return skybox; }
    public String getLightingPreset() { return lightingPreset; }

    public void addDialogue(DialogueNode node) { dialogue.add(node); }
}
