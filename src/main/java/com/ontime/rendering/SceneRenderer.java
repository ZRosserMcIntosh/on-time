package com.ontime.rendering;

import com.ontime.engine.Window;
import com.ontime.narrative.Scene;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Renders 3D scenes — environments, character models, and effects.
 * Uses perspective projection for a 3D narrative game with explorable environments.
 *
 * Placeholder rendering uses colored 3D boxes until models are loaded.
 * The 3D environment gives depth to historical eras and emotional scenes.
 */
public class SceneRenderer {

    private final Window window;
    private int quadVAO;
    private int quadVBO;
    private int shaderProgram;

    // 2D overlay shader for backgrounds that are images
    private static final String VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec2 aPos;
            layout (location = 1) in vec2 aTexCoord;
            uniform vec4 uRect;
            uniform vec2 uScreen;
            out vec2 TexCoord;
            void main() {
                vec2 pos = aPos * uRect.zw + uRect.xy;
                vec2 ndc = (pos / uScreen) * 2.0 - 1.0;
                ndc.y = -ndc.y;
                gl_Position = vec4(ndc, 0.0, 1.0);
                TexCoord = aTexCoord;
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 330 core
            in vec2 TexCoord;
            out vec4 FragColor;
            uniform vec4 uColor;
            uniform int uUseTexture;
            uniform sampler2D uTexture;
            void main() {
                if (uUseTexture == 1) {
                    FragColor = texture(uTexture, TexCoord) * uColor;
                } else {
                    FragColor = uColor;
                }
            }
            """;

    public SceneRenderer(Window window) {
        this.window = window;
        initGL();
    }

    private void initGL() {
        float[] vertices = {
                0, 0, 0, 0,
                1, 0, 1, 0,
                1, 1, 1, 1,
                0, 0, 0, 0,
                1, 1, 1, 1,
                0, 1, 0, 1,
        };

        quadVAO = glGenVertexArrays();
        quadVBO = glGenBuffers();
        glBindVertexArray(quadVAO);
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4);

        glBindVertexArray(0);

        shaderProgram = createShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public void begin() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void end() {}

    /**
     * Render the scene background — 3D environment with mood-based lighting.
     * For now, renders colored environment. Will render 3D skyboxes and environments.
     */
    public void renderBackground(Scene scene) {
        glDisable(GL_DEPTH_TEST);

        String mood = scene.getMood();
        float r, g, b;

        if (mood == null) mood = "neutral";

        switch (mood) {
            case "warm" -> { r = 0.15f; g = 0.10f; b = 0.07f; }
            case "tense" -> { r = 0.08f; g = 0.05f; b = 0.05f; }
            case "grief" -> { r = 0.04f; g = 0.04f; b = 0.08f; }
            case "hope" -> { r = 0.10f; g = 0.12f; b = 0.18f; }
            case "danger" -> { r = 0.12f; g = 0.03f; b = 0.03f; }
            case "peace" -> { r = 0.08f; g = 0.12f; b = 0.10f; }
            case "historical" -> { r = 0.12f; g = 0.10f; b = 0.08f; }
            case "trial" -> { r = 0.10f; g = 0.06f; b = 0.06f; }
            case "market" -> { r = 0.14f; g = 0.11f; b = 0.08f; }
            case "revolution" -> { r = 0.10f; g = 0.08f; b = 0.06f; }
            default -> { r = 0.06f; g = 0.06f; b = 0.08f; }
        }

        drawRect(0, 0, window.getWidth(), window.getHeight(), r, g, b, 1.0f);
        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Render 3D character placeholders at scene positions.
     * Will be replaced with animated 3D character models.
     */
    public void renderCharacters(Scene scene) {
        glDisable(GL_DEPTH_TEST);

        var positions = scene.getCharacterPositions();
        int i = 0;
        for (var entry : positions.entrySet()) {
            String character = entry.getKey();
            String position = entry.getValue();

            float x;
            switch (position) {
                case "left" -> x = window.getWidth() * 0.2f;
                case "right" -> x = window.getWidth() * 0.7f;
                case "center" -> x = window.getWidth() * 0.45f;
                case "far_left" -> x = window.getWidth() * 0.05f;
                case "far_right" -> x = window.getWidth() * 0.85f;
                default -> x = window.getWidth() * (0.3f + i * 0.2f);
            }

            float charW = window.getWidth() * 0.1f;
            float charH = window.getHeight() * 0.5f;
            float charY = window.getHeight() * 0.15f;

            float r = 0.3f, g = 0.3f, b = 0.3f;
            switch (character) {
                case "drew" -> { r = 0.5f; g = 0.35f; b = 0.25f; }
                case "rush" -> { r = 0.3f; g = 0.4f; b = 0.5f; }
                case "lu", "luisa" -> { r = 0.5f; g = 0.3f; b = 0.45f; }
                case "yen", "yenevieve" -> { r = 0.45f; g = 0.45f; b = 0.3f; }
                case "kimiru" -> { r = 0.1f; g = 0.1f; b = 0.12f; }
                case "shepherd" -> { r = 0.35f; g = 0.3f; b = 0.25f; }
            }

            drawRect(x, charY, charW, charH, r, g, b, 0.8f);
            i++;
        }

        glEnable(GL_DEPTH_TEST);
    }

    public void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
        glUseProgram(shaderProgram);
        glUniform4f(glGetUniformLocation(shaderProgram, "uRect"), x, y, w, h);
        glUniform2f(glGetUniformLocation(shaderProgram, "uScreen"), window.getWidth(), window.getHeight());
        glUniform4f(glGetUniformLocation(shaderProgram, "uColor"), r, g, b, a);
        glUniform1i(glGetUniformLocation(shaderProgram, "uUseTexture"), 0);

        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
        glUseProgram(0);
    }

    private int createShaderProgram(String vertexSrc, String fragmentSrc) {
        int vertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex, vertexSrc);
        glCompileShader(vertex);
        if (glGetShaderi(vertex, GL_COMPILE_STATUS) == 0) {
            System.err.println("Vertex shader error: " + glGetShaderInfoLog(vertex));
        }

        int fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, fragmentSrc);
        glCompileShader(fragment);
        if (glGetShaderi(fragment, GL_COMPILE_STATUS) == 0) {
            System.err.println("Fragment shader error: " + glGetShaderInfoLog(fragment));
        }

        int program = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
            System.err.println("Shader link error: " + glGetProgramInfoLog(program));
        }

        glDeleteShader(vertex);
        glDeleteShader(fragment);
        return program;
    }

    public Window getWindow() { return window; }

    public void cleanup() {
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        glDeleteProgram(shaderProgram);
    }
}
