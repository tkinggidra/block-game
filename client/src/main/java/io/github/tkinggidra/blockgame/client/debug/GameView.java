
package io.github.tkinggidra.blockgame.client.debug;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import io.github.tkinggidra.blockgame.client.graphics.opengl.OpenGLFramebuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public final class GameView {
    @FunctionalInterface
    public interface SceneRenderer {
        void render(float aspectRatio);
    }

    public void render(
            OpenGLFramebuffer framebuffer,
            SceneRenderer renderer
    ) {
        boolean visible = ImGui.begin(
                "Game View", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove
        );

        try {
            if (!visible) {
                return;
            }

            float availableWidth = ImGui.getContentRegionAvailX();
            float availableHeight = ImGui.getContentRegionAvailY();

            if (availableWidth < 1 || availableHeight < 1) {
                ImGui.textDisabled("No space available for rendering");
                return;
            }

            float scaleX = ImGui.getIO().getDisplayFramebufferScaleX();
            float scaleY = ImGui.getIO().getDisplayFramebufferScaleY();

            int width = Math.max(1, Math.round(availableWidth * scaleX));
            int height = Math.max(1, Math.round(availableHeight * scaleY));

            framebuffer.resize(width, height);

            framebuffer.bind();

            try {
                glClearColor(0.00f, 0.09f, 0.12f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                renderer.render((float) width / height);
            } finally {
                glBindFramebuffer(GL_FRAMEBUFFER, 0);
                glViewport(
                        0, 0,
                        Math.round(ImGui.getIO().getDisplaySizeX() * scaleX),
                        Math.round(ImGui.getIO().getDisplaySizeY() * scaleY)
                );
            }

            ImGui.image(
                    framebuffer.textureId(),
                    availableWidth,
                    availableHeight,
                    0.0f, 1.0f,
                    1.0f, 0.0f
            );

        } finally {
            ImGui.end();
        }
    }
}
