package com.therushlight.narrative;

import java.util.*;

/**
 * A single dialogue node — one speaker, one line, zero or more choices.
 * If choices is empty, the dialogue auto-advances after a pause.
 */
public class DialogueNode {

    private String speaker;          // Character ID, or null for narration
    private String text;             // The line
    private String emotion;          // "neutral", "sad", "angry", "scared", "gentle", "broken"
    private List<Choice> choices;    // Player choices (empty = auto-advance)
    private float timer = 0;         // Seconds to choose (0 = no timer)
    private int timeoutDefault = -1; // Which choice fires if timer runs out (-1 = none)
    private String nextNode;         // If no choices, go to this node ID
    private String nextScene;        // If set, transition to this scene after
    private List<Effect> effects;    // State changes that happen when this node is shown
    private Condition condition;     // Only show this node if condition is met

    public DialogueNode() {}

    public String getSpeaker() { return speaker; }
    public String getText() { return text; }
    public String getEmotion() { return emotion != null ? emotion : "neutral"; }
    public List<Choice> getChoices() { return choices != null ? choices : List.of(); }
    public float getTimer() { return timer; }
    public int getTimeoutDefault() { return timeoutDefault; }
    public String getNextNode() { return nextNode; }
    public String getNextScene() { return nextScene; }
    public List<Effect> getEffects() { return effects != null ? effects : List.of(); }
    public Condition getCondition() { return condition; }
    public boolean hasChoices() { return choices != null && !choices.isEmpty(); }
}
