package com.ontime.logic;

/**
 * Breath Mechanic — a deliberate-pause system that teaches emotional regulation.
 *
 * When the player holds SHIFT during dialogue, the game enters a
 * "breathing" state:
 *   • Time dilation slows dialogue and choice timers
 *   • A subtle visual indicator expands/contracts rhythmically
 *   • Extended breathing boosts the Emotional Regulation cognitive score
 *   • The player learns to pause before reacting
 *
 * Inspired by Virgil's Principle #7: Emotional Regulation —
 * "Strong emotions are data, not directives."
 */
public class BreathMechanic {

    // ── Tuning constants ─────────────────────────────────────────

    private static final float BREATH_CYCLE_SECONDS = 4.0f;     // One full inhale-exhale
    private static final float TIME_DILATION_FACTOR = 0.35f;     // How much time slows (0 = frozen, 1 = normal)
    private static final float MIN_BREATH_FOR_BONUS = 2.0f;      // Seconds of breathing before regulation bonus
    private static final float REGULATION_BOOST_RATE = 0.01f;    // Per-second boost to emotional regulation

    // ── State ────────────────────────────────────────────────────

    private boolean breathing = false;
    private float breathPhase = 0;           // 0.0 – 1.0  (0 = exhale, 0.5 = peak inhale, 1.0 = exhale)
    private float breathDuration = 0;        // Total seconds spent breathing this session
    private float totalBreathTime = 0;       // Lifetime cumulative
    private int breathSessions = 0;          // Number of times the player has breathed

    // ── Per-frame update ─────────────────────────────────────────

    public void update(float dt) {
        if (breathing) {
            breathDuration += dt;
            totalBreathTime += dt;
            breathPhase += dt / BREATH_CYCLE_SECONDS;
            if (breathPhase > 1.0f) breathPhase -= 1.0f;
        } else {
            // Smoothly return indicator to rest
            if (breathPhase > 0) {
                breathPhase = Math.max(0, breathPhase - dt * 2.0f);
            }
        }
    }

    // ── Controls ─────────────────────────────────────────────────

    public void setBreathing(boolean active) {
        if (active && !breathing) {
            // Starting a new breath session
            breathSessions++;
            breathDuration = 0;
        }
        this.breathing = active;
    }

    public boolean isBreathing() {
        return breathing;
    }

    // ── Outputs ──────────────────────────────────────────────────

    /**
     * Returns the current time dilation factor.
     * 1.0 = normal speed, lower = slower.
     */
    public float getTimeDilation() {
        if (!breathing) return 1.0f;
        // Gradually increase dilation the longer they breathe
        float depth = Math.min(1.0f, breathDuration / 3.0f);
        return 1.0f - (1.0f - TIME_DILATION_FACTOR) * depth;
    }

    /**
     * Returns 0.0 – 1.0 representing where in the breath cycle we are.
     * 0.0 / 1.0 = exhale (rest), 0.5 = peak inhale.
     * Used by HUDRenderer for the visual indicator.
     */
    public float getBreathPhase() {
        return breathPhase;
    }

    /**
     * Returns how long the current breath session has lasted.
     */
    public float getBreathDuration() {
        return breathDuration;
    }

    /**
     * Whether the current session is long enough to earn a regulation bonus.
     */
    public boolean qualifiesForBonus() {
        return breathing && breathDuration >= MIN_BREATH_FOR_BONUS;
    }

    /**
     * Returns per-second regulation boost if currently qualifying.
     */
    public float getRegulationBoostRate() {
        return qualifiesForBonus() ? REGULATION_BOOST_RATE : 0;
    }

    // ── Stats ────────────────────────────────────────────────────

    public float getTotalBreathTime()  { return totalBreathTime; }
    public int getBreathSessions()     { return breathSessions; }
}
