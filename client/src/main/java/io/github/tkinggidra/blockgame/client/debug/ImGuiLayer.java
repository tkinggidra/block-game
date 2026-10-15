package io.github.tkinggidra.blockgame.client.debug;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public final class ImGuiLayer implements AutoCloseable {
    private final ImGuiImplGlfw glfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 gl3 = new ImGuiImplGl3();

    public ImGuiLayer(long windowHandle) {
        ImGui.createContext();
        ImGui.styleColorsDark();

        glfw.init(windowHandle, true);
        gl3.init("#version 330 core");
    }

    public void beginFrame() {
        gl3.newFrame();
        glfw.newFrame();
        ImGui.newFrame();
    }

    public void endFrame() {
        ImGui.render();
        gl3.renderDrawData(ImGui.getDrawData());
    }

    @Override
    public void close() {
        gl3.shutdown();
        glfw.shutdown();
        ImGui.destroyContext();
    }
}
