package com.ontime.narrative;

/**
 * A state change caused by a choice or event.
 * This is how On Time teaches through consequences, not lectures.
 */
public class Effect {

    public enum Type {
        SET_FLAG, SET_VAR, ADD_VAR, SET_STRING,
        ADJUST_RELATIONSHIP, KILL_CHARACTER,
        ADD_MEMORY, ADD_VIRTUE, ADD_VICE, ADJUST_GRIEF,
        PLAY_SOUND, SCREEN_SHAKE,
        // NEW: Cognitive effects
        ADD_COGNITIVE,          // Add to a cognitive score
        SET_BELIEF,             // Update player's belief about something (Bayesian)
        ADD_EVIDENCE,           // Add item to evidence board
        CLASSIFY_EVIDENCE,      // Player classified evidence (check if correct)
        ADD_HISTORICAL_INSIGHT, // Player learned something about history
        QUEUE_RIPPLE,           // Queue a consequence for later (second-order effects)
        ADD_OPPORTUNITY_COST,   // Record what was sacrificed by this choice
    }

    private Type type;
    private String target;
    private String value;

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
            case ADD_COGNITIVE -> applyCognitive(state);
            case SET_BELIEF -> state.setBelief(target, value);
            case ADD_HISTORICAL_INSIGHT -> state.addHistoricalInsight(target);
            case QUEUE_RIPPLE -> state.queueRipple(target);
            case ADD_OPPORTUNITY_COST -> state.addMemory("opportunity_cost:" + target + ":" + value);
            case PLAY_SOUND, SCREEN_SHAKE, ADD_EVIDENCE, CLASSIFY_EVIDENCE -> {}
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

    private void applyCognitive(StoryState state) {
        int amount = getIntValue();
        switch (target.toLowerCase()) {
            case "first_principles" -> state.addFirstPrinciples(amount);
            case "causal_reasoning" -> state.addCausalReasoning(amount);
            case "steelman" -> state.addSteelman(amount);
            case "belief_flexibility" -> state.addBeliefFlexibility(amount);
            case "long_term_thinking" -> state.addLongTermThinking(amount);
            case "emotional_regulation" -> state.addEmotionalRegulation(amount);
            case "second_order" -> state.addSecondOrder(amount);
        }
    }
}
