package com.ontime.narrative;

/**
 * A condition that gates dialogue nodes, choices, or chapters.
 */
public class Condition {

    public enum Type {
        FLAG_TRUE, FLAG_FALSE,
        VAR_GREATER, VAR_LESS, VAR_EQUALS,
        CHARACTER_ALIVE, CHARACTER_DEAD,
        RELATIONSHIP_ABOVE, RELATIONSHIP_BELOW,
        VIRTUE_ABOVE, VICE_ABOVE,
        HAS_MEMORY,
        // NEW: Cognitive conditions
        COGNITIVE_ABOVE,        // A cognitive score > value
        HAS_BELIEF,             // Player holds a specific belief
        HAS_HISTORICAL_INSIGHT, // Player has discovered a historical insight
        ERA_ACTIVE,             // Currently in a specific era
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
            case COGNITIVE_ABOVE -> getCognitive(state, target) > getIntValue();
            case HAS_BELIEF -> state.getBelief(target).equals(value);
            case HAS_HISTORICAL_INSIGHT -> state.hasHistoricalInsight(target);
            case ERA_ACTIVE -> target.equals(state.getCurrentEraId());
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

    private int getCognitive(StoryState state, String name) {
        return switch (name.toLowerCase()) {
            case "first_principles" -> state.getFirstPrinciplesScore();
            case "causal_reasoning" -> state.getCausalReasoningScore();
            case "steelman" -> state.getSteelmanScore();
            case "belief_flexibility" -> state.getBeliefFlexibilityScore();
            case "long_term_thinking" -> state.getLongTermThinkingScore();
            case "emotional_regulation" -> state.getEmotionalRegulationScore();
            case "second_order" -> state.getSecondOrderScore();
            default -> 0;
        };
    }
}
