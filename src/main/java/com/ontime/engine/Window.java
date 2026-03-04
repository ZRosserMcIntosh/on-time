package com.ontime.engine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * GLFW window with OpenGL 3.3 context.
 * Configured for 3D rendering with depth buffer and MSAA.
 */
public class Window {

    private long handle;
    private final String title;
    private int width, height;
    private boolean fullscreen = false;
    private int windowedX, windowedY, windowedW, windowedH;

    public Window(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4); // MSAA

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(handle, (window, w, h) -> {
            width = w;
            height = h;
            glViewport(0, 0, w, h);
        });

        // Center window
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(handle, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidmode != null) {
                glfwSetWindowPos(handle,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2);
            }
        }

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1); // V-sync

        GL.createCapabilities();

        // 3D setup
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // Cull face disabled by default — 2D quads have flipped winding from Y-invert.
        // Enable selectively during 3D scene rendering when models are loaded.
        glClearColor(0.02f, 0.02f, 0.04f, 1.0f);

        glfwShowWindow(handle);
    }

    public void update() {
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void toggleFullscreen() {
        if (!fullscreen) {
            int[] xpos = new int[1], ypos = new int[1];
            glfwGetWindowPos(handle, xpos, ypos);
            windowedX = xpos[0];
            windowedY = ypos[0];
            windowedW = width;
            windowedH = height;
            GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (mode != null) {
                glfwSetWindowMonitor(handle, glfwGetPrimaryMonitor(), 0, 0,
                        mode.width(), mode.height(), mode.refreshRate());
            }
        } else {
            glfwSetWindowMonitor(handle, NULL, windowedX, windowedY,
                    windowedW, windowedH, 0);
        }
        fullscreen = !fullscreen;
    }

    public void cleanup() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        GLFWErrorCallback cb = glfwSetErrorCallback(null);
        if (cb != null) cb.free();
    }

    public long getHandle() { return handle; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public float getAspect() { return (float) width / height; }
}
