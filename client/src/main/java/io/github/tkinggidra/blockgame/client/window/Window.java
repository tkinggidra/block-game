package io.github.tkinggidra.blockgame.client.window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Window implements AutoCloseable {
    private final long handle;

    public Window(int width, int height, String title) {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            GLFWErrorCallback callback = glfwSetErrorCallback(null);
            if (callback != null) callback.free();

            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        handle = glfwCreateWindow(width, height, title, NULL, NULL);

        if (handle == NULL) {
            glfwTerminate();

            GLFWErrorCallback callback = glfwSetErrorCallback(null);
            if (callback != null) callback.free();

            throw new IllegalStateException("Failed to create window");
        }

        glfwMakeContextCurrent(handle);
        GL.createCapabilities();

        glfwSwapInterval(1);

        glfwSetFramebufferSizeCallback(handle, (window, w, h) -> {
            glViewport(0, 0, w, h);
        });

        glViewport(0, 0, width, height);

        glfwShowWindow(handle);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void close() {
        glfwDestroyWindow(handle);
        glfwTerminate();

        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) callback.free();
    }
}