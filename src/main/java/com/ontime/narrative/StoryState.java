package com.ontime.narrative;

import java.util.*;

/**
 * Persistent state across all 100 chapters of On Time.
 *
 * Every choice, every consequence, every relationship score,
 * every cognitive virtue, and every historical insight lives here.
 *
 * THE LIE this game destroys:
 * "If I do everything right, I can keep everyone safe."
 *
 * THE TRUTH this game teaches:
 * You can't. But you can choose who you become in the wreckage.
 * And you can learn to think clearly while everything burns.
 */
public class StoryState {

    // Progress
    private String currentChapterId;
    private String currentSceneId;
    private int chaptersCompleted = 0;
    private String currentEraId;

    // Boolean flags
    private Map<String, Boolean> flags = new HashMap<>();

    // Numeric variables
    private Map<String, Integer> variables = new HashMap<>();

    // String state
    private Map<String, String> strings = new HashMap<>();

    // Character relationship scores
    private Map<String, Integer> relationships = new HashMap<>();

    // Characters who have died (permanent)
    private Set<String> dead = new HashSet<>();

    // Virtue/vice meters
    private int discipline = 0;
    private int courage = 0;
    private int honesty = 0;
    private int generosity = 0;
    private int patience = 0;
    private int faith = 0;

    // Vice accumulation
    private int pride = 0;
    private int bitterness = 0;
    private int cowardice = 0;
    private int greed = 0;

    // Grief weight
    private int grief = 50;

    // Memories
    private List<String> memories = new ArrayList<>();

    // === NEW: Cognitive Virtue Tracking ===
    // These track how the player thinks, not just what they choose

    // First Principles: did the player separate facts from assumptions?
    private int firstPrinciplesScore = 0;

    // Correlation vs Causation: did the player investigate vs follow mob logic?
    private int causalReasoningScore = 0;

    // Steelman: did the player articulate opponent's position fairly?
    private int steelmanScore = 0;

    // Bayesian Updating: did the player change beliefs with new evidence?
    private int beliefFlexibilityScore = 0;

    // Long-term vs Short-term: did the player think beyond the immediate?
    private int longTermThinkingScore = 0;

    // Emotional Regulation: did the player pause before reacting?
    private int emotionalRegulationScore = 0;

    // Second-Order Effects: did the player consider consequences of consequences?
    private int secondOrderScore = 0;

    // Evidence classification accuracy
    private int factsCorrectlyIdentified = 0;
    private int inferencesCorrectlyIdentified = 0;
    private int emotionsCorrectlyIdentified = 0;
    private int misclassifications = 0;

    // Historical knowledge demonstrated through choices
    private Set<String> historicalInsights = new HashSet<>();

    // Belief states — what the player currently believes about key questions
    // These can be updated as new evidence appears (Bayesian Updating)
    private Map<String, String> beliefs = new HashMap<>();

    // Ripple effects — consequences queued from past choices
    private List<String> pendingRipples = new ArrayList<>();

    // --- Accessors ---

    public String getCurrentChapterId() { return currentChapterId; }
    public void setCurrentChapterId(String id) { this.currentChapterId = id; }

    public String getCurrentSceneId() { return currentSceneId; }
    public void setCurrentSceneId(String id) { this.currentSceneId = id; }

    public String getCurrentEraId() { return currentEraId; }
    public void setCurrentEraId(String id) { this.currentEraId = id; }

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
        if (!memories.contains(memory)) memories.add(memory);
    }
    public boolean hasMemory(String memory) { return memories.contains(memory); }
    public List<String> getMemories() { return Collections.unmodifiableList(memories); }

    // === Cognitive Scores ===

    public void addFirstPrinciples(int d) { firstPrinciplesScore = Math.max(0, firstPrinciplesScore + d); }
    public void addCausalReasoning(int d) { causalReasoningScore = Math.max(0, causalReasoningScore + d); }
    public void addSteelman(int d) { steelmanScore = Math.max(0, steelmanScore + d); }
    public void addBeliefFlexibility(int d) { beliefFlexibilityScore = Math.max(0, beliefFlexibilityScore + d); }
    public void addLongTermThinking(int d) { longTermThinkingScore = Math.max(0, longTermThinkingScore + d); }
    public void addEmotionalRegulation(int d) { emotionalRegulationScore = Math.max(0, emotionalRegulationScore + d); }
    public void addSecondOrder(int d) { secondOrderScore = Math.max(0, secondOrderScore + d); }

    public int getFirstPrinciplesScore() { return firstPrinciplesScore; }
    public int getCausalReasoningScore() { return causalReasoningScore; }
    public int getSteelmanScore() { return steelmanScore; }
    public int getBeliefFlexibilityScore() { return beliefFlexibilityScore; }
    public int getLongTermThinkingScore() { return longTermThinkingScore; }
    public int getEmotionalRegulationScore() { return emotionalRegulationScore; }
    public int getSecondOrderScore() { return secondOrderScore; }

    // Float setters for CognitiveVirtueSystem sync (converts 0.0-1.0 to 0-100 int)
    public void setFirstPrinciples(float v) { firstPrinciplesScore = Math.round(v * 100); }
    public void setCausalReasoning(float v) { causalReasoningScore = Math.round(v * 100); }
    public void setSteelman(float v) { steelmanScore = Math.round(v * 100); }
    public void setBeliefFlexibility(float v) { beliefFlexibilityScore = Math.round(v * 100); }
    public void setLongTermThinking(float v) { longTermThinkingScore = Math.round(v * 100); }
    public void setEmotionalRegulation(float v) { emotionalRegulationScore = Math.round(v * 100); }
    public void setSecondOrder(float v) { secondOrderScore = Math.round(v * 100); }

    // Evidence Board
    public void recordCorrectFact() { factsCorrectlyIdentified++; }
    public void recordCorrectInference() { inferencesCorrectlyIdentified++; }
    public void recordCorrectEmotion() { emotionsCorrectlyIdentified++; }
    public void recordMisclassification() { misclassifications++; }

    // Historical Insights
    public void addHistoricalInsight(String insight) { historicalInsights.add(insight); }
    public boolean hasHistoricalInsight(String insight) { return historicalInsights.contains(insight); }
    public Set<String> getHistoricalInsights() { return Collections.unmodifiableSet(historicalInsights); }

    // Beliefs
    public void setBelief(String topic, String belief) { beliefs.put(topic, belief); }
    public String getBelief(String topic) { return beliefs.getOrDefault(topic, "unknown"); }
    public boolean hasBeliefChanged(String topic, String newBelief) {
        String old = beliefs.get(topic);
        return old != null && !old.equals(newBelief);
    }

    // Ripple effects
    public void queueRipple(String rippleId) { pendingRipples.add(rippleId); }
    public List<String> consumeRipples() {
        List<String> ripples = new ArrayList<>(pendingRipples);
        pendingRipples.clear();
        return ripples;
    }
    public boolean hasRipple(String rippleId) { return pendingRipples.contains(rippleId); }

    /**
     * Get the "character" of the player based on accumulated virtues/vices.
     */
    public String getCharacterArchetype() {
        int virtueTotal = discipline + courage + honesty + generosity + patience + faith;
        int viceTotal = pride + bitterness + cowardice + greed;
        int cognitiveTotal = firstPrinciplesScore + causalReasoningScore + steelmanScore
                + beliefFlexibilityScore + longTermThinkingScore + emotionalRegulationScore;

        if (virtueTotal > viceTotal * 2 && cognitiveTotal > 50) return "steadfast";
        if (viceTotal > virtueTotal * 2) return "broken";
        if (courage > cowardice && honesty > pride) return "scarred_but_good";
        if (bitterness > faith) return "hardened";
        if (cognitiveTotal > virtueTotal && cognitiveTotal > viceTotal) return "thoughtful";
        return "human"; // the most common — mixed, real
    }

    /**
     * Total playtime estimate based on chapters completed.
     * Used to verify we're hitting the 40-50+ hour minimum.
     */
    public float getEstimatedPlaytimeHours() {
        return chaptersCompleted * 0.55f; // ~33 min average per chapter
    }
}
