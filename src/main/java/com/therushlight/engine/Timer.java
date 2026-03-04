package com.therushlight.engine;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Timer {
    private double lastTime;

    public Timer() {
        lastTime = glfwGetTime();
    }

    public float getElapsedTime() {
        double time = glfwGetTime();
        float elapsed = (float) (time - lastTime);
        lastTime = time;
        return elapsed;
    }
}
