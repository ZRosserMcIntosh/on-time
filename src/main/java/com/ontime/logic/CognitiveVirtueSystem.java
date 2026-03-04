package com.ontime.logic;

import com.ontime.narrative.StoryState;

/**
 * Tracks and evolves the player's cognitive virtues based on Virgil's 12 principles.
 *
 * The 12 Cognitive Principles:
 *  1. First Principles Thinking
 *  2. Correlation vs Causation (Causal Reasoning)
 *  3. Incentives Drive Behavior
 *  4. Steelman Principle
 *  5. Opportunity Cost
 *  6. Long-Term vs Short-Term Thinking
 *  7. Emotional Regulation
 *  8. Bayesian Updating (Belief Flexibility)
 *  9. Moral Tradeoff Awareness
 * 10. Tragedy of the Commons
 * 11. Second-Order Effects
 * 12. The Danger of Certainty
 *
 * These are distilled into 5 composite display virtues for the HUD:
 *   Humility   = avg(bayesianUpdating, dangerOfCertainty)
 *   Patience   = avg(longTermThinking, opportunityCost)
 *   Clarity    = avg(firstPrinciples, causalReasoning, secondOrderEffects)
 *   Empathy    = avg(steelman, moralTradeoff, tragedyOfCommons)
 *   Resolve    = avg(emotionalRegulation, incentivesAwareness)
 */
public class CognitiveVirtueSystem {

    // ── Raw principle scores (0.0 – 1.0) ────────────────────────

    private float firstPrinciples       = 0.0f;
    private float causalReasoning       = 0.0f;
    private float incentivesAwareness   = 0.0f;
    private float steelman              = 0.0f;
    private float opportunityCost       = 0.0f;
    private float longTermThinking      = 0.0f;
    private float emotionalRegulation   = 0.0f;
    private float bayesianUpdating      = 0.0f;
    private float moralTradeoff         = 0.0f;
    private float tragedyOfCommons      = 0.0f;
    private float secondOrderEffects    = 0.0f;
    private float dangerOfCertainty     = 0.0f;

    // ── Engagement tracking ──────────────────────────────────────

    private int steelmanAttempts        = 0;
    private int steelmanSuccesses       = 0;
    private int regulationChoices       = 0;
    private int longTermChoices         = 0;
    private int shortTermChoices        = 0;
    private int evidencePresented       = 0;
    private int principlesEngaged       = 0;

    // ── Composite HUD virtues ────────────────────────────────────

    public float getHumility() {
        return clamp((bayesianUpdating + dangerOfCertainty) / 2.0f);
    }

    public float getPatience() {
        return clamp((longTermThinking + opportunityCost) / 2.0f);
    }

    public float getClarity() {
        return clamp((firstPrinciples + causalReasoning + secondOrderEffects) / 3.0f);
    }

    public float getEmpathy() {
        return clamp((steelman + moralTradeoff + tragedyOfCommons) / 3.0f);
    }

    public float getResolve() {
        return clamp((emotionalRegulation + incentivesAwareness) / 2.0f);
    }

    // ── Per-frame update ─────────────────────────────────────────

    /**
     * Called every frame. Smoothly decays/grows scores toward equilibrium
     * and syncs composite values into StoryState for narrative branching.
     */
    public void update(float dt, StoryState state) {
        // Slow natural decay — virtues atrophy without practice
        float decay = 0.001f * dt;
        firstPrinciples     = clamp(firstPrinciples     - decay);
        causalReasoning      = clamp(causalReasoning      - decay);
        incentivesAwareness  = clamp(incentivesAwareness  - decay);
        steelman             = clamp(steelman             - decay);
        opportunityCost      = clamp(opportunityCost      - decay);
        longTermThinking     = clamp(longTermThinking     - decay);
        emotionalRegulation  = clamp(emotionalRegulation  - decay);
        bayesianUpdating     = clamp(bayesianUpdating     - decay);
        moralTradeoff        = clamp(moralTradeoff        - decay);
        tragedyOfCommons     = clamp(tragedyOfCommons     - decay);
        secondOrderEffects   = clamp(secondOrderEffects   - decay);
        dangerOfCertainty    = clamp(dangerOfCertainty    - decay);

        // Sync into StoryState so narrative conditions can read them
        syncToState(state);
    }

    // ── Era entrance bonus ───────────────────────────────────────

    /**
     * Called when the player enters a historical era. Provides a small boost
     * to the cognitive principle associated with that era.
     */
    public void onEraEnter(com.ontime.history.HistoricalEra era) {
        if (era == null || era.getCognitivePrinciple() == null) return;
        boostPrinciple(era.getCognitivePrinciple(), 0.05f);
        System.out.println("[Cognitive] Era '" + era.getName()
                + "' entered — boosted " + era.getCognitivePrinciple());
    }

    // ── Event callbacks ──────────────────────────────────────────

    /**
     * Called when the Evidence Board classifies a piece of evidence.
     */
    public void onEvidencePresented(String type, String text) {
        evidencePresented++;
        switch (type != null ? type.toLowerCase() : "") {
            case "fact":
                boostPrinciple("first_principles", 0.03f);
                boostPrinciple("causal_reasoning", 0.02f);
                break;
            case "inference":
                boostPrinciple("causal_reasoning", 0.03f);
                boostPrinciple("second_order", 0.02f);
                break;
            case "emotion":
                boostPrinciple("emotional_regulation", 0.03f);
                boostPrinciple("danger_of_certainty", 0.02f);
                break;
        }
    }

    /**
     * Called when a dialogue choice engages a cognitive principle.
     */
    public void onPrincipleEngaged(String principle) {
        principlesEngaged++;
        boostPrinciple(principle, 0.04f);
    }

    /**
     * Called when the player attempts a steelman choice.
     */
    public void onSteelmanAttempt(boolean success) {
        steelmanAttempts++;
        if (success) {
            steelmanSuccesses++;
            boostPrinciple("steelman", 0.06f);
            boostPrinciple("moral_tradeoff", 0.02f);
        } else {
            boostPrinciple("steelman", 0.02f); // effort still counts
        }
    }

    /**
     * Called when the player makes a deliberate emotional-regulation choice
     * (e.g. pausing to breathe, choosing calm response over reactive one).
     */
    public void onRegulationChoice() {
        regulationChoices++;
        boostPrinciple("emotional_regulation", 0.05f);
        boostPrinciple("long_term", 0.02f);
    }

    /**
     * Called when the player explicitly chooses a long-term or short-term option.
     */
    public void onTermChoice(boolean longTerm) {
        if (longTerm) {
            longTermChoices++;
            boostPrinciple("long_term", 0.05f);
            boostPrinciple("opportunity_cost", 0.03f);
        } else {
            shortTermChoices++;
            // Short-term isn't always wrong — but it doesn't boost these
            boostPrinciple("incentives", 0.02f);
        }
    }

    /**
     * Called when the player updates a belief in light of new evidence.
     */
    public void onBeliefUpdated() {
        boostPrinciple("bayesian_updating", 0.05f);
        boostPrinciple("danger_of_certainty", 0.03f);
    }

    /**
     * Called when the player recognises a commons-type dilemma.
     */
    public void onCommonsRecognised() {
        boostPrinciple("tragedy_of_commons", 0.05f);
        boostPrinciple("second_order", 0.03f);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void boostPrinciple(String principle, float amount) {
        if (principle == null) return;
        switch (principle.toLowerCase().replace(" ", "_")) {
            case "first_principles":     firstPrinciples     = clamp(firstPrinciples     + amount); break;
            case "causal_reasoning":
            case "correlation_causation": causalReasoning      = clamp(causalReasoning      + amount); break;
            case "incentives":
            case "incentives_drive":      incentivesAwareness  = clamp(incentivesAwareness  + amount); break;
            case "steelman":              steelman             = clamp(steelman             + amount); break;
            case "opportunity_cost":      opportunityCost      = clamp(opportunityCost      + amount); break;
            case "long_term":
            case "long_term_thinking":    longTermThinking     = clamp(longTermThinking     + amount); break;
            case "emotional_regulation":  emotionalRegulation  = clamp(emotionalRegulation  + amount); break;
            case "bayesian_updating":
            case "bayesian":              bayesianUpdating     = clamp(bayesianUpdating     + amount); break;
            case "moral_tradeoff":
            case "moral_tradeoffs":       moralTradeoff        = clamp(moralTradeoff        + amount); break;
            case "tragedy_of_commons":
            case "commons":               tragedyOfCommons     = clamp(tragedyOfCommons     + amount); break;
            case "second_order":
            case "second_order_effects":  secondOrderEffects   = clamp(secondOrderEffects   + amount); break;
            case "danger_of_certainty":
            case "certainty":             dangerOfCertainty    = clamp(dangerOfCertainty    + amount); break;
        }
    }

    private void syncToState(StoryState state) {
        state.setFirstPrinciples(firstPrinciples);
        state.setCausalReasoning(causalReasoning);
        state.setSteelman(steelman);
        state.setBeliefFlexibility(bayesianUpdating);
        state.setLongTermThinking(longTermThinking);
        state.setEmotionalRegulation(emotionalRegulation);
        state.setSecondOrder(secondOrderEffects);
    }

    private float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    // ── Getters for raw principles ───────────────────────────────

    public float getFirstPrinciples()      { return firstPrinciples; }
    public float getCausalReasoning()       { return causalReasoning; }
    public float getIncentivesAwareness()   { return incentivesAwareness; }
    public float getSteelman()              { return steelman; }
    public float getOpportunityCost()       { return opportunityCost; }
    public float getLongTermThinking()      { return longTermThinking; }
    public float getEmotionalRegulation()   { return emotionalRegulation; }
    public float getBayesianUpdating()      { return bayesianUpdating; }
    public float getMoralTradeoff()         { return moralTradeoff; }
    public float getTragedyOfCommons()      { return tragedyOfCommons; }
    public float getSecondOrderEffects()    { return secondOrderEffects; }
    public float getDangerOfCertainty()     { return dangerOfCertainty; }

    // ── Stats ────────────────────────────────────────────────────

    public int getSteelmanAttempts()        { return steelmanAttempts; }
    public int getSteelmanSuccesses()       { return steelmanSuccesses; }
    public int getRegulationChoices()       { return regulationChoices; }
    public int getLongTermChoices()          { return longTermChoices; }
    public int getShortTermChoices()        { return shortTermChoices; }
}
