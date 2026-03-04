package com.ontime.rendering;

import com.ontime.engine.Window;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Handles screen transitions — fade to/from black, white flash, era transitions.
 */
public class TransitionRenderer {

    private final Window window;
    private int quadVAO, quadVBO;
    private int shaderProgram;

    private static final String VERT = """
            #version 330 core
            layout (location = 0) in vec2 aPos;
            void main() {
                gl_Position = vec4(aPos * 2.0 - 1.0, 0.0, 1.0);
            }
            """;

    private static final String FRAG = """
            #version 330 core
            out vec4 FragColor;
            uniform vec4 uColor;
            void main() {
                FragColor = uColor;
            }
            """;

    public TransitionRenderer(Window window) {
        this.window = window;

        float[] verts = { 0,0, 1,0, 1,1, 0,0, 1,1, 0,1 };
        quadVAO = glGenVertexArrays();
        quadVBO = glGenBuffers();
        glBindVertexArray(quadVAO);
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
        glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glBindVertexArray(0);

        int vert = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vert, VERT);
        glCompileShader(vert);

        int frag = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(frag, FRAG);
        glCompileShader(frag);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vert);
        glAttachShader(shaderProgram, frag);
        glLinkProgram(shaderProgram);
        glDeleteShader(vert);
        glDeleteShader(frag);
    }

    public void renderFade(float alpha) {
        renderFade(alpha, 0, 0, 0);
    }

    public void renderFade(float alpha, float r, float g, float b) {
        glDisable(GL_DEPTH_TEST);
        glUseProgram(shaderProgram);
        glUniform4f(glGetUniformLocation(shaderProgram, "uColor"), r, g, b, alpha);

        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
        glUseProgram(0);
        glEnable(GL_DEPTH_TEST);
    }

    public void cleanup() {
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        glDeleteProgram(shaderProgram);
    }
}
