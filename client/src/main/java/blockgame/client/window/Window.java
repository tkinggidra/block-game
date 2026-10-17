
package blockgame.client.window;

import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Window implements AutoCloseable {
    private final long handle;
    private final Thread ownerThread = Thread.currentThread();
    private final GLFWWindowRefreshCallback refreshCallback;

    private Runnable frameRenderer;
    private Throwable frameFailure;
    private boolean rendering;
    private boolean closed;

    Window(int width, int height, String title) {
        long createdHandle = NULL;
        GLFWWindowRefreshCallback createdRefreshCallback = null;

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

            glfwSetWindowSizeLimits(
                    createdHandle, width, height,
                    GLFW_DONT_CARE, GLFW_DONT_CARE
            );

            glfwMakeContextCurrent(createdHandle);
            GL.createCapabilities();

            glfwSwapInterval(1);

            createdRefreshCallback = GLFWWindowRefreshCallback.create(
                    ignored -> refresh()
            );
            glfwSetWindowRefreshCallback(createdHandle, createdRefreshCallback);

            glfwShowWindow(createdHandle);

        } catch (RuntimeException | Error failure) {
            if (createdHandle != NULL) {
                glfwMakeContextCurrent(NULL);
                GL.setCapabilities(null);
                glfwDestroyWindow(createdHandle);
            }

            if (createdRefreshCallback != null) {
                createdRefreshCallback.free();
            }

            throw failure;
        }

        this.handle = createdHandle;
        this.refreshCallback = createdRefreshCallback;
    }

    void setFrameRenderer(Runnable renderer) {
        ensureOpen();

        if (renderer != null && frameRenderer != null) {
            throw new IllegalStateException("A render loop is already running");
        }

        frameRenderer = renderer;
    }

    void checkFrameFailure() {
        if (frameFailure instanceof RuntimeException failure) {
            throw failure;
        }

        if (frameFailure instanceof Error failure) {
            throw failure;
        }
    }

    boolean renderFrame() {
        ensureOpen();
        checkFrameFailure();

        if (rendering || frameRenderer == null) {
            return false;
        }

        if (Thread.currentThread() != ownerThread || glfwGetCurrentContext() != handle) {
            throw new IllegalStateException("Rendering requires the owning thread and context");
        }

        rendering = true;

        try {
            if (glfwGetWindowAttrib(handle, GLFW_ICONIFIED) == GLFW_TRUE
                    || !updateViewport()) {
                return false;
            }

            frameRenderer.run();
            swapBuffers();
            return true;
        } finally {
            rendering = false;
        }
    }

    private void refresh() {
        if (frameRenderer == null || frameFailure != null) {
            return;
        }

        try {
            // Windows can request redraws while its sizing loop holds pollEvents().
            renderFrame();
        } catch (RuntimeException | Error failure) {
            // Propagate after native event dispatch returns, not through its callback.
            frameFailure = failure;
        }
    }

    public boolean shouldClose() {
        ensureOpen();
        return glfwWindowShouldClose(handle);
    }

    public float framebufferAspectRatio() {
        ensureOpen();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);

            glfwGetFramebufferSize(handle, width, height);

            int framebufferWidth = width.get(0);
            int framebufferHeight = height.get(0);

            if (framebufferWidth <= 0 || framebufferHeight <= 0) {
                return 0.0f;
            }

            return (float) framebufferWidth / framebufferHeight;
        }
    }

    public boolean updateViewport() {
        ensureOpen();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);

            glfwGetFramebufferSize(handle, width, height);

            if (width.get(0) <= 0 || height.get(0) <= 0) {
                return false;
            }

            glViewport(0, 0, width.get(0), height.get(0));
            return true;
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
        frameRenderer = null;

        glfwSetWindowRefreshCallback(handle, null);
        refreshCallback.free();

        glfwMakeContextCurrent(NULL);
        GL.setCapabilities(null);
        glfwDestroyWindow(handle);
    }
}
