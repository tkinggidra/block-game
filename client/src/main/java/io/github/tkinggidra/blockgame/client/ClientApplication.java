
package io.github.tkinggidra.blockgame.client;

import io.github.tkinggidra.blockgame.client.debug.DebugOverlay;
import io.github.tkinggidra.blockgame.client.debug.ImGuiLayer;
import io.github.tkinggidra.blockgame.client.window.GlfwRuntime;
import io.github.tkinggidra.blockgame.client.window.Window;

import static org.lwjgl.opengl.GL11.*;

public final class ClientApplication {
    public void run() {
        try (GlfwRuntime glfw = new GlfwRuntime()) {
            Window window = glfw.createWindow(
                    1280, 720,
                    "Block Game - " + BuildInfo.VERSION
            );

            try (ImGuiLayer imgui = new ImGuiLayer(window.getHandle())) {
                DebugOverlay debugOverlay = new DebugOverlay();

                while (!window.shouldClose()) {
                    glfw.pollEvents();

                    window.updateViewport();

                    glClearColor(0.00f, 0.09f, 0.12f, 1.0f);
                    glClear(
                            GL_COLOR_BUFFER_BIT
                                    | GL_DEPTH_BUFFER_BIT
                    );

                    imgui.beginFrame();
                    debugOverlay.render();
                    imgui.endFrame();

                    window.swapBuffers();
                }
            }
        }
    }
}
