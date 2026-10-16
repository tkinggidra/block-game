
package io.github.tkinggidra.blockgame.client.graphics.opengl;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public final class OpenGLShader implements AutoCloseable {
    private final int programId;
    private boolean closed;

    public OpenGLShader(String vertexSource, String fragmentSource) {
        Objects.requireNonNull(vertexSource, "vertexSource");
        Objects.requireNonNull(fragmentSource, "fragmentSource");

        int vertexShader = 0;
        int fragmentShader = 0;
        int program = 0;

        try {
            vertexShader = compile(
                    GL_VERTEX_SHADER,
                    vertexSource,
                    "vertex"
            );

            fragmentShader = compile(
                    GL_FRAGMENT_SHADER,
                    fragmentSource,
                    "fragment"
            );

            program = glCreateProgram();

            if (program == 0) {
                throw new IllegalStateException(
                        "Failed to create OpenGL shader program"
                );
            }

            glAttachShader(program, vertexShader);
            glAttachShader(program, fragmentShader);
            glLinkProgram(program);

            if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
                throw new IllegalStateException(
                        "Shader program linking failed:\n"
                                + glGetProgramInfoLog(program)
                );
            }

        } catch (RuntimeException | Error failure) {
            if (program != 0) {
                glDeleteProgram(program);
            }

            throw failure;
        } finally {
            if (vertexShader != 0) {
                glDeleteShader(vertexShader);
            }

            if (fragmentShader != 0) {
                glDeleteShader(fragmentShader);
            }
        }

        this.programId = program;
    }

    private static int compile(
            int type,
            String source,
            String stageName
    ) {
        int shader = glCreateShader(type);

        if (shader == 0) {
            throw new IllegalStateException(
                    "Failed to create " + stageName + " shader"
            );
        }

        try {
            glShaderSource(shader, source);
            glCompileShader(shader);

            if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
                throw new IllegalStateException(
                        stageName + " shader compilation failed:\n"
                                + glGetShaderInfoLog(shader)
                );
            }

            return shader;

        } catch (RuntimeException | Error failure) {
            glDeleteShader(shader);
            throw failure;
        }
    }

    public void bind() {
        ensureOpen();
        glUseProgram(programId);
    }

    public void unbind() {
        ensureOpen();
        glUseProgram(0);
    }

    public void setMatrix4(String name, Matrix4f matrix) {
        ensureOpen();

        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(matrix, "matrix");

        int location = glGetUniformLocation(programId, name);

        if (location == -1) {
            throw new IllegalArgumentException(
                    "Shader uniform not found: " + name
            );
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);

            matrix.get(0, buffer);

            glUniformMatrix4fv(location, false, buffer);
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("OpenGL shader is closed");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;
        glDeleteProgram(programId);
    }
}
