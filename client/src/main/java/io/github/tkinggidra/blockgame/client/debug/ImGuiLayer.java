
package io.github.tkinggidra.blockgame.client.debug;

import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.lwjgl.glfw.GLFW.glfwGetWindowContentScale;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class ImGuiLayer implements AutoCloseable {

    private static final float UI_SCALE = 1.5f;
    private static final float BASE_FONT_SIZE = 10.0f;

    private final ImGuiImplGlfw glfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 gl3 = new ImGuiImplGl3();
    private final long windowHandle;
    private final Path fontPath;

    private float scale = 1.0f;
    private boolean enabled;
    private boolean closed;

    public ImGuiLayer(long windowHandle) {
        this.windowHandle = windowHandle;
        this.fontPath = loadFont();

        boolean contextCreated = false;
        boolean glfwInitialized = false;
        boolean gl3Initialized = false;

        try {
            ImGui.createContext();
            contextCreated = true;

            ImGui.getIO().addConfigFlags(
                    ImGuiConfigFlags.DockingEnable
            );
            ImGui.getIO().setAppAcceptingEvents(false);

            ImGui.styleColorsDark();
            updateScale(getDisplayScale());

            glfw.init(windowHandle, true);
            glfwInitialized = true;

            gl3.init("#version 330 core");
            gl3Initialized = true;

            // The 1.92.7.1 Java backend builds and uploads its legacy atlas here.
            gl3.newFrame();

        } catch (RuntimeException | Error failure) {
            try {
                if (gl3Initialized) {
                    gl3.shutdown();
                }
            } catch (RuntimeException | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }

            try {
                if (glfwInitialized) {
                    glfw.shutdown();
                }
            } catch (RuntimeException | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }

            try {
                if (contextCreated) {
                    ImGui.destroyContext();
                }
            } catch (RuntimeException | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }

            deleteFont();
            throw failure;
        }
    }

    public void setEnabled(boolean enabled) {
        ensureOpen();

        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        var io = ImGui.getIO();
        io.clearEventsQueue();
        io.clearInputKeys();
        io.clearInputMouse();
        io.setAppAcceptingEvents(enabled);

        if (enabled) {
            // Resume the backend clock without counting time spent hidden.
            glfw.newFrame();
        } else {
            io.setWantCaptureMouse(false);
            io.setWantCaptureKeyboard(false);
            glfwSetCursor(windowHandle, NULL);
        }
    }

    public void beginFrame() {
        ensureOpen();

        float updatedScale = getDisplayScale();

        if (scale != updatedScale) {
            gl3.destroyFontsTexture();
            updateScale(updatedScale);
        }

        gl3.newFrame();
        glfw.newFrame();
        ImGui.newFrame();
    }

    public void endFrame() {
        ensureOpen();

        ImGui.render();
        gl3.renderDrawData(ImGui.getDrawData());
    }

    private float getDisplayScale() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var xScale = stack.mallocFloat(1);
            var yScale = stack.mallocFloat(1);

            glfwGetWindowContentScale(windowHandle, xScale, yScale);

            return UI_SCALE * Math.max(xScale.get(0), yScale.get(0));
        }
    }

    private void updateScale(float updatedScale) {
        // Resize the complete style only when display scaling changes.
        ImGui.getStyle().scaleAllSizes(updatedScale / scale);
        scale = updatedScale;

        var fonts = ImGui.getIO().getFonts();
        fonts.clear();

        if (fontPath != null) {
            fonts.addFontFromFileTTF(
                    fontPath.toString(), BASE_FONT_SIZE * scale
            );
        } else {
            ImFontConfig config = new ImFontConfig();

            try {
                config.setSizePixels(BASE_FONT_SIZE * scale);
                fonts.addFontDefaultVector(config);
            } finally {
                config.destroy();
            }
        }
    }

    private static Path loadFont() {
        Path path = null;

        try (var stream = ImGuiLayer.class.getResourceAsStream(
                "/fonts/JetBrainsMono-Regular.ttf")) {

            if (stream == null) {
                return null;
            }

            // File loading gives the atlas native ownership of its TTF data.
            path = Files.createTempFile("block-game-font-", ".ttf");
            Files.copy(stream, path, REPLACE_EXISTING);

            return path;
        } catch (IOException failure) {
            if (path != null) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }

            return null;
        }
    }

    private void deleteFont() {
        if (fontPath == null) {
            return;
        }

        try {
            Files.deleteIfExists(fontPath);
        } catch (IOException ignored) {
            fontPath.toFile().deleteOnExit();
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("ImGui layer is closed");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        try {
            gl3.shutdown();
        } finally {
            try {
                glfw.shutdown();
            } finally {
                try {
                    ImGui.destroyContext();
                } finally {
                    deleteFont();
                }
            }
        }
    }
}
