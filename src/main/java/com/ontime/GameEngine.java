package com.ontime;

import com.ontime.engine.Window;
import com.ontime.engine.Timer;
import com.ontime.engine.input.InputHandler;
import com.ontime.engine.input.MouseHandler;
import com.ontime.audio.AudioEngine;
import com.ontime.narrative.*;
import com.ontime.rendering.*;
import com.ontime.logic.CognitiveVirtueSystem;
import com.ontime.logic.EvidenceBoard;
import com.ontime.logic.BreathMechanic;
import com.ontime.history.HistoricalEraManager;
import com.ontime.history.HistoricalEra;

import static org.lwjgl.glfw.GLFW.*;

/**
 * The main game engine for On Time.
 *
 * Manages the 3D game loop, state transitions, all subsystems,
 * and the cognitive virtue + historical era systems.
 *
 * Design target: 40-50+ hours of gameplay that teaches logic,
 * history, and critical thinking through consequences, not lectures.
 */
public class GameEngine {

    public static final String TITLE = "On Time";
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;
    public static final float TARGET_UPS = 60.0f;

    private Window window;
    private Timer timer;
    private InputHandler input;
    private MouseHandler mouse;
    private AudioEngine audio;

    // 3D Rendering
    private MasterRenderer masterRenderer;
    private SceneRenderer sceneRenderer;
    private DialogueRenderer dialogueRenderer;
    private TransitionRenderer transitionRenderer;
    private UIRenderer uiRenderer;
    private HUDRenderer hudRenderer;

    // Narrative
    private StoryState storyState;
    private ChapterManager chapterManager;
    private Chapter currentChapter;
    private Scene currentScene;
    private DialogueRunner dialogueRunner;
    private CutscenePlayer cutscenePlayer;

    // Logic & Critical Thinking Systems
    private CognitiveVirtueSystem cognitiveSystem;
    private EvidenceBoard evidenceBoard;
    private BreathMechanic breathMechanic;

    // Historical Eras
    private HistoricalEraManager eraManager;
    private HistoricalEra currentEra;

    // Game state
    public enum Phase {
        TITLE_SCREEN, PLAYING, CUTSCENE, PAUSED, CHAPTER_TITLE,
        TRANSITION, EVIDENCE_BOARD, ERA_TRANSITION, BREATH_PAUSE
    }
    private Phase phase = Phase.TITLE_SCREEN;
    private boolean running = true;

    // Transition
    private float transitionAlpha = 0.0f;
    private boolean fadingIn = false;
    private boolean fadingOut = false;
    private Runnable onTransitionComplete = null;

    // Era transition
    private float eraTransitionTimer = 0;
    private static final float ERA_TRANSITION_DURATION = 4.0f;

    // Chapter title display
    private float chapterTitleTimer = 0;
    private static final float CHAPTER_TITLE_DURATION = 3.5f;

    public void run() {
        try {
            init();
            loop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void init() throws Exception {
        window = new Window(TITLE, WIDTH, HEIGHT);
        window.init();

        timer = new Timer();
        input = new InputHandler(window.getHandle());
        mouse = new MouseHandler(window.getHandle());
        audio = new AudioEngine();

        // 3D renderers
        masterRenderer = new MasterRenderer(window);
        sceneRenderer = new SceneRenderer(window);
        dialogueRenderer = new DialogueRenderer(window);
        transitionRenderer = new TransitionRenderer(window);
        uiRenderer = new UIRenderer(window);
        hudRenderer = new HUDRenderer(window);

        // Logic systems
        cognitiveSystem = new CognitiveVirtueSystem();
        evidenceBoard = new EvidenceBoard();
        breathMechanic = new BreathMechanic();

        // Historical eras
        eraManager = new HistoricalEraManager();

        // Load story
        storyState = SaveManager.loadOrCreate();
        chapterManager = new ChapterManager();
        dialogueRunner = new DialogueRunner(storyState, cognitiveSystem);
        cutscenePlayer = new CutscenePlayer();

        // If continuing a saved game, jump directly into it
        // If new game, stay on title screen — chapter loads when player presses SPACE
        if (storyState.getCurrentChapterId() != null) {
            startChapter(storyState.getCurrentChapterId());
        }
        // else: phase stays TITLE_SCREEN
    }

    private void loop() {
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        while (!window.shouldClose() && running) {
            float elapsed = timer.getElapsedTime();
            accumulator += elapsed;

            processInput();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();
            window.update();
        }
    }

    private void processInput() {
        input.update();
        mouse.update();

        if (input.isKeyPressed(GLFW_KEY_ESCAPE)) {
            if (phase == Phase.PAUSED) {
                phase = Phase.PLAYING;
            } else if (phase == Phase.PLAYING) {
                phase = Phase.PAUSED;
            } else if (phase == Phase.EVIDENCE_BOARD) {
                phase = Phase.PLAYING;
            }
        }

        // Evidence board toggle
        if (input.isKeyPressed(GLFW_KEY_TAB) && phase == Phase.PLAYING) {
            phase = Phase.EVIDENCE_BOARD;
        }

        // Breath mechanic — hold SHIFT to slow dialogue timer
        if (phase == Phase.PLAYING) {
            breathMechanic.setBreathing(input.isKeyDown(GLFW_KEY_LEFT_SHIFT));
        }

        // Skip cutscene
        if (phase == Phase.CUTSCENE && input.isKeyPressed(GLFW_KEY_SPACE)) {
            if (cutscenePlayer.isPlaying()) {
                cutscenePlayer.skip();
            }
        }

        // Title screen — any key to start
        if (phase == Phase.TITLE_SCREEN) {
            if (input.isKeyPressed(GLFW_KEY_SPACE) || input.isKeyPressed(GLFW_KEY_ENTER)
                    || mouse.isLeftButtonPressed()) {
                fadeToBlack(() -> {
                    startChapter("prologue");
                });
            }
        }
    }

    private void update(float dt) {
        // Handle transitions
        if (fadingOut) {
            transitionAlpha += dt * 1.5f;
            if (transitionAlpha >= 1.0f) {
                transitionAlpha = 1.0f;
                fadingOut = false;
                if (onTransitionComplete != null) {
                    onTransitionComplete.run();
                    onTransitionComplete = null;
                }
            }
            return;
        }

        if (fadingIn) {
            transitionAlpha -= dt * 1.5f;
            if (transitionAlpha <= 0.0f) {
                transitionAlpha = 0.0f;
                fadingIn = false;
            }
        }

        if (phase == Phase.PAUSED || phase == Phase.TITLE_SCREEN) return;

        // Chapter title card
        if (phase == Phase.CHAPTER_TITLE) {
            chapterTitleTimer -= dt;
            if (chapterTitleTimer <= 0) {
                phase = Phase.PLAYING;
                fadingIn = true;
            }
            return;
        }

        // Era transition
        if (phase == Phase.ERA_TRANSITION) {
            eraTransitionTimer -= dt;
            if (eraTransitionTimer <= 0) {
                phase = Phase.CHAPTER_TITLE;
                chapterTitleTimer = CHAPTER_TITLE_DURATION;
            }
            return;
        }

        if (phase == Phase.CUTSCENE) {
            cutscenePlayer.update(dt, audio);
            if (cutscenePlayer.isFinished()) {
                phase = Phase.PLAYING;
            }
            return;
        }

        // Evidence board — interactive
        if (phase == Phase.EVIDENCE_BOARD) {
            evidenceBoard.update(dt, mouse, input);
            return;
        }

        // Breath mechanic update
        breathMechanic.update(dt);

        if (phase == Phase.PLAYING && currentScene != null) {
            // Apply breath mechanic time dilation to dialogue timer
            float effectiveDt = dt * breathMechanic.getTimeDilation();

            // Update dialogue
            dialogueRunner.update(effectiveDt, mouse, input, window);

            // Check for scene transitions
            if (dialogueRunner.isWaitingForTransition()) {
                String nextSceneId = dialogueRunner.getNextSceneId();
                if (nextSceneId != null) {
                    if (nextSceneId.startsWith("chapter:")) {
                        String nextChapter = nextSceneId.substring(8);
                        fadeToBlack(() -> startChapter(nextChapter));
                    } else if (nextSceneId.startsWith("era:")) {
                        String eraId = nextSceneId.substring(4);
                        fadeToBlack(() -> transitionToEra(eraId));
                    } else {
                        fadeToBlack(() -> loadScene(nextSceneId));
                    }
                    dialogueRunner.clearTransition();
                }
            }

            dialogueRunner.updateNotifications(dt);
        }

        // Update cognitive system ripple effects
        cognitiveSystem.update(dt, storyState);

        audio.update();
    }

    private void render() {
        masterRenderer.begin();

        switch (phase) {
            case TITLE_SCREEN -> renderTitleScreen();
            case PLAYING, PAUSED -> renderGameplay();
            case CUTSCENE -> renderCutscene();
            case CHAPTER_TITLE -> renderChapterTitle();
            case EVIDENCE_BOARD -> renderEvidenceBoard();
            case ERA_TRANSITION -> renderEraTransition();
            case BREATH_PAUSE -> renderBreathPause();
            default -> {}
        }

        // Transition overlay (fade to/from black)
        if (transitionAlpha > 0.001f) {
            transitionRenderer.renderFade(transitionAlpha);
        }

        masterRenderer.end();
    }

    private void renderTitleScreen() {
        uiRenderer.renderTitleScreen();
    }

    private void renderGameplay() {
        if (currentScene != null) {
            // 3D scene
            sceneRenderer.renderBackground(currentScene);
            sceneRenderer.renderCharacters(currentScene);

            // Dialogue box + choices
            dialogueRenderer.render(dialogueRunner);
            dialogueRenderer.renderNotifications(dialogueRunner);

            // HUD — cognitive virtues, era indicator
            hudRenderer.renderCognitiveHUD(cognitiveSystem, storyState);
            hudRenderer.renderEraIndicator(currentEra);

            // Breath mechanic visual
            if (breathMechanic.isBreathing()) {
                hudRenderer.renderBreathIndicator(breathMechanic);
            }

            // Pause overlay
            if (phase == Phase.PAUSED) {
                uiRenderer.renderPauseMenu();
            }
        }
    }

    private void renderCutscene() {
        cutscenePlayer.render(sceneRenderer, dialogueRenderer);
    }

    private void renderChapterTitle() {
        if (currentChapter != null) {
            uiRenderer.renderChapterTitle(currentChapter.getNumber(), currentChapter.getTitle(),
                    currentEra != null ? currentEra.getName() : null);
        }
    }

    private void renderEvidenceBoard() {
        evidenceBoard.render(sceneRenderer, window);
        uiRenderer.renderEvidenceBoardOverlay(evidenceBoard);
    }

    private void renderEraTransition() {
        if (currentEra != null) {
            float progress = 1.0f - (eraTransitionTimer / ERA_TRANSITION_DURATION);
            uiRenderer.renderEraTransition(currentEra, progress);
        }
    }

    private void renderBreathPause() {
        if (currentScene != null) {
            sceneRenderer.renderBackground(currentScene);
            sceneRenderer.renderCharacters(currentScene);
        }
        hudRenderer.renderBreathIndicator(breathMechanic);
    }

    // --- Story management ---

    private void startChapter(String chapterId) {
        currentChapter = chapterManager.loadChapter(chapterId);
        if (currentChapter == null) {
            System.err.println("Chapter not found: " + chapterId);
            return;
        }

        storyState.setCurrentChapterId(chapterId);

        // Check if this chapter has an associated era
        HistoricalEra era = eraManager.getEraForChapter(chapterId);
        if (era != null && (currentEra == null || !era.getId().equals(currentEra.getId()))) {
            currentEra = era;
            cognitiveSystem.onEraEnter(era);
            phase = Phase.ERA_TRANSITION;
            eraTransitionTimer = ERA_TRANSITION_DURATION;
        } else {
            phase = Phase.CHAPTER_TITLE;
            chapterTitleTimer = CHAPTER_TITLE_DURATION;
        }

        fadingIn = true;

        String firstScene = currentChapter.getFirstSceneId();
        loadScene(firstScene);
    }

    private void transitionToEra(String eraId) {
        currentEra = eraManager.getEra(eraId);
        if (currentEra != null) {
            cognitiveSystem.onEraEnter(currentEra);
            phase = Phase.ERA_TRANSITION;
            eraTransitionTimer = ERA_TRANSITION_DURATION;
        }
    }

    private void loadScene(String sceneId) {
        if (currentChapter == null) return;

        currentScene = currentChapter.getScene(sceneId);
        if (currentScene == null) {
            System.err.println("Scene not found: " + sceneId + " in chapter " + currentChapter.getId());
            return;
        }

        dialogueRunner.startScene(currentScene);

        if (currentScene.getMusic() != null) {
            audio.playMusic(currentScene.getMusic());
        }

        fadingIn = true;
    }

    private void fadeToBlack(Runnable then) {
        fadingOut = true;
        transitionAlpha = 0.0f;
        onTransitionComplete = then;
    }

    private void cleanup() {
        if (storyState != null) {
            SaveManager.save(storyState);
        }

        if (audio != null) audio.cleanup();
        if (dialogueRenderer != null) dialogueRenderer.cleanup();
        if (sceneRenderer != null) sceneRenderer.cleanup();
        if (transitionRenderer != null) transitionRenderer.cleanup();
        if (uiRenderer != null) uiRenderer.cleanup();
        if (hudRenderer != null) hudRenderer.cleanup();
        if (masterRenderer != null) masterRenderer.cleanup();
        if (window != null) window.cleanup();
    }

    // --- Accessors for subsystems ---
    public CognitiveVirtueSystem getCognitiveSystem() { return cognitiveSystem; }
    public EvidenceBoard getEvidenceBoard() { return evidenceBoard; }
    public HistoricalEra getCurrentEra() { return currentEra; }
    public StoryState getStoryState() { return storyState; }
}
