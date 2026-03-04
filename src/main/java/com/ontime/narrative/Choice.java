package com.ontime.narrative;

import java.util.List;

/**
 * A player choice within a dialogue node.
 * Choices have consequences — relationship changes, cognitive scores, virtue/vice adjustments.
 */
public class Choice {

    private String text;
    private String nextNode;
    private String nextScene;
    private List<Effect> effects;
    private Condition condition;
    private String notification;
    private boolean silent = false;

    // Cognitive principle this choice relates to
    // null = no principle, just a regular choice
    // "steelman" = player is articulating opponent's view
    // "first_principles" = player is stripping assumptions
    // "bayesian" = player is updating beliefs with new evidence
    private String cognitivePrinciple;

    // Does this choice represent a steelman of Shepherd's position?
    private boolean steelmanChoice = false;

    // Does this choice require emotional regulation (choosing calm over impulse)?
    private boolean regulationChoice = false;

    // Long-term vs short-term
    private String termType; // "short" or "long"

    public Choice() {}

    public String getText() { return text; }
    public String getNextNode() { return nextNode; }
    public String getNextScene() { return nextScene; }
    public List<Effect> getEffects() { return effects != null ? effects : List.of(); }
    public Condition getCondition() { return condition; }
    public String getNotification() { return notification; }
    public boolean isSilent() { return silent; }

    public String getCognitivePrinciple() { return cognitivePrinciple; }
    public boolean isSteelmanChoice() { return steelmanChoice; }
    public boolean isRegulationChoice() { return regulationChoice; }
    public String getTermType() { return termType; }
}
