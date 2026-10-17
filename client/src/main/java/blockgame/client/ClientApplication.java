package blockgame.client;

import blockgame.client.debug.DeveloperWorkspace;
import blockgame.client.debug.GameView;
import blockgame.client.debug.ImGuiLayer;
import blockgame.client.debug.RenderSmokeTest;
import blockgame.client.graphics.opengl.OpenGLFramebuffer;
import blockgame.client.input.DeveloperShortcut;
import blockgame.client.window.GlfwRuntime;
import blockgame.client.window.Window;

import static org.lwjgl.opengl.GL11.*;

public final class ClientApplication {
    public void run() {
        try (GlfwRuntime glfw = new GlfwRuntime()) {
            Window window = glfw.createWindow(
                    960, 480,
                    "Block Game - " + BuildInfo.VERSION
            );

            try (RenderSmokeTest renderTest = new RenderSmokeTest();
                 DeveloperShortcut shortcut = new DeveloperShortcut(window.getHandle());
                 ImGuiLayer imgui = new ImGuiLayer(window.getHandle());
                 OpenGLFramebuffer framebuffer = new OpenGLFramebuffer(1, 1)) {

                DeveloperWorkspace workspace = new DeveloperWorkspace();
                GameView.SceneRenderer renderer = renderTest::render;

                glfw.runLoop(() -> {
                    imgui.setEnabled(shortcut.isOpen());

                    glClearColor(0.00f, 0.09f, 0.12f, 1.0f);
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                    if (!shortcut.isOpen()) {
                        float aspectRatio = window.framebufferAspectRatio();

                        if (aspectRatio > 0.0f) {
                            renderTest.render(aspectRatio);
                        }
                    }

                    if (shortcut.isOpen()) {
                        imgui.beginFrame();
                        workspace.render(framebuffer, renderer);
                        imgui.endFrame();
                    }
                });
            }
        }
    }
}
