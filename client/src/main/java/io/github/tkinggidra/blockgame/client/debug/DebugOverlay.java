
package io.github.tkinggidra.blockgame.client.debug;

import imgui.ImGui;
import io.github.tkinggidra.blockgame.client.BuildInfo;

import static org.lwjgl.opengl.GL11.*;

public final class DebugOverlay {
    public void render() {
        ImGui.begin("Block Game - Diagnostics");

        try {
            ImGui.text("Engine Bootstrap");
            ImGui.separator();

            float fps = ImGui.getIO().getFramerate();

            ImGui.text("FPS: %.1f".formatted(fps));
            ImGui.text("Frame time: %.2f ms".formatted(
                    1000.0f / Math.max(fps, 0.001f)
            ));

            ImGui.separator();

            ImGui.text("Renderer: OpenGL");
            ImGui.text("GPU: " + glGetString(GL_RENDERER));
            ImGui.text("OpenGL: " + glGetString(GL_VERSION));
            ImGui.text("Version: " + BuildInfo.VERSION);

        } finally {
            ImGui.end();
        }
    }
}
