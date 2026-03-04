package com.therushlight.engine.input;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {

    private final boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] keysPressed = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] prevKeys = new boolean[GLFW_KEY_LAST + 1];

    public InputHandler(long window) {
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                keys[key] = (action != GLFW_RELEASE);
            }
        });
    }

    public void update() {
        for (int i = 0; i < keys.length; i++) {
            keysPressed[i] = keys[i] && !prevKeys[i];
            prevKeys[i] = keys[i];
        }
    }

    public boolean isKeyDown(int key) {
        return key >= 0 && key <= GLFW_KEY_LAST && keys[key];
    }

    public boolean isKeyPressed(int key) {
        return key >= 0 && key <= GLFW_KEY_LAST && keysPressed[key];
    }
}
