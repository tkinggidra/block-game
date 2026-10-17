
package blockgame.client.debug;

import blockgame.client.graphics.opengl.OpenGLMesh;
import blockgame.client.graphics.opengl.OpenGLShader;
import blockgame.graphics.camera.Camera;
import blockgame.graphics.mesh.*;
import blockgame.math.Transform;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RenderSmokeTest implements AutoCloseable {
    private static final String VERTEX_SHADER = """
            #version 330 core

            layout(location = 0) in vec3 position;

            uniform mat4 model;
            uniform mat4 view;
            uniform mat4 projection;

            void main() {
                gl_Position = projection * view * model * vec4(position, 1.0);
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 330 core

            out vec4 fragColor;

            void main() {
                fragColor = vec4(0.95, 0.45, 0.15, 1.0);
            }
            """;

    private final Camera camera = new Camera();

    private final Transform left = new Transform();
    private final Transform center = new Transform();
    private final Transform right = new Transform();

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

        indices.putInt(0);
        indices.putInt(1);
        indices.putInt(2);
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

        camera.transform().setPosition(0.0f, 0.0f, 4.0f);

        left.setPosition(-1.5f, 0.0f, 0.0f);
        center.setPosition(0.0f, 0.0f, -1.0f);
        right.setPosition(1.5f, 0.0f, 0.0f);
    }


    public void render(float aspectRatio) {
        shader.bind();

        try {
            shader.setMatrix4("view", camera.viewMatrix());
            shader.setMatrix4(
                    "projection",
                    camera.projectionMatrix(aspectRatio)
            );

            draw(left);
            draw(center);
            draw(right);
        } finally {
            shader.unbind();
        }
    }

    private void draw(Transform transform) {
        shader.setMatrix4("model", transform.matrix());
        mesh.draw();
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
