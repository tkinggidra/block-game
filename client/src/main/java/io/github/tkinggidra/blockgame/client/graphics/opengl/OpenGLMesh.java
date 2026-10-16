
package io.github.tkinggidra.blockgame.client.graphics.opengl;

import io.github.tkinggidra.blockgame.engine.graphics.mesh.*;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public final class OpenGLMesh implements AutoCloseable {
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final int indexCount;
    private final int indexType;

    private boolean closed;

    public OpenGLMesh(MeshData data) {
        Objects.requireNonNull(data, "data");

        int createdVao = 0;
        int createdVbo = 0;
        int createdEbo = 0;

        try {
            createdVao = glGenVertexArrays();
            createdVbo = glGenBuffers();
            createdEbo = glGenBuffers();

            glBindVertexArray(createdVao);

            glBindBuffer(GL_ARRAY_BUFFER, createdVbo);
            glBufferData(GL_ARRAY_BUFFER, data.vertices(), GL_STATIC_DRAW);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, createdEbo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, data.indices(), GL_STATIC_DRAW);

            configureAttributes(data.layout());

            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

        } catch (RuntimeException | Error failure) {
            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            if (createdEbo != 0) glDeleteBuffers(createdEbo);
            if (createdVbo != 0) glDeleteBuffers(createdVbo);
            if (createdVao != 0) glDeleteVertexArrays(createdVao);

            throw failure;
        }

        this.vao = createdVao;
        this.vbo = createdVbo;
        this.ebo = createdEbo;
        this.indexCount = data.indexCount();
        this.indexType = switch (data.indexType()) {
            case UINT16 -> GL_UNSIGNED_SHORT;
            case UINT32 -> GL_UNSIGNED_INT;
        };
    }

    private static void configureAttributes(VertexLayout layout) {
        int location = 0;

        for (VertexLayout.Element element : layout.elements()) {
            VertexAttribute attribute = element.attribute();

            int glType = toGLType(attribute.type());

            glEnableVertexAttribArray(location);

            glVertexAttribPointer(
                    location,
                    attribute.components(),
                    glType,
                    attribute.normalized(),
                    layout.stride(),
                    element.offset()
            );

            location++;
        }
    }

    private static int toGLType(ComponentType type) {
        return switch (type) {
            case FLOAT32 -> GL_FLOAT;
            case UINT8 -> GL_UNSIGNED_BYTE;
            case UINT16 -> GL_UNSIGNED_SHORT;
            case UINT32 -> GL_UNSIGNED_INT;
            case INT8 -> GL_BYTE;
            case INT16 -> GL_SHORT;
            case INT32 -> GL_INT;
        };
    }

    public void draw() {
        ensureOpen();

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indexCount, indexType, 0L);
        glBindVertexArray(0);
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("OpenGL mesh is closed");
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;

        glDeleteBuffers(ebo);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
