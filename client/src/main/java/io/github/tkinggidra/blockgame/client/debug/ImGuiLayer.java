
package io.github.tkinggidra.blockgame.client.debug;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public final class ImGuiLayer implements AutoCloseable {
    private final ImGuiImplGlfw glfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 gl3 = new ImGuiImplGl3();

    private boolean closed;

    public ImGuiLayer(long windowHandle) {
        boolean contextCreated = false;
        boolean glfwInitialized = false;

        try {
            ImGui.createContext();
            contextCreated = true;

            ImGui.styleColorsDark();

            glfw.init(windowHandle, true);
            glfwInitialized = true;

            gl3.init("#version 330 core");

        } catch (RuntimeException | Error failure) {
            if (glfwInitialized) {
                glfw.shutdown();
            }

            if (contextCreated) {
                ImGui.destroyContext();
            }

            throw failure;
        }
    }

    public void beginFrame() {
        ensureOpen();

        gl3.newFrame();
        glfw.newFrame();
        ImGui.newFrame();
    }

    public void endFrame() {
        ensureOpen();

        ImGui.render();
        gl3.renderDrawData(ImGui.getDrawData());
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
                ImGui.destroyContext();
            }
        }
    }
}
