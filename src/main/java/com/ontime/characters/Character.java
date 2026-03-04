package com.ontime.characters;

/**
 * A character in On Time. Tracks name, display info, relationship to player,
 * and current emotional/alive state.
 */
public class Character {

    private String id;
    private String displayName;
    private String description;
    private String spriteBase;
    private String modelPath;       // Path to 3D character model
    private String currentEmotion = "neutral";
    private boolean alive = true;
    private boolean present = false;

    private float colorR, colorG, colorB;

    public Character() {}

    public Character(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getSpriteBase() { return spriteBase; }
    public String getModelPath() { return modelPath; }
    public String getCurrentEmotion() { return currentEmotion; }
    public boolean isAlive() { return alive; }
    public boolean isPresent() { return present; }

    public void setCurrentEmotion(String emotion) { this.currentEmotion = emotion; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public void setPresent(boolean present) { this.present = present; }
    public void setColor(float r, float g, float b) { colorR = r; colorG = g; colorB = b; }
    public void setModelPath(String path) { this.modelPath = path; }

    public float getColorR() { return colorR; }
    public float getColorG() { return colorG; }
    public float getColorB() { return colorB; }
}
