package com.therushlight.narrative;

import java.util.List;

/**
 * A player choice within a dialogue node.
 * Choices have consequences — relationship changes, flag sets, virtue/vice adjustments.
 */
public class Choice {

    private String text;             // What the player sees
    private String nextNode;         // Where dialogue goes after this choice
    private String nextScene;        // Scene transition (overrides nextNode)
    private List<Effect> effects;    // Consequences
    private Condition condition;     // Only show if condition met
    private String notification;     // "Rush will remember that." (null = none)
    private boolean silent = false;  // True for "[Say nothing]" type options

    public Choice() {}

    public String getText() { return text; }
    public String getNextNode() { return nextNode; }
    public String getNextScene() { return nextScene; }
    public List<Effect> getEffects() { return effects != null ? effects : List.of(); }
    public Condition getCondition() { return condition; }
    public String getNotification() { return notification; }
    public boolean isSilent() { return silent; }
}
