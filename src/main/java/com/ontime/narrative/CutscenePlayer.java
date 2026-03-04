package com.ontime.narrative;

import com.ontime.audio.AudioEngine;
import com.ontime.rendering.SceneRenderer;
import com.ontime.rendering.DialogueRenderer;

/**
 * Plays scripted cutscenes — 3D camera moves, dialogue, sound cues,
 * timed to a timeline. Used for major story beats.
 */
public class CutscenePlayer {

    private boolean playing = false;
    private boolean finished = false;
    private float elapsed = 0;

    public void play(String cutsceneId) {
        playing = true;
        finished = false;
        elapsed = 0;
    }

    public void update(float dt, AudioEngine audio) {
        if (!playing) return;
        elapsed += dt;
    }

    public void render(SceneRenderer sceneRenderer, DialogueRenderer dialogueRenderer) {
        // Render current cutscene frame with 3D camera work
    }

    public void skip() {
        playing = false;
        finished = true;
    }

    public boolean isPlaying() { return playing; }
    public boolean isFinished() { return finished; }
}
