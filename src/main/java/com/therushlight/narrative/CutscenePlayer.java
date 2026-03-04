package com.therushlight.narrative;

import com.therushlight.audio.AudioEngine;
import com.therushlight.rendering.SceneRenderer;
import com.therushlight.rendering.DialogueRenderer;

/**
 * Plays scripted cutscenes — camera moves, dialogue, sound cues,
 * timed to a timeline. Used for major story beats (like Drew's death).
 */
public class CutscenePlayer {

    private boolean playing = false;
    private boolean finished = false;
    private float elapsed = 0;

    // TODO: Full timeline system with tracks
    // For now, this is a stub that can be fleshed out

    public void play(String cutsceneId) {
        playing = true;
        finished = false;
        elapsed = 0;
    }

    public void update(float dt, AudioEngine audio) {
        if (!playing) return;
        elapsed += dt;

        // Placeholder: cutscene ends after it runs out of timeline events
        // Real implementation would iterate through keyframes
    }

    public void render(SceneRenderer sceneRenderer, DialogueRenderer dialogueRenderer) {
        // Render current cutscene frame
    }

    public void skip() {
        // Must still apply all state changes even when skipping
        playing = false;
        finished = true;
    }

    public boolean isPlaying() { return playing; }
    public boolean isFinished() { return finished; }
}
