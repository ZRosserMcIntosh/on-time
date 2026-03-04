package com.ontime.narrative;

import java.util.*;

/**
 * A single dialogue node — one speaker, one line, zero or more choices.
 */
public class DialogueNode {

    private String speaker;
    private String text;
    private String emotion;
    private List<Choice> choices;
    private float timer = 0;
    private int timeoutDefault = -1;
    private String nextNode;
    private String nextScene;
    private List<Effect> effects;
    private Condition condition;

    // Cognitive teaching metadata — which principle this node relates to
    private String cognitivePrinciple;  // "first_principles", "correlation_causation", etc.
    private String evidenceType;         // "fact", "inference", "emotion" — for evidence board

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

    public String getCognitivePrinciple() { return cognitivePrinciple; }
    public String getEvidenceType() { return evidenceType; }
}
