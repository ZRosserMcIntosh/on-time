package com.therushlight.narrative;

/**
 * A condition that gates whether a dialogue node, choice, or chapter is available.
 * Conditions are checked against the current StoryState.
 */
public class Condition {

    public enum Type {
        FLAG_TRUE,          // A flag must be true
        FLAG_FALSE,         // A flag must be false
        VAR_GREATER,        // A variable must be > value
        VAR_LESS,           // A variable must be < value
        VAR_EQUALS,         // A variable must == value
        CHARACTER_ALIVE,    // A character must be alive
        CHARACTER_DEAD,     // A character must be dead
        RELATIONSHIP_ABOVE, // Relationship with character > value
        RELATIONSHIP_BELOW, // Relationship with character < value
        VIRTUE_ABOVE,       // A virtue > value
        VICE_ABOVE,         // A vice > value
        HAS_MEMORY,         // Player has a specific memory
    }

    private Type type;
    private String target;
    private String value;

    public Condition() {}

    public Condition(Type type, String target, String value) {
        this.type = type;
        this.target = target;
        this.value = value;
    }

    public Type getType() { return type; }
    public String getTarget() { return target; }

    public int getIntValue() {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return 0; }
    }

    public boolean isMet(StoryState state) {
        return switch (type) {
            case FLAG_TRUE -> state.getFlag(target);
            case FLAG_FALSE -> !state.getFlag(target);
            case VAR_GREATER -> state.getVar(target) > getIntValue();
            case VAR_LESS -> state.getVar(target) < getIntValue();
            case VAR_EQUALS -> state.getVar(target) == getIntValue();
            case CHARACTER_ALIVE -> !state.isDead(target);
            case CHARACTER_DEAD -> state.isDead(target);
            case RELATIONSHIP_ABOVE -> state.getRelationship(target) > getIntValue();
            case RELATIONSHIP_BELOW -> state.getRelationship(target) < getIntValue();
            case VIRTUE_ABOVE -> getVirtue(state, target) > getIntValue();
            case VICE_ABOVE -> getVice(state, target) > getIntValue();
            case HAS_MEMORY -> state.hasMemory(target);
        };
    }

    private int getVirtue(StoryState state, String name) {
        return switch (name.toLowerCase()) {
            case "discipline" -> state.getDiscipline();
            case "courage" -> state.getCourage();
            case "honesty" -> state.getHonesty();
            case "generosity" -> state.getGenerosity();
            case "patience" -> state.getPatience();
            case "faith" -> state.getFaith();
            default -> 0;
        };
    }

    private int getVice(StoryState state, String name) {
        return switch (name.toLowerCase()) {
            case "pride" -> state.getPride();
            case "bitterness" -> state.getBitterness();
            case "cowardice" -> state.getCowardice();
            case "greed" -> state.getGreed();
            default -> 0;
        };
    }
}
