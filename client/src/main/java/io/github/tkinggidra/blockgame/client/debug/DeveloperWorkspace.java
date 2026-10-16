package io.github.tkinggidra.blockgame.client.debug;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import io.github.tkinggidra.blockgame.client.BuildInfo;
import io.github.tkinggidra.blockgame.client.graphics.opengl.OpenGLFramebuffer;

public final class DeveloperWorkspace {
    private static final String DOCKSPACE_ID = "DeveloperDockSpace";

    private final GameView gameView = new GameView();
    private final DebugOverlay diagnostics = new DebugOverlay();
    private final ConsolePanel console = new ConsolePanel();

    private final ImBoolean diagnosticsOpen = new ImBoolean();
    private final ImBoolean consoleOpen = new ImBoolean();
    private final ImBoolean aboutOpen = new ImBoolean();

    private boolean initialized;
    private boolean resetLayout;

    public void render(
            OpenGLFramebuffer framebuffer,
            GameView.SceneRenderer renderer
    ) {
        var viewport = ImGui.getMainViewport();

        ImGui.setNextWindowPos(viewport.getPosX(), viewport.getPosY());
        ImGui.setNextWindowSize(viewport.getSizeX(), viewport.getSizeY());
        ImGui.setNextWindowViewport(viewport.getID());

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f);

        int windowFlags = ImGuiWindowFlags.MenuBar
                | ImGuiWindowFlags.NoDocking
                | ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoBringToFrontOnFocus
                | ImGuiWindowFlags.NoNavFocus;

        ImGui.begin("Developer Workspace", windowFlags);
        ImGui.popStyleVar(3);

        try {
            renderMenuBar();

            int dockspaceId = ImGui.getID(DOCKSPACE_ID);

            if (!initialized || resetLayout) {
                // Check before dockSpace(), which would create an empty node.
                var existingNode = imgui.internal.ImGui.dockBuilderGetNode(dockspaceId);

                if (resetLayout || existingNode.isNotValidPtr()) {
                    initializeLayout(dockspaceId);
                }

                initialized = true;
                resetLayout = false;
            }

            ImGui.dockSpace(
                    dockspaceId, 0.0f, 0.0f,
                    ImGuiDockNodeFlags.NoDockingOverCentralNode
            );

            // Keep the game in the central leaf even when tools split the layout.
            var center = imgui.internal.ImGui.dockBuilderGetCentralNode(dockspaceId);

            if (center.isValidPtr()) {
                ImGui.setNextWindowDockID(center.getID(), ImGuiCond.Always);
            }

            gameView.render(framebuffer, renderer);

            if (diagnosticsOpen.get()) {
                diagnostics.render(diagnosticsOpen);
            }

            if (consoleOpen.get()) {
                console.render(consoleOpen);
            }

            if (aboutOpen.get()) {
                renderAbout();
            }
        } finally {
            ImGui.end();
        }
    }

    private void renderMenuBar() {
        if (!ImGui.beginMenuBar()) {
            return;
        }

        try {
            ImGui.beginMenu("File", false);

            if (ImGui.beginMenu("Window")) {
                try {
                    if (ImGui.menuItem("Game View")) {
                        ImGui.setWindowFocus("Game View");
                    }

                    if (ImGui.menuItem("Diagnostics")) {
                        diagnosticsOpen.set(true);
                        ImGui.setWindowCollapsed("Diagnostics", false);
                        ImGui.setWindowFocus("Diagnostics");
                    }

                    if (ImGui.menuItem("Console")) {
                        consoleOpen.set(true);
                        ImGui.setWindowCollapsed("Console", false);
                        ImGui.setWindowFocus("Console");
                    }
                } finally {
                    ImGui.endMenu();
                }
            }

            ImGui.beginMenu("Game", false);

            if (ImGui.beginMenu("View")) {
                try {
                    if (ImGui.menuItem("Reset Layout")) {
                        resetLayout = true;
                    }
                } finally {
                    ImGui.endMenu();
                }
            }

            if (ImGui.beginMenu("Help")) {
                try {
                    if (ImGui.menuItem("About Block Game")) {
                        aboutOpen.set(true);
                        ImGui.setWindowFocus("About Block Game");
                    }
                } finally {
                    ImGui.endMenu();
                }
            }
        } finally {
            ImGui.endMenuBar();
        }
    }

    private void initializeLayout(int dockspaceId) {
        imgui.internal.ImGui.dockBuilderRemoveNode(dockspaceId);
        imgui.internal.ImGui.dockBuilderAddNode(
                dockspaceId, imgui.internal.flag.ImGuiDockNodeFlags.DockSpace
        );
        imgui.internal.ImGui.dockBuilderSetNodeSize(
                dockspaceId,
                ImGui.getContentRegionAvailX(),
                ImGui.getContentRegionAvailY()
        );

        ImInt center = new ImInt(dockspaceId);
        ImInt right = new ImInt();
        ImInt bottom = new ImInt();

        imgui.internal.ImGui.dockBuilderSplitNode(
                center.get(), ImGuiDir.Right, 0.25f, right, center
        );
        imgui.internal.ImGui.dockBuilderSplitNode(
                center.get(), ImGuiDir.Down, 0.25f, bottom, center
        );

        imgui.internal.ImGui.dockBuilderDockWindow("Game View", center.get());
        imgui.internal.ImGui.dockBuilderDockWindow("Diagnostics", right.get());
        imgui.internal.ImGui.dockBuilderDockWindow("Console", bottom.get());
        imgui.internal.ImGui.dockBuilderFinish(dockspaceId);

        console.addLine("Workspace layout initialized.");
    }

    private void renderAbout() {
        ImGui.setNextWindowSize(600.0f, 0.0f, ImGuiCond.FirstUseEver);
        boolean visible = ImGui.begin(
                "About Block Game", aboutOpen,
                ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.AlwaysAutoResize
        );

        try {
            if (visible) {
                ImGui.textUnformatted("Block Game - " + BuildInfo.VERSION);
                ImGui.separator();
                ImGui.textWrapped("A voxel sandbox game with modding capabilities.");
                ImGui.textUnformatted("Right Shift toggles the developer workspace.");
            }
        } finally {
            ImGui.end();
        }
    }
}
