package blockgame.client.debug;

import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class ConsolePanel {
    private static final int MAX_LINES = 256;

    private final Deque<String> lines = new ArrayDeque<>(MAX_LINES);

    public void addLine(String line) {
        Objects.requireNonNull(line, "line");

        if (lines.size() == MAX_LINES) {
            lines.removeFirst();
        }

        lines.addLast(line);
    }

    public void render(ImBoolean open) {
        boolean visible = ImGui.begin("Console", open);

        try {
            if (!visible) {
                return;
            }

            if (ImGui.button("Clear")) {
                lines.clear();
            }

            ImGui.sameLine();

            if (ImGui.button("Copy")) {
                ImGui.setClipboardText(String.join(System.lineSeparator(), lines));
            }

            ImGui.separator();

            for (String line : lines) {
                ImGui.textUnformatted(line);
            }
        } finally {
            ImGui.end();
        }
    }
}
