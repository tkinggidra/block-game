
package blockgame.client.input;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;

public final class DeveloperShortcut implements AutoCloseable {
    private final long windowHandle;
    private final GLFWKeyCallback callback;
    private final GLFWKeyCallback previousCallback;

    private boolean closed;
    private boolean open;
    private boolean rightShiftDown;

    public DeveloperShortcut(long windowHandle) {
        this.windowHandle = windowHandle;
        callback = GLFWKeyCallback.create(this::onKey);

        try {
            previousCallback = glfwSetKeyCallback(windowHandle, callback);
        } catch (RuntimeException | Error failure) {
            callback.free();
            throw failure;
        }
    }

    private void onKey(
            long window,
            int key,
            int scancode,
            int action,
            int modifiers
    ) {
        if (key == GLFW_KEY_RIGHT_SHIFT) {
            if (action == GLFW_PRESS && !rightShiftDown) {
                open = !open;
                rightShiftDown = true;
            } else if (action == GLFW_RELEASE) {
                rightShiftDown = false;
            }

            return;
        }

        if (previousCallback != null) {
            previousCallback.invoke(window, key, scancode, action, modifiers);
        }
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        try {
            glfwSetKeyCallback(windowHandle, previousCallback);
        } finally {
            callback.free();
        }
    }
}
