package com.therushlight.narrative;

/**
 * A state change caused by a choice or event.
 * This is how the game teaches through consequences, not lectures.
 */
public class Effect {

    public enum Type {
        SET_FLAG,           // Set a boolean flag
        SET_VAR,            // Set a numeric variable
        ADD_VAR,            // Add to a numeric variable
        SET_STRING,         // Set a string variable
        ADJUST_RELATIONSHIP,// Change relationship with a character
        KILL_CHARACTER,     // Permanent character death
        ADD_MEMORY,         // Add a memorable moment
        ADD_VIRTUE,         // discipline, courage, honesty, generosity, patience, faith
        ADD_VICE,           // pride, bitterness, cowardice, greed
        ADJUST_GRIEF,       // Change grief level
        PLAY_SOUND,         // Trigger a sound effect
        SCREEN_SHAKE,       // Visual impact
    }

    private Type type;
    private String target;   // Flag name, character name, virtue name, etc.
    private String value;    // "true", "5", "-3", "Drew", etc.

    public Effect() {}

    public Effect(Type type, String target, String value) {
        this.type = type;
        this.target = target;
        this.value = value;
    }

    public Type getType() { return type; }
    public String getTarget() { return target; }
    public String getValue() { return value; }

    public int getIntValue() {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return 0; }
    }

    public boolean getBoolValue() {
        return "true".equalsIgnoreCase(value);
    }

    /**
     * Apply this effect to the story state.
     */
    public void apply(StoryState state) {
        switch (type) {
            case SET_FLAG -> state.setFlag(target, getBoolValue());
            case SET_VAR -> state.setVar(target, getIntValue());
            case ADD_VAR -> state.addVar(target, getIntValue());
            case SET_STRING -> state.setString(target, value);
            case ADJUST_RELATIONSHIP -> state.adjustRelationship(target, getIntValue());
            case KILL_CHARACTER -> state.kill(target);
            case ADD_MEMORY -> state.addMemory(target);
            case ADD_VIRTUE -> applyVirtue(state);
            case ADD_VICE -> applyVice(state);
            case ADJUST_GRIEF -> state.adjustGrief(getIntValue());
            case PLAY_SOUND, SCREEN_SHAKE -> {} // Handled by renderer
        }
    }

    private void applyVirtue(StoryState state) {
        int amount = getIntValue();
        switch (target.toLowerCase()) {
            case "discipline" -> state.addDiscipline(amount);
            case "courage" -> state.addCourage(amount);
            case "honesty" -> state.addHonesty(amount);
            case "generosity" -> state.addGenerosity(amount);
            case "patience" -> state.addPatience(amount);
            case "faith" -> state.addFaith(amount);
        }
    }

    private void applyVice(StoryState state) {
        int amount = getIntValue();
        switch (target.toLowerCase()) {
            case "pride" -> state.addPride(amount);
            case "bitterness" -> state.addBitterness(amount);
            case "cowardice" -> state.addCowardice(amount);
            case "greed" -> state.addGreed(amount);
        }
    }
}
