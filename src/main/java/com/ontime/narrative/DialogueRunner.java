package com.ontime.narrative;

import com.ontime.engine.Window;
import com.ontime.engine.input.InputHandler;
import com.ontime.engine.input.MouseHandler;
import com.ontime.logic.CognitiveVirtueSystem;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Runs dialogue for the current scene. Manages current node,
 * choice selection, timers, typewriter text reveal, and cognitive teaching.
 */
public class DialogueRunner {

    private final StoryState storyState;
    private final CognitiveVirtueSystem cognitiveSystem;

    private Scene currentScene;
    private List<DialogueNode> nodes;
    private int currentNodeIndex = 0;
    private DialogueNode currentNode;

    // Typewriter
    private String fullText = "";
    private int revealedChars = 0;
    private float charTimer = 0;
    private static final float CHARS_PER_SECOND = 40f;
    private boolean textFullyRevealed = false;

    // Choice
    private int selectedChoice = 0;
    private float choiceTimer = 0;
    private boolean waitingForChoice = false;
    private boolean waitingForAdvance = false;

    // Scene transition
    private boolean waitingForTransition = false;
    private String nextSceneId = null;

    // Notifications
    private final List<Notification> notifications = new ArrayList<>();

    public DialogueRunner(StoryState storyState, CognitiveVirtueSystem cognitiveSystem) {
        this.storyState = storyState;
        this.cognitiveSystem = cognitiveSystem;
    }

    public void startScene(Scene scene) {
        this.currentScene = scene;
        this.nodes = scene.getDialogue();
        this.currentNodeIndex = 0;
        this.waitingForTransition = false;
        this.nextSceneId = null;
        advanceToNode(0);
    }

    private void advanceToNode(int index) {
        while (index < nodes.size()) {
            DialogueNode node = nodes.get(index);
            if (node.getCondition() == null || node.getCondition().isMet(storyState)) {
                break;
            }
            index++;
        }

        if (index >= nodes.size()) {
            currentNode = null;
            return;
        }

        currentNodeIndex = index;
        currentNode = nodes.get(index);

        for (Effect effect : currentNode.getEffects()) {
            effect.apply(storyState);
        }

        // Track evidence type for the evidence board
        if (currentNode.getEvidenceType() != null) {
            cognitiveSystem.onEvidencePresented(currentNode.getEvidenceType(), currentNode.getText());
        }

        fullText = currentNode.getText() != null ? currentNode.getText() : "";
        revealedChars = 0;
        charTimer = 0;
        textFullyRevealed = false;
        selectedChoice = 0;

        if (currentNode.hasChoices()) {
            waitingForChoice = true;
            waitingForAdvance = false;
            choiceTimer = currentNode.getTimer();
        } else {
            waitingForChoice = false;
            waitingForAdvance = true;
        }
    }

    public void update(float dt, MouseHandler mouse, InputHandler input, Window window) {
        if (currentNode == null) return;

        // Typewriter reveal
        if (!textFullyRevealed) {
            charTimer += dt * CHARS_PER_SECOND;
            while (charTimer >= 1.0f && revealedChars < fullText.length()) {
                revealedChars++;
                charTimer -= 1.0f;
            }
            if (revealedChars >= fullText.length()) {
                textFullyRevealed = true;
            }

            if (input.isKeyPressed(GLFW_KEY_SPACE) || mouse.isLeftButtonPressed()) {
                revealedChars = fullText.length();
                textFullyRevealed = true;
            }
            return;
        }

        // Waiting for advance
        if (waitingForAdvance) {
            if (input.isKeyPressed(GLFW_KEY_SPACE) || input.isKeyPressed(GLFW_KEY_ENTER)
                    || mouse.isLeftButtonPressed()) {
                advanceDialogue();
            }
            return;
        }

        // Waiting for choice
        if (waitingForChoice) {
            List<Choice> validChoices = getValidChoices();

            // Timer countdown
            if (choiceTimer > 0) {
                choiceTimer -= dt;
                if (choiceTimer <= 0) {
                    int defaultIdx = currentNode.getTimeoutDefault();
                    if (defaultIdx >= 0 && defaultIdx < validChoices.size()) {
                        makeChoice(validChoices.get(defaultIdx));
                    } else {
                        advanceDialogue();
                    }
                    return;
                }
            }

            // Navigate
            if (input.isKeyPressed(GLFW_KEY_W) || input.isKeyPressed(GLFW_KEY_UP)) {
                selectedChoice = Math.max(0, selectedChoice - 1);
            }
            if (input.isKeyPressed(GLFW_KEY_S) || input.isKeyPressed(GLFW_KEY_DOWN)) {
                selectedChoice = Math.min(validChoices.size() - 1, selectedChoice + 1);
            }

            // Number keys
            for (int i = 0; i < Math.min(validChoices.size(), 4); i++) {
                if (input.isKeyPressed(GLFW_KEY_1 + i)) {
                    selectedChoice = i;
                    makeChoice(validChoices.get(i));
                    return;
                }
            }

            // Confirm
            if (input.isKeyPressed(GLFW_KEY_SPACE) || input.isKeyPressed(GLFW_KEY_ENTER)
                    || mouse.isLeftButtonPressed()) {
                if (selectedChoice >= 0 && selectedChoice < validChoices.size()) {
                    makeChoice(validChoices.get(selectedChoice));
                }
            }
        }
    }

    private void makeChoice(Choice choice) {
        for (Effect effect : choice.getEffects()) {
            effect.apply(storyState);
        }

        // Track cognitive principles
        if (choice.getCognitivePrinciple() != null) {
            cognitiveSystem.onPrincipleEngaged(choice.getCognitivePrinciple());
        }

        // Track steelman choices
        if (choice.isSteelmanChoice()) {
            storyState.addSteelman(3);
            cognitiveSystem.onSteelmanAttempt(true);
        }

        // Track emotional regulation
        if (choice.isRegulationChoice()) {
            storyState.addEmotionalRegulation(2);
            cognitiveSystem.onRegulationChoice();
        }

        // Track long-term vs short-term
        if ("long".equals(choice.getTermType())) {
            storyState.addLongTermThinking(2);
        } else if ("short".equals(choice.getTermType())) {
            storyState.addLongTermThinking(-1);
        }

        if (choice.getNotification() != null) {
            notifications.add(new Notification(choice.getNotification(), 3.0f));
        }

        waitingForChoice = false;

        if (choice.getNextScene() != null) {
            waitingForTransition = true;
            nextSceneId = choice.getNextScene();
        } else if (choice.getNextNode() != null) {
            advanceDialogue();
        } else {
            advanceDialogue();
        }
    }

    private void advanceDialogue() {
        if (currentNode != null && currentNode.getNextScene() != null) {
            waitingForTransition = true;
            nextSceneId = currentNode.getNextScene();
            return;
        }

        advanceToNode(currentNodeIndex + 1);
    }

    public List<Choice> getValidChoices() {
        if (currentNode == null || !currentNode.hasChoices()) return List.of();

        List<Choice> valid = new ArrayList<>();
        for (Choice choice : currentNode.getChoices()) {
            if (choice.getCondition() == null || choice.getCondition().isMet(storyState)) {
                valid.add(choice);
            }
        }
        return valid;
    }

    public void updateNotifications(float dt) {
        notifications.removeIf(n -> {
            n.update(dt);
            return n.isExpired();
        });
    }

    // --- Getters ---
    public DialogueNode getCurrentNode() { return currentNode; }
    public String getRevealedText() {
        return revealedChars >= fullText.length() ? fullText : fullText.substring(0, revealedChars);
    }
    public boolean isTextFullyRevealed() { return textFullyRevealed; }
    public boolean isWaitingForChoice() { return waitingForChoice; }
    public boolean isWaitingForAdvance() { return waitingForAdvance; }
    public int getSelectedChoice() { return selectedChoice; }
    public float getChoiceTimer() { return choiceTimer; }
    public float getChoiceTimerMax() { return currentNode != null ? currentNode.getTimer() : 0; }
    public List<Notification> getNotifications() { return notifications; }
    public boolean isWaitingForTransition() { return waitingForTransition; }
    public String getNextSceneId() { return nextSceneId; }
    public void clearTransition() { waitingForTransition = false; nextSceneId = null; }
    public boolean isSceneComplete() { return currentNode == null && !waitingForTransition; }

    /**
     * A notification that fades in and out.
     */
    public static class Notification {
        private final String text;
        private float duration;
        private float elapsed = 0;

        public Notification(String text, float duration) {
            this.text = text;
            this.duration = duration;
        }

        public void update(float dt) { elapsed += dt; }
        public boolean isExpired() { return elapsed >= duration; }
        public String getText() { return text; }
        public float getAlpha() {
            if (elapsed < 0.5f) return elapsed / 0.5f;
            if (elapsed > duration - 0.5f) return (duration - elapsed) / 0.5f;
            return 1.0f;
        }
    }
}
