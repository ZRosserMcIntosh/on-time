package com.ontime.rendering;

import com.ontime.engine.Window;
import com.ontime.history.HistoricalEra;
import com.ontime.logic.EvidenceBoard;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Renders UI elements — title screen, pause menu, chapter title cards,
 * era transitions, and evidence board overlay.
 *
 * On Time — "Every moment is a choice. Every era is a teacher."
 */
public class UIRenderer {

    private final Window window;
    private int quadVAO, quadVBO;
    private int shaderProgram;

    private static final float CHAR_W = 12;
    private static final float CHAR_H = 18;

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

    public UIRenderer(Window window) {
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
     * Title screen — "ON TIME" centered, with tagline and prompt.
     */
    public void renderTitleScreen() {
        glDisable(GL_DEPTH_TEST);

        float sw = window.getWidth();
        float sh = window.getHeight();

        drawRect(0, 0, sw, sh, 0.03f, 0.02f, 0.02f, 1.0f);

        // Title: ON TIME
        String title = "ON TIME";
        float titleScale = 3.0f;
        float titleW = title.length() * CHAR_W * titleScale;
        float titleX = (sw - titleW) / 2;
        float titleY = sh * 0.28f;
        drawTextScaled(title, titleX, titleY, titleScale, 0.85f, 0.75f, 0.55f, 1.0f);

        // Tagline
        String tagline1 = "Every moment is a choice.";
        float tag1W = tagline1.length() * CHAR_W;
        drawText(tagline1, (sw - tag1W) / 2, titleY + 70, 0.55f, 0.45f, 0.35f, 0.7f);

        String tagline2 = "Every era is a teacher.";
        float tag2W = tagline2.length() * CHAR_W;
        drawText(tagline2, (sw - tag2W) / 2, titleY + 95, 0.50f, 0.42f, 0.33f, 0.6f);

        // Pulsing prompt
        float pulse = (float) (0.4 + 0.3 * Math.sin(System.currentTimeMillis() / 500.0));
        String prompt = "Press SPACE to begin";
        float promptW = prompt.length() * CHAR_W;
        drawText(prompt, (sw - promptW) / 2, sh * 0.65f, 0.6f, 0.5f, 0.4f, pulse);

        // Hourglass symbol (the On Time emblem)
        float hourglassX = sw / 2 - 6;
        float hourglassY = titleY - 50;
        float flicker = (float) (1.0 + 0.1 * Math.sin(System.currentTimeMillis() / 150.0));
        // Top triangle
        drawRect(hourglassX - 4, hourglassY, 14, 3, 0.8f, 0.6f, 0.3f, 0.9f * flicker);
        drawRect(hourglassX - 1, hourglassY + 3, 8, 3, 0.8f, 0.6f, 0.3f, 0.85f * flicker);
        drawRect(hourglassX + 2, hourglassY + 6, 2, 4, 0.9f, 0.7f, 0.3f, 0.9f * flicker);
        // Bottom triangle
        drawRect(hourglassX - 1, hourglassY + 10, 8, 3, 0.8f, 0.6f, 0.3f, 0.85f * flicker);
        drawRect(hourglassX - 4, hourglassY + 13, 14, 3, 0.8f, 0.6f, 0.3f, 0.9f * flicker);

        // Credits
        String credit = "For Yen, Rush, and Lu";
        float creditW = credit.length() * CHAR_W * 0.7f;
        drawTextScaled(credit, (sw - creditW) / 2, sh * 0.85f, 0.7f, 0.4f, 0.35f, 0.3f, 0.5f);

        String credit2 = "A small light is still a light.";
        float credit2W = credit2.length() * CHAR_W * 0.6f;
        drawTextScaled(credit2, (sw - credit2W) / 2, sh * 0.90f, 0.6f, 0.35f, 0.30f, 0.28f, 0.4f);

        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Chapter title card with optional era indicator.
     */
    public void renderChapterTitle(int number, String title, String eraName) {
        glDisable(GL_DEPTH_TEST);

        float sw = window.getWidth();
        float sh = window.getHeight();

        drawRect(0, 0, sw, sh, 0.0f, 0.0f, 0.0f, 1.0f);

        // Era name (if in a historical era)
        if (eraName != null) {
            float eraScale = 0.8f;
            float eraW = eraName.length() * CHAR_W * eraScale;
            drawTextScaled(eraName, (sw - eraW) / 2, sh * 0.32f, eraScale,
                    0.4f, 0.35f, 0.5f, 0.7f);
        }

        // Chapter number
        String chapterStr = number > 0 ? "Chapter " + number : "Prologue";
        float chW = chapterStr.length() * CHAR_W;
        drawText(chapterStr, (sw - chW) / 2, sh * 0.4f, 0.5f, 0.4f, 0.3f, 0.8f);

        // Title
        float titleScale = 1.8f;
        float titleW = title.length() * CHAR_W * titleScale;
        drawTextScaled(title, (sw - titleW) / 2, sh * 0.48f, titleScale,
                0.85f, 0.7f, 0.45f, 1.0f);

        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Era transition screen — immersive display when traveling to a new historical period.
     */
    public void renderEraTransition(HistoricalEra era, float progress) {
        glDisable(GL_DEPTH_TEST);

        float sw = window.getWidth();
        float sh = window.getHeight();

        // Background with era-specific color
        float[] color = era.getThemeColor();
        float alpha = Math.min(1.0f, progress * 2);
        drawRect(0, 0, sw, sh, color[0] * 0.15f, color[1] * 0.15f, color[2] * 0.15f, alpha);

        if (progress > 0.3f) {
            // Era year
            String year = era.getYearRange();
            float yearScale = 2.5f;
            float yearW = year.length() * CHAR_W * yearScale;
            float yearAlpha = Math.min(1.0f, (progress - 0.3f) * 3);
            drawTextScaled(year, (sw - yearW) / 2, sh * 0.3f, yearScale,
                    color[0], color[1], color[2], yearAlpha);
        }

        if (progress > 0.5f) {
            // Era name
            String name = era.getName();
            float nameScale = 1.5f;
            float nameW = name.length() * CHAR_W * nameScale;
            float nameAlpha = Math.min(1.0f, (progress - 0.5f) * 3);
            drawTextScaled(name, (sw - nameW) / 2, sh * 0.45f, nameScale,
                    0.8f, 0.75f, 0.65f, nameAlpha);
        }

        if (progress > 0.7f) {
            // Cognitive principle this era teaches
            String principle = "Principle: " + era.getCognitivePrinciple();
            float pW = principle.length() * CHAR_W * 0.7f;
            float pAlpha = Math.min(1.0f, (progress - 0.7f) * 3);
            drawTextScaled(principle, (sw - pW) / 2, sh * 0.58f, 0.7f,
                    0.5f, 0.5f, 0.55f, pAlpha * 0.6f);
        }

        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Evidence board overlay.
     */
    public void renderEvidenceBoardOverlay(EvidenceBoard board) {
        glDisable(GL_DEPTH_TEST);

        float sw = window.getWidth();
        float sh = window.getHeight();

        // Dim background
        drawRect(0, 0, sw, sh, 0.0f, 0.0f, 0.0f, 0.7f);

        // Title
        String title = "EVIDENCE BOARD";
        float titleScale = 1.5f;
        float titleW = title.length() * CHAR_W * titleScale;
        drawTextScaled(title, (sw - titleW) / 2, 30, titleScale, 0.7f, 0.6f, 0.4f, 0.9f);

        // Categories
        float colWidth = sw / 3;

        // Verified Facts
        drawText("VERIFIED FACTS", colWidth * 0 + 30, 80, 0.3f, 0.7f, 0.3f, 0.8f);
        int y = 110;
        for (com.ontime.logic.EvidenceBoard.Evidence fact : board.getVerifiedFacts()) {
            drawText(fact.text, colWidth * 0 + 30, y, 0.6f, 0.6f, 0.55f, 0.7f);
            y += 22;
        }

        // Inferences
        drawText("INFERENCES", colWidth * 1 + 30, 80, 0.7f, 0.7f, 0.3f, 0.8f);
        y = 110;
        for (com.ontime.logic.EvidenceBoard.Evidence inference : board.getInferences()) {
            drawText(inference.text, colWidth * 1 + 30, y, 0.6f, 0.6f, 0.55f, 0.7f);
            y += 22;
        }

        // Emotional Reactions
        drawText("EMOTIONAL REACTIONS", colWidth * 2 + 30, 80, 0.7f, 0.3f, 0.3f, 0.8f);
        y = 110;
        for (com.ontime.logic.EvidenceBoard.Evidence reaction : board.getEmotionalReactions()) {
            drawText(reaction.text, colWidth * 2 + 30, y, 0.6f, 0.6f, 0.55f, 0.7f);
            y += 22;
        }

        // Hint
        String hint = "Press TAB or ESC to close";
        float hintW = hint.length() * CHAR_W;
        drawText(hint, (sw - hintW) / 2, sh - 40, 0.4f, 0.4f, 0.4f, 0.5f);

        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Pause menu overlay.
     */
    public void renderPauseMenu() {
        glDisable(GL_DEPTH_TEST);

        float sw = window.getWidth();
        float sh = window.getHeight();

        drawRect(0, 0, sw, sh, 0.0f, 0.0f, 0.0f, 0.6f);

        String paused = "PAUSED";
        float pw = paused.length() * CHAR_W * 2;
        drawTextScaled(paused, (sw - pw) / 2, sh * 0.4f, 2.0f, 0.7f, 0.6f, 0.5f, 0.9f);

        String hint = "Press ESC to resume";
        float hw = hint.length() * CHAR_W;
        drawText(hint, (sw - hw) / 2, sh * 0.52f, 0.5f, 0.4f, 0.4f, 0.6f);

        glEnable(GL_DEPTH_TEST);
    }

    // --- Drawing helpers ---

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

    private void drawTextScaled(String text, float x, float y, float scale,
                                 float r, float g, float b, float a) {
        float cw = CHAR_W * scale;
        float ch = CHAR_H * scale;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != ' ') {
                drawRect(x + i * cw, y, cw - 2, ch - 2, r, g, b, a);
            }
        }
    }

    public void cleanup() {
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        glDeleteProgram(shaderProgram);
    }
}
