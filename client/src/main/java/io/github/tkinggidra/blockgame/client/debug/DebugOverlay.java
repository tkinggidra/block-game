package io.github.tkinggidra.blockgame.client.debug;

import imgui.ImGui;
import io.github.tkinggidra.blockgame.client.BuildInfo;

import static org.lwjgl.opengl.GL11.*;

public final class DebugOverlay {
    public void render() {
        ImGui.begin("block-game : version #" + BuildInfo.VERSION);

        ImGui.text("Engine Bootstrap");
        ImGui.separator();

        ImGui.text("FPS: %.1f".formatted(ImGui.getIO().getFramerate()));
        ImGui.text("Frame time: %.2f ms".formatted(
                1000.0f / Math.max(ImGui.getIO().getFramerate(), 0.001f)
        ));

        ImGui.separator();

        ImGui.text("Renderer: OpenGL");
        ImGui.text("GPU: " + glGetString(GL_RENDERER));
        ImGui.text("OpenGL: " + glGetString(GL_VERSION));

        ImGui.end();
    }
}
