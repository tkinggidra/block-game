package io.github.tkinggidra.blockgame.client.debug;

import imgui.ImGui;
import imgui.type.ImBoolean;
import io.github.tkinggidra.blockgame.client.BuildInfo;

import static org.lwjgl.opengl.GL11.*;

public final class DebugOverlay {
    private static final double PERFORMANCE_REFRESH_INTERVAL = 0.25;

    private final String gpu = "GPU: " + glGetString(GL_RENDERER);
    private final String openGL = "OpenGL: " + glGetString(GL_VERSION);
    private final String version = "Version: " + BuildInfo.VERSION;

    private double nextPerformanceUpdate;
    private String fpsText;
    private String frameTimeText;

    public void render(ImBoolean open) {
        boolean visible = ImGui.begin("Diagnostics", open);

        try {
            if (!visible) {
                return;
            }

            if (ImGui.collapsingHeader("Performance & Hardware")) {
                double now = ImGui.getTime();

                if (now >= nextPerformanceUpdate) {
                    float fps = ImGui.getIO().getFramerate();

                    fpsText = "FPS: %.1f".formatted(fps);
                    frameTimeText = "Frame time: %.2f ms".formatted(
                            1000.0f / Math.max(fps, 0.001f)
                    );

                    nextPerformanceUpdate = now + PERFORMANCE_REFRESH_INTERVAL;
                }

                ImGui.textUnformatted(fpsText);
                ImGui.textUnformatted(frameTimeText);
                ImGui.separator();

                ImGui.textUnformatted(gpu);
                ImGui.textUnformatted(openGL);
            }

            if (ImGui.collapsingHeader("Runtime")) {
                ImGui.textUnformatted(version);
                ImGui.textUnformatted("Mode: Developer Workspace");
            }
        } finally {
            ImGui.end();
        }
    }
}
