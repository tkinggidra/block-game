
package io.github.tkinggidra.blockgame.client.window;

import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public final class GlfwRuntime implements AutoCloseable {
    private static boolean active;

    private final GLFWErrorCallback errorCallback;
    private Window window;
    private boolean closed;

    public GlfwRuntime() {
        if (active) {
            throw new IllegalStateException(
                    "GLFW runtime already exists in this process"
            );
        }

        active = true;

        GLFWErrorCallback callback = null;

        try {
            callback = GLFWErrorCallback.createPrint(System.err);
            callback.set();

            if (!glfwInit()) {
                throw new IllegalStateException("Failed to initialize GLFW");
            }
        } catch (RuntimeException | Error failure) {
            glfwSetErrorCallback(null);

            if (callback != null) {
                callback.free();
            }

            active = false;
            throw failure;
        }

        this.errorCallback = callback;
    }

    public Window createWindow(int width, int height, String title) {
        ensureOpen();

        if (window != null) {
            throw new IllegalStateException(
                    "This instance already owns a window"
            );
        }

        window = new Window(width, height, title);
        return window;
    }

    public void pollEvents() {
        ensureOpen();
        glfwPollEvents();
    }

    public void runLoop(Runnable renderFrame) {
        ensureOpen();

        if (window == null) {
            throw new IllegalStateException("Create a window before running the render loop");
        }

        window.setFrameRenderer(Objects.requireNonNull(renderFrame, "renderFrame"));

        try {
            while (!window.shouldClose()) {
                pollEvents();
                window.checkFrameFailure();

                if (window.shouldClose()) {
                    break;
                }

                if (!window.renderFrame()) {
                    // Minimized and zero-sized windows have nothing to present.
                    glfwWaitEventsTimeout(0.1);
                    window.checkFrameFailure();
                }
            }
        } finally {
            window.setFrameRenderer(null);
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("GLFW runtime is closed");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        try {
            if (window != null) {
                window.close();
            }
        } finally {
            try {
                glfwTerminate();
            } finally {
                glfwSetErrorCallback(null);
                errorCallback.free();
                active = false;
            }
        }
    }
}
