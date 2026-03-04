package com.ontime.rendering;

import com.ontime.engine.Window;
import com.ontime.history.HistoricalEra;
import com.ontime.logic.BreathMechanic;
import com.ontime.logic.CognitiveVirtueSystem;
import com.ontime.narrative.StoryState;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * HUD Renderer — renders the cognitive virtue indicators, era information,
 * and breath mechanic overlay. Designed to be subtle and non-intrusive.
 *
 * No popups. No "Lesson Unlocked!". Just quiet visual indicators
 * that the player can notice or ignore.
 */
public class HUDRenderer {

    private final Window window;
    private int quadVAO, quadVBO;
    private int shaderProgram;

    private static final float CHAR_W = 8;
    private static final float CHAR_H = 12;

    private static final String VERT = """
            #version 330 core
            layout (location = 0) in vec2 aPos;
            uniform vec4 uRect;
            uniform vec2 uScreen;
            void main() {
                vec2 pos = aPos * uRect.zw + uRect.xy;
                vec2 ndc = (pos / uScreen) * 2.0 - 1.0;
                ndc.y = -ndc.y;
                gl_Position = vec4(ndc, 0.0, 1.0);
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

    public HUDRenderer(Window window) {
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

    /**
     * Render the cognitive virtue indicators in the top-right corner.
     * Small, subtle bars that show the player's cognitive virtues
     * without labels — just colors and sizes.
     */
    public void renderCognitiveHUD(CognitiveVirtueSystem system, StoryState state) {
        glDisable(GL_DEPTH_TEST);

        float sw = window.getWidth();
        float x = sw - 120;
        float y = 20;
        float barW = 80;
        float barH = 4;
        float gap = 8;

        // Humility (blue-gray)
        float humility = system.getHumility() / 100f;
        drawRect(x, y, barW, barH, 0.15f, 0.15f, 0.2f, 0.3f);
        drawRect(x, y, barW * humility, barH, 0.4f, 0.5f, 0.7f, 0.6f);
        y += gap;

        // Patience (warm gold)
        float patience = system.getPatience() / 100f;
        drawRect(x, y, barW, barH, 0.2f, 0.15f, 0.1f, 0.3f);
        drawRect(x, y, barW * patience, barH, 0.8f, 0.65f, 0.3f, 0.6f);
        y += gap;

        // Clarity (cool white)
        float clarity = system.getClarity() / 100f;
        drawRect(x, y, barW, barH, 0.15f, 0.15f, 0.15f, 0.3f);
        drawRect(x, y, barW * clarity, barH, 0.8f, 0.8f, 0.85f, 0.6f);
        y += gap;

        // Empathy (soft pink)
        float empathy = system.getEmpathy() / 100f;
        drawRect(x, y, barW, barH, 0.2f, 0.1f, 0.15f, 0.3f);
        drawRect(x, y, barW * empathy, barH, 0.7f, 0.4f, 0.5f, 0.6f);
        y += gap;

        // Resolve (deep red)
        float resolve = system.getResolve() / 100f;
        drawRect(x, y, barW, barH, 0.2f, 0.1f, 0.1f, 0.3f);
        drawRect(x, y, barW * resolve, barH, 0.7f, 0.3f, 0.3f, 0.6f);

        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Render the current historical era indicator in the top-left corner.
     */
    public void renderEraIndicator(HistoricalEra era) {
        if (era == null) return;

        glDisable(GL_DEPTH_TEST);

        float x = 20;
        float y = 20;

        // Era name in small text
        float[] color = era.getThemeColor();
        drawText(era.getYearRange(), x, y, color[0] * 0.7f, color[1] * 0.7f, color[2] * 0.7f, 0.5f);

        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Render the breath mechanic indicator — a subtle expanding/contracting circle
     * that shows the player they're slowing time.
     */
    public void renderBreathIndicator(BreathMechanic breath) {
        glDisable(GL_DEPTH_TEST);

        float sw = window.getWidth();
        float sh = window.getHeight();

        float centerX = sw / 2;
        float centerY = sh * 0.45f;

        float breathPhase = breath.getBreathPhase();
        float size = 20 + breathPhase * 15;

        // Soft circle (approximated with rect for now)
        float alpha = 0.15f + breathPhase * 0.1f;
        drawRect(centerX - size, centerY - size, size * 2, size * 2,
                0.5f, 0.5f, 0.6f, alpha);
        drawRect(centerX - size * 0.6f, centerY - size * 0.6f, size * 1.2f, size * 1.2f,
                0.6f, 0.6f, 0.7f, alpha * 1.5f);

        // "Breathe" text, very faint
        if (breath.getBreathDuration() > 1.0f) {
            String text = "breathe";
            float textW = text.length() * CHAR_W;
            drawText(text, centerX - textW / 2, centerY + size + 10,
                    0.5f, 0.5f, 0.55f, 0.3f);
        }

        glEnable(GL_DEPTH_TEST);
    }

    private void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
        glUseProgram(shaderProgram);
        glUniform4f(glGetUniformLocation(shaderProgram, "uRect"), x, y, w, h);
        glUniform2f(glGetUniformLocation(shaderProgram, "uScreen"), window.getWidth(), window.getHeight());
        glUniform4f(glGetUniformLocation(shaderProgram, "uColor"), r, g, b, a);
        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
        glUseProgram(0);
    }

    private void drawText(String text, float x, float y, float r, float g, float b, float a) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != ' ') {
                drawRect(x + i * CHAR_W, y, CHAR_W - 2, CHAR_H - 2, r, g, b, a);
            }
        }
    }

    public void cleanup() {
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        glDeleteProgram(shaderProgram);
    }
}
