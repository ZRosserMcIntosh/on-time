package com.ontime.engine.input;

import static org.lwjgl.glfw.GLFW.*;

public class MouseHandler {

    private final long window;
    private double mouseX, mouseY;
    private double prevMouseX, prevMouseY;
    private float dx, dy;
    private double scrollY;

    private boolean leftDown, rightDown;
    private boolean leftPressed, rightPressed;
    private boolean prevLeftDown, prevRightDown;

    public MouseHandler(long window) {
        this.window = window;

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) leftDown = (action != GLFW_RELEASE);
            if (button == GLFW_MOUSE_BUTTON_RIGHT) rightDown = (action != GLFW_RELEASE);
        });

        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            scrollY = yoffset;
        });
    }

    public void update() {
        dx = (float) (mouseX - prevMouseX);
        dy = (float) (mouseY - prevMouseY);
        prevMouseX = mouseX;
        prevMouseY = mouseY;

        leftPressed = leftDown && !prevLeftDown;
        rightPressed = rightDown && !prevRightDown;
        prevLeftDown = leftDown;
        prevRightDown = rightDown;
    }

    public double getX() { return mouseX; }
    public double getY() { return mouseY; }
    public float getDX() { return dx; }
    public float getDY() { return dy; }
    public boolean isLeftButtonPressed() { return leftPressed; }
    public boolean isRightButtonPressed() { return rightPressed; }
    public boolean isLeftButtonDown() { return leftDown; }
}
