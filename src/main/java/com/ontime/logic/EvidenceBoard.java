package com.ontime.logic;

import com.ontime.engine.input.InputHandler;
import com.ontime.engine.input.MouseHandler;
import com.ontime.engine.Window;
import com.ontime.rendering.SceneRenderer;

import java.util.*;

/**
 * The Evidence Board — a player-facing tool for classifying information
 * encountered during dialogue and exploration.
 *
 * Three columns:
 *   • Verified Facts       — things the player can prove
 *   • Inferences           — conclusions drawn from evidence
 *   • Emotional Reactions  — gut feelings, biases, and unverified intuitions
 *
 * Correct classification strengthens cognitive virtues (first-principles,
 * causal reasoning, emotional regulation). Misclassification triggers
 * gentle feedback and a learning opportunity.
 */
public class EvidenceBoard {

    public enum Category {
        FACT, INFERENCE, EMOTION
    }

    public static class Evidence {
        public final String id;
        public final String text;
        public final Category correctCategory;
        public Category playerCategory;
        public boolean classified;

        public Evidence(String id, String text, Category correct) {
            this.id = id;
            this.text = text;
            this.correctCategory = correct;
            this.playerCategory = null;
            this.classified = false;
        }
    }

    // ── State ────────────────────────────────────────────────────

    private final List<Evidence> allEvidence = new ArrayList<>();
    private final List<Evidence> unclassified = new ArrayList<>();
    private final List<Evidence> facts = new ArrayList<>();
    private final List<Evidence> inferences = new ArrayList<>();
    private final List<Evidence> emotions = new ArrayList<>();

    private boolean open = false;
    private int selectedIndex = 0;
    private float scrollOffset = 0;

    private String feedbackMessage = null;
    private float feedbackTimer = 0;

    // ── Public API ───────────────────────────────────────────────

    /**
     * Add a new piece of evidence to the board (unclassified).
     */
    public void addEvidence(String id, String text, String correctCategory) {
        Category cat;
        switch (correctCategory != null ? correctCategory.toLowerCase() : "") {
            case "inference": cat = Category.INFERENCE; break;
            case "emotion":   cat = Category.EMOTION;   break;
            default:          cat = Category.FACT;       break;
        }
        Evidence e = new Evidence(id, text, cat);
        allEvidence.add(e);
        unclassified.add(e);
    }

    /**
     * Player classifies a piece of evidence.
     * Returns true if they got it right.
     */
    public boolean classify(String evidenceId, Category playerChoice) {
        Evidence e = findById(evidenceId);
        if (e == null || e.classified) return false;

        e.playerCategory = playerChoice;
        e.classified = true;
        unclassified.remove(e);

        boolean correct = (playerChoice == e.correctCategory);

        // Sort into display column based on PLAYER's classification
        switch (playerChoice) {
            case FACT:      facts.add(e);      break;
            case INFERENCE: inferences.add(e); break;
            case EMOTION:   emotions.add(e);   break;
        }

        if (!correct) {
            feedbackMessage = "Hmm — consider: is \"" + truncate(e.text, 40)
                    + "\" really a " + playerChoice.name().toLowerCase()
                    + "? It might be more of a " + e.correctCategory.name().toLowerCase() + ".";
            feedbackTimer = 4.0f;
        } else {
            feedbackMessage = "Good classification.";
            feedbackTimer = 2.0f;
        }

        return correct;
    }

    /**
     * Per-frame update. Handles input when the board is open.
     */
    public void update(float dt, MouseHandler mouse, InputHandler input) {
        if (feedbackTimer > 0) {
            feedbackTimer -= dt;
            if (feedbackTimer <= 0) feedbackMessage = null;
        }

        if (!open) return;

        // Keyboard navigation through unclassified items
        if (input != null) {
            // Up/Down to select unclassified evidence
            // 1/2/3 to classify as Fact/Inference/Emotion
            // (Actual key handling deferred to GameEngine integration)
        }
    }

    /**
     * Render the evidence board overlay (delegates to SceneRenderer + UIRenderer).
     */
    public void render(SceneRenderer sceneRenderer, Window window) {
        // Rendering is handled by UIRenderer.renderEvidenceBoardOverlay()
        // This method is kept for future custom rendering on the board itself
    }

    // ── Getters ──────────────────────────────────────────────────

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public void toggle() { this.open = !this.open; }

    public List<Evidence> getUnclassified() { return Collections.unmodifiableList(unclassified); }
    public List<Evidence> getVerifiedFacts() { return Collections.unmodifiableList(facts); }
    public List<Evidence> getInferences() { return Collections.unmodifiableList(inferences); }
    public List<Evidence> getEmotionalReactions() { return Collections.unmodifiableList(emotions); }

    public String getFeedbackMessage() { return feedbackMessage; }
    public float getFeedbackTimer() { return feedbackTimer; }

    public int getTotalEvidence() { return allEvidence.size(); }
    public int getClassifiedCount() { return allEvidence.size() - unclassified.size(); }

    public int getCorrectClassifications() {
        int count = 0;
        for (Evidence e : allEvidence) {
            if (e.classified && e.playerCategory == e.correctCategory) count++;
        }
        return count;
    }

    public int getMisclassifications() {
        int count = 0;
        for (Evidence e : allEvidence) {
            if (e.classified && e.playerCategory != e.correctCategory) count++;
        }
        return count;
    }

    // ── Helpers ──────────────────────────────────────────────────

    private Evidence findById(String id) {
        for (Evidence e : allEvidence) {
            if (e.id.equals(id)) return e;
        }
        return null;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
