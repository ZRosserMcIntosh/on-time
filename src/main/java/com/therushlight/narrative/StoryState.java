package com.therushlight.narrative;

import java.util.*;

/**
 * Persistent state across all 100 chapters.
 *
 * Every choice, every consequence, every relationship score
 * lives here. This is serialized to JSON for save/load.
 *
 * THE LIE this game destroys:
 * "If I do everything right, I can keep everyone safe."
 */
public class StoryState {

    // Progress
    private String currentChapterId;
    private String currentSceneId;
    private int chaptersCompleted = 0;

    // Boolean flags: "drew_alive", "told_truth_at_river", "found_the_letter"
    private Map<String, Boolean> flags = new HashMap<>();

    // Numeric variables: "rush_trust": 7, "supplies": 3, "courage": 12
    private Map<String, Integer> variables = new HashMap<>();

    // String state: "chapter_5_choice": "protected_lu"
    private Map<String, String> strings = new HashMap<>();

    // Character relationship scores
    private Map<String, Integer> relationships = new HashMap<>();

    // Characters who have died (permanent)
    private Set<String> dead = new HashSet<>();

    // Virtue/vice meters — these are the "skill levels" that build through repeated behavior
    private int discipline = 0;    // Built by choosing hard-right over easy-wrong
    private int courage = 0;       // Built by acting under fear
    private int honesty = 0;       // Built by truth-telling when it costs
    private int generosity = 0;    // Built by giving when you can't afford to
    private int patience = 0;      // Built by choosing long-term over short-term
    private int faith = 0;         // Built by trusting when you can't see

    // Vice accumulation — temptation mechanics
    private int pride = 0;         // Built by choosing self over others
    private int bitterness = 0;    // Built by refusing to forgive
    private int cowardice = 0;     // Built by avoiding hard things
    private int greed = 0;         // Built by hoarding, taking

    // The "weight" — how much Drew's death is affecting the remaining siblings
    private int grief = 50;        // Starts at 50. Can go up or down.

    // Memories — key moments that NPCs can reference
    private List<String> memories = new ArrayList<>();

    // --- Accessors ---

    public String getCurrentChapterId() { return currentChapterId; }
    public void setCurrentChapterId(String id) { this.currentChapterId = id; }

    public String getCurrentSceneId() { return currentSceneId; }
    public void setCurrentSceneId(String id) { this.currentSceneId = id; }

    public int getChaptersCompleted() { return chaptersCompleted; }
    public void completeChapter() { chaptersCompleted++; }

    // Flags
    public boolean getFlag(String key) { return flags.getOrDefault(key, false); }
    public void setFlag(String key, boolean value) { flags.put(key, value); }

    // Variables
    public int getVar(String key) { return variables.getOrDefault(key, 0); }
    public void setVar(String key, int value) { variables.put(key, value); }
    public void addVar(String key, int delta) { variables.put(key, getVar(key) + delta); }

    // Strings
    public String getString(String key) { return strings.getOrDefault(key, ""); }
    public void setString(String key, String value) { strings.put(key, value); }

    // Relationships
    public int getRelationship(String character) { return relationships.getOrDefault(character, 50); }
    public void adjustRelationship(String character, int delta) {
        int current = getRelationship(character);
        relationships.put(character, Math.max(0, Math.min(100, current + delta)));
    }

    // Death
    public boolean isDead(String character) { return dead.contains(character); }
    public void kill(String character) { dead.add(character); }

    // Virtues
    public void addDiscipline(int d) { discipline = Math.max(0, discipline + d); }
    public void addCourage(int d) { courage = Math.max(0, courage + d); }
    public void addHonesty(int d) { honesty = Math.max(0, honesty + d); }
    public void addGenerosity(int d) { generosity = Math.max(0, generosity + d); }
    public void addPatience(int d) { patience = Math.max(0, patience + d); }
    public void addFaith(int d) { faith = Math.max(0, faith + d); }

    public int getDiscipline() { return discipline; }
    public int getCourage() { return courage; }
    public int getHonesty() { return honesty; }
    public int getGenerosity() { return generosity; }
    public int getPatience() { return patience; }
    public int getFaith() { return faith; }

    // Vices
    public void addPride(int d) { pride = Math.max(0, pride + d); }
    public void addBitterness(int d) { bitterness = Math.max(0, bitterness + d); }
    public void addCowardice(int d) { cowardice = Math.max(0, cowardice + d); }
    public void addGreed(int d) { greed = Math.max(0, greed + d); }

    public int getPride() { return pride; }
    public int getBitterness() { return bitterness; }
    public int getCowardice() { return cowardice; }
    public int getGreed() { return greed; }

    // Grief
    public int getGrief() { return grief; }
    public void adjustGrief(int delta) { grief = Math.max(0, Math.min(100, grief + delta)); }

    // Memories
    public void addMemory(String memory) {
        if (!memories.contains(memory)) {
            memories.add(memory);
        }
    }
    public boolean hasMemory(String memory) { return memories.contains(memory); }
    public List<String> getMemories() { return Collections.unmodifiableList(memories); }

    /**
     * Get the "character" of the player based on accumulated virtues/vices.
     * This determines the ending.
     */
    public String getCharacterArchetype() {
        int virtueTotal = discipline + courage + honesty + generosity + patience + faith;
        int viceTotal = pride + bitterness + cowardice + greed;

        if (virtueTotal > viceTotal * 2) return "steadfast";
        if (viceTotal > virtueTotal * 2) return "broken";
        if (courage > cowardice && honesty > pride) return "scarred_but_good";
        if (bitterness > faith) return "hardened";
        return "human"; // the most common — mixed, real
    }
}
