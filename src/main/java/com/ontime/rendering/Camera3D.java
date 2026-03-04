package com.ontime.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 3D Camera for scene rendering.
 * Supports smooth interpolation between positions for cinematic camera work.
 */
public class Camera3D {

    private final Vector3f position = new Vector3f(0, 2, 5);
    private final Vector3f target = new Vector3f(0, 1, 0);
    private final Vector3f up = new Vector3f(0, 1, 0);

    // Smooth camera movement
    private final Vector3f targetPosition = new Vector3f(0, 2, 5);
    private final Vector3f targetLookAt = new Vector3f(0, 1, 0);
    private float lerpSpeed = 2.0f;

    // Camera shake
    private float shakeIntensity = 0;
    private float shakeTimer = 0;

    public void setPosition(Vector3f pos) {
        position.set(pos);
        targetPosition.set(pos);
    }

    public void setTarget(Vector3f tgt) {
        target.set(tgt);
        targetLookAt.set(tgt);
    }

    /**
     * Smoothly move camera to a new position over time.
     */
    public void moveTo(Vector3f newPos, Vector3f newTarget, float speed) {
        targetPosition.set(newPos);
        targetLookAt.set(newTarget);
        lerpSpeed = speed;
    }

    /**
     * Trigger camera shake (for impacts, emotional beats).
     */
    public void shake(float intensity, float duration) {
        shakeIntensity = intensity;
        shakeTimer = duration;
    }

    public void update(float dt) {
        // Lerp position
        position.lerp(targetPosition, Math.min(1.0f, lerpSpeed * dt));
        target.lerp(targetLookAt, Math.min(1.0f, lerpSpeed * dt));

        // Camera shake
        if (shakeTimer > 0) {
            shakeTimer -= dt;
            if (shakeTimer <= 0) {
                shakeIntensity = 0;
            }
        }
    }

    public Matrix4f getViewMatrix(Matrix4f dest) {
        float shakeX = 0, shakeY = 0;
        if (shakeIntensity > 0) {
            shakeX = (float) (Math.random() - 0.5) * shakeIntensity * 2;
            shakeY = (float) (Math.random() - 0.5) * shakeIntensity * 2;
        }

        return dest.identity().lookAt(
                position.x + shakeX, position.y + shakeY, position.z,
                target.x, target.y, target.z,
                up.x, up.y, up.z
        );
    }

    public Vector3f getPosition() { return position; }
    public Vector3f getTarget() { return target; }
}
