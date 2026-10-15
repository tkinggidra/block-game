
package io.github.tkinggidra.blockgame.client.window;

import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Window implements AutoCloseable {
    private final long handle;
    private boolean closed;

    Window(int width, int height, String title) {
        long createdHandle = NULL;

        try {
            glfwDefaultWindowHints();

            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(
                    GLFW_OPENGL_PROFILE,
                    GLFW_OPENGL_CORE_PROFILE
            );

            createdHandle = glfwCreateWindow(
                    width, height, title, NULL, NULL
            );

            if (createdHandle == NULL) {
                throw new IllegalStateException(
                        "Failed to create GLFW window"
                );
            }

            glfwMakeContextCurrent(createdHandle);
            GL.createCapabilities();

            glfwSwapInterval(1);

            glfwShowWindow(createdHandle);

        } catch (RuntimeException | Error failure) {
            if (createdHandle != NULL) {
                glfwMakeContextCurrent(NULL);
                GL.setCapabilities(null);
                glfwDestroyWindow(createdHandle);
            }

            throw failure;
        }

        this.handle = createdHandle;
    }

    public boolean shouldClose() {
        ensureOpen();
        return glfwWindowShouldClose(handle);
    }

    public void updateViewport() {
        ensureOpen();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);

            glfwGetFramebufferSize(handle, width, height);

            glViewport(0, 0, width.get(0), height.get(0));
        }
    }

    public void swapBuffers() {
        ensureOpen();
        glfwSwapBuffers(handle);
    }

    public long getHandle() {
        ensureOpen();
        return handle;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Window is closed");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        glfwMakeContextCurrent(NULL);
        GL.setCapabilities(null);
        glfwDestroyWindow(handle);
    }
}
