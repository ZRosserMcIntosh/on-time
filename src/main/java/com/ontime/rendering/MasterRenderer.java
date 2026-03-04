package com.ontime.rendering;

import com.ontime.engine.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * 3D Master Renderer — manages the perspective projection, view matrix,
 * and coordinates all 3D and 2D overlay rendering passes.
 *
 * Rendering pipeline:
 * 1. 3D scene pass (perspective projection — environments, characters)
 * 2. 2D overlay pass (orthographic projection — dialogue, UI, HUD)
 * 3. Post-processing (transitions, effects)
 */
public class MasterRenderer {

    private final Window window;

    // 3D projection
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f modelMatrix = new Matrix4f();

    // Camera
    private final Camera3D camera;

    // Shader programs
    private int sceneShader;
    private int uiShader;

    // 3D scene shader source
    private static final String SCENE_VERTEX = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec3 aNormal;
            layout (location = 2) in vec2 aTexCoord;
            
            uniform mat4 uProjection;
            uniform mat4 uView;
            uniform mat4 uModel;
            
            out vec3 FragPos;
            out vec3 Normal;
            out vec2 TexCoord;
            
            void main() {
                FragPos = vec3(uModel * vec4(aPos, 1.0));
                Normal = mat3(transpose(inverse(uModel))) * aNormal;
                TexCoord = aTexCoord;
                gl_Position = uProjection * uView * uModel * vec4(aPos, 1.0);
            }
            """;

    private static final String SCENE_FRAGMENT = """
            #version 330 core
            in vec3 FragPos;
            in vec3 Normal;
            in vec2 TexCoord;
            
            out vec4 FragColor;
            
            uniform vec4 uColor;
            uniform vec3 uLightDir;
            uniform vec3 uLightColor;
            uniform vec3 uAmbient;
            uniform float uMoodIntensity;
            
            void main() {
                // Diffuse lighting
                vec3 norm = normalize(Normal);
                vec3 lightDir = normalize(uLightDir);
                float diff = max(dot(norm, lightDir), 0.0);
                vec3 diffuse = diff * uLightColor;
                
                // Combine ambient + diffuse
                vec3 lighting = uAmbient + diffuse;
                vec3 result = lighting * uColor.rgb;
                
                // Mood tinting
                result = mix(result, result * vec3(1.0, 0.9, 0.8), uMoodIntensity);
                
                FragColor = vec4(result, uColor.a);
            }
            """;

    // 2D UI shader
    private static final String UI_VERTEX = """
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

    private static final String UI_FRAGMENT = """
            #version 330 core
            out vec4 FragColor;
            uniform vec4 uColor;
            void main() {
                FragColor = uColor;
            }
            """;

    public MasterRenderer(Window window) {
        this.window = window;
        this.camera = new Camera3D();

        // Compile shaders
        sceneShader = compileShader(SCENE_VERTEX, SCENE_FRAGMENT);
        uiShader = compileShader(UI_VERTEX, UI_FRAGMENT);

        // Set default camera position
        camera.setPosition(new Vector3f(0, 2, 5));
        camera.setTarget(new Vector3f(0, 1, 0));
    }

    public void begin() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Update projection matrix
        float aspect = window.getAspect();
        projectionMatrix.identity().perspective(
                (float) Math.toRadians(45.0f), aspect, 0.1f, 100.0f);

        // Update view matrix from camera
        camera.getViewMatrix(viewMatrix);
    }

    public void end() {
        // Nothing special needed
    }

    /**
     * Begin 3D scene rendering pass.
     */
    public void beginScenePass() {
        glEnable(GL_DEPTH_TEST);
        glUseProgram(sceneShader);

        // Upload matrices
        float[] projBuf = new float[16];
        float[] viewBuf = new float[16];
        projectionMatrix.get(projBuf);
        viewMatrix.get(viewBuf);

        glUniformMatrix4fv(glGetUniformLocation(sceneShader, "uProjection"), false, projBuf);
        glUniformMatrix4fv(glGetUniformLocation(sceneShader, "uView"), false, viewBuf);

        // Default lighting
        glUniform3f(glGetUniformLocation(sceneShader, "uLightDir"), 0.5f, 1.0f, 0.3f);
        glUniform3f(glGetUniformLocation(sceneShader, "uLightColor"), 1.0f, 0.95f, 0.9f);
        glUniform3f(glGetUniformLocation(sceneShader, "uAmbient"), 0.2f, 0.2f, 0.25f);
        glUniform1f(glGetUniformLocation(sceneShader, "uMoodIntensity"), 0.0f);
    }

    /**
     * Begin 2D overlay rendering pass (dialogue, UI, HUD).
     */
    public void beginUIPass() {
        glDisable(GL_DEPTH_TEST);
        glUseProgram(uiShader);
    }

    /**
     * Render a 3D colored box at a position (placeholder for models).
     */
    public void renderBox(Vector3f position, Vector3f scale, float r, float g, float b, float a) {
        glUseProgram(sceneShader);

        modelMatrix.identity()
                .translate(position)
                .scale(scale);

        float[] modelBuf = new float[16];
        modelMatrix.get(modelBuf);
        glUniformMatrix4fv(glGetUniformLocation(sceneShader, "uModel"), false, modelBuf);
        glUniform4f(glGetUniformLocation(sceneShader, "uColor"), r, g, b, a);

        // Draw using the cube VAO
        renderCube();
    }

    // Cube geometry
    private int cubeVAO = -1;
    private int cubeVBO = -1;

    private void renderCube() {
        if (cubeVAO == -1) {
            initCube();
        }
        glBindVertexArray(cubeVAO);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);
    }

    private void initCube() {
        float[] vertices = {
            // positions          // normals           // tex coords
            // Front face
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f,
             0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 0.0f,
             0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f,
             0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 1.0f,
            // Back face
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
             0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
             0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
             0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
            // Top face
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
             0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
             0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
             0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
            // Bottom face
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
             0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
             0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
             0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
            // Right face
             0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
             0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
             0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
             0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
             0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
             0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
            // Left face
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
            -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
        };

        cubeVAO = glGenVertexArrays();
        cubeVBO = glGenBuffers();
        glBindVertexArray(cubeVAO);
        glBindBuffer(GL_ARRAY_BUFFER, cubeVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int stride = 8 * 4; // 8 floats per vertex
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);           // position
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * 4);       // normal
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * 4);       // texcoord

        glBindVertexArray(0);
    }

    public void setMoodLighting(String mood, float intensity) {
        glUseProgram(sceneShader);
        switch (mood) {
            case "warm" -> {
                glUniform3f(glGetUniformLocation(sceneShader, "uLightColor"), 1.0f, 0.85f, 0.7f);
                glUniform3f(glGetUniformLocation(sceneShader, "uAmbient"), 0.25f, 0.2f, 0.15f);
            }
            case "tense" -> {
                glUniform3f(glGetUniformLocation(sceneShader, "uLightColor"), 0.7f, 0.7f, 0.8f);
                glUniform3f(glGetUniformLocation(sceneShader, "uAmbient"), 0.1f, 0.1f, 0.15f);
            }
            case "grief" -> {
                glUniform3f(glGetUniformLocation(sceneShader, "uLightColor"), 0.5f, 0.5f, 0.7f);
                glUniform3f(glGetUniformLocation(sceneShader, "uAmbient"), 0.08f, 0.08f, 0.12f);
            }
            case "hope" -> {
                glUniform3f(glGetUniformLocation(sceneShader, "uLightColor"), 0.9f, 0.95f, 1.0f);
                glUniform3f(glGetUniformLocation(sceneShader, "uAmbient"), 0.2f, 0.22f, 0.28f);
            }
            case "danger" -> {
                glUniform3f(glGetUniformLocation(sceneShader, "uLightColor"), 1.0f, 0.4f, 0.3f);
                glUniform3f(glGetUniformLocation(sceneShader, "uAmbient"), 0.15f, 0.05f, 0.05f);
            }
        }
        glUniform1f(glGetUniformLocation(sceneShader, "uMoodIntensity"), intensity);
    }

    public Camera3D getCamera() { return camera; }
    public int getSceneShader() { return sceneShader; }
    public int getUIShader() { return uiShader; }
    public Matrix4f getProjectionMatrix() { return projectionMatrix; }
    public Matrix4f getViewMatrix() { return viewMatrix; }

    private int compileShader(String vertSrc, String fragSrc) {
        int vert = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vert, vertSrc);
        glCompileShader(vert);
        if (glGetShaderi(vert, GL_COMPILE_STATUS) == 0) {
            System.err.println("Vertex shader error: " + glGetShaderInfoLog(vert));
        }

        int frag = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(frag, fragSrc);
        glCompileShader(frag);
        if (glGetShaderi(frag, GL_COMPILE_STATUS) == 0) {
            System.err.println("Fragment shader error: " + glGetShaderInfoLog(frag));
        }

        int prog = glCreateProgram();
        glAttachShader(prog, vert);
        glAttachShader(prog, frag);
        glLinkProgram(prog);
        if (glGetProgrami(prog, GL_LINK_STATUS) == 0) {
            System.err.println("Shader link error: " + glGetProgramInfoLog(prog));
        }

        glDeleteShader(vert);
        glDeleteShader(frag);
        return prog;
    }

    public void cleanup() {
        glDeleteProgram(sceneShader);
        glDeleteProgram(uiShader);
        if (cubeVAO != -1) {
            glDeleteVertexArrays(cubeVAO);
            glDeleteBuffers(cubeVBO);
        }
    }
}
