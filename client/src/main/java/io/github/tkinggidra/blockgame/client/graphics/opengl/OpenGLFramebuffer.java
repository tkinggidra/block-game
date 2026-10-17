package io.github.tkinggidra.blockgame.client.graphics.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;

public final class OpenGLFramebuffer implements AutoCloseable {
    private final int framebuffer;
    private final int colorTexture;
    private final int depthRenderbuffer;

    private int width;
    private int height;
    private boolean closed;

    public OpenGLFramebuffer(int width, int height) {
        validateDimensions(width, height);

        int createdFramebuffer = 0;
        int createdTexture = 0;
        int createdRenderbuffer = 0;

        int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
        int previousTexture = glGetInteger(GL_TEXTURE_BINDING_2D);
        int previousRenderbuffer = glGetInteger(GL_RENDERBUFFER_BINDING);

        try {
            createdFramebuffer = glGenFramebuffers();
            createdTexture = glGenTextures();
            createdRenderbuffer = glGenRenderbuffers();

            glBindFramebuffer(GL_FRAMEBUFFER, createdFramebuffer);

            glBindTexture(GL_TEXTURE_2D, createdTexture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    width,
                    height,
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    (java.nio.ByteBuffer) null
            );

            glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_2D,
                    createdTexture,
                    0
            );

            glBindRenderbuffer(GL_RENDERBUFFER, createdRenderbuffer);

            glRenderbufferStorage(
                    GL_RENDERBUFFER,
                    GL_DEPTH24_STENCIL8,
                    width,
                    height
            );

            glFramebufferRenderbuffer(
                    GL_FRAMEBUFFER,
                    GL_DEPTH_STENCIL_ATTACHMENT,
                    GL_RENDERBUFFER,
                    createdRenderbuffer
            );

            checkComplete();

        } catch (RuntimeException | Error failure) {
            if (createdRenderbuffer != 0) {
                glDeleteRenderbuffers(createdRenderbuffer);
            }

            if (createdTexture != 0) {
                glDeleteTextures(createdTexture);
            }

            if (createdFramebuffer != 0) {
                glDeleteFramebuffers(createdFramebuffer);
            }

            throw failure;

        } finally {
            glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
            glBindTexture(GL_TEXTURE_2D, previousTexture);
            glBindRenderbuffer(GL_RENDERBUFFER, previousRenderbuffer);
        }

        this.framebuffer = createdFramebuffer;
        this.colorTexture = createdTexture;
        this.depthRenderbuffer = createdRenderbuffer;
        this.width = width;
        this.height = height;
    }

    public void bind() {
        ensureOpen();

        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glViewport(0, 0, width, height);
    }

    public void resize(int width, int height) {
        ensureOpen();
        validateDimensions(width, height);

        if (this.width == width && this.height == height) {
            return;
        }

        int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
        int previousTexture = glGetInteger(GL_TEXTURE_BINDING_2D);
        int previousRenderbuffer = glGetInteger(GL_RENDERBUFFER_BINDING);

        try {
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

            glBindTexture(GL_TEXTURE_2D, colorTexture);
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    width,
                    height,
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    (java.nio.ByteBuffer) null
            );

            glBindRenderbuffer(GL_RENDERBUFFER, depthRenderbuffer);
            glRenderbufferStorage(
                    GL_RENDERBUFFER,
                    GL_DEPTH24_STENCIL8,
                    width,
                    height
            );

            checkComplete();

            this.width = width;
            this.height = height;

        } finally {
            glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
            glBindTexture(GL_TEXTURE_2D, previousTexture);
            glBindRenderbuffer(GL_RENDERBUFFER, previousRenderbuffer);
        }
    }

    private static void checkComplete() {
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException(
                    "OpenGL framebuffer incomplete: 0x"
                            + Integer.toHexString(status)
            );
        }
    }

    private static void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Framebuffer dimensions must be positive"
            );
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("OpenGL framebuffer is closed");
        }
    }

    public int textureId() {
        ensureOpen();
        return colorTexture;
    }

    public int width() {
        ensureOpen();
        return width;
    }

    public int height() {
        ensureOpen();
        return height;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        glDeleteRenderbuffers(depthRenderbuffer);
        glDeleteTextures(colorTexture);
        glDeleteFramebuffers(framebuffer);
    }
}