
package io.github.tkinggidra.blockgame.client.debug;

import io.github.tkinggidra.blockgame.client.graphics.opengl.OpenGLMesh;
import io.github.tkinggidra.blockgame.client.graphics.opengl.OpenGLShader;
import io.github.tkinggidra.blockgame.engine.graphics.mesh.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RenderSmokeTest implements AutoCloseable {

    private static final String VERTEX_SHADER = """
            #version 330 core

            layout(location = 0) in vec3 position;

            void main() {
                gl_Position = vec4(position, 1.0);
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 330 core

            out vec4 fragColor;

            void main() {
                fragColor = vec4(0.95, 0.45, 0.15, 1.0);
            }
            """;

    private final OpenGLMesh mesh;
    private final OpenGLShader shader;

    public RenderSmokeTest() {
        VertexLayout layout = VertexLayout.builder()
                .add("position", ComponentType.FLOAT32, 3)
                .build();

        ByteBuffer vertices = ByteBuffer.allocateDirect(
                3 * layout.stride()
        ).order(ByteOrder.nativeOrder());

        vertices.putFloat(-0.5f).putFloat(-0.5f).putFloat(0.0f);
        vertices.putFloat( 0.5f).putFloat(-0.5f).putFloat(0.0f);
        vertices.putFloat( 0.0f).putFloat( 0.5f).putFloat(0.0f);
        vertices.flip();

        ByteBuffer indices = ByteBuffer.allocateDirect(
                3 * Integer.BYTES
        ).order(ByteOrder.nativeOrder());

        indices.putInt(0).putInt(1).putInt(2);
        indices.flip();

        MeshData meshData = new MeshData(
                layout,
                vertices,
                3,
                indices,
                IndexType.UINT32,
                3
        );

        OpenGLMesh createdMesh = null;
        OpenGLShader createdShader = null;

        try {
            createdMesh = new OpenGLMesh(meshData);
            createdShader = new OpenGLShader(
                    VERTEX_SHADER,
                    FRAGMENT_SHADER
            );
        } catch (RuntimeException | Error failure) {
            if (createdShader != null) {
                createdShader.close();
            }
            if (createdMesh != null) {
                createdMesh.close();
            }
            throw failure;
        }

        this.mesh = createdMesh;
        this.shader = createdShader;
    }

    public void render() {
        shader.bind();
        mesh.draw();
        shader.unbind();
    }

    @Override
    public void close() {
        try {
            shader.close();
        } finally {
            mesh.close();
        }
    }
}
