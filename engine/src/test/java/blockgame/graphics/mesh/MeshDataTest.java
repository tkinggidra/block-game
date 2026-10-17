
package blockgame.graphics.mesh;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class MeshDataTest {

    private static final VertexLayout LAYOUT =
            VertexLayout.builder()
                    .add("position", ComponentType.FLOAT32, 3)
                    .build();

    @Test
    void calculatesLayoutOffsets() {
        VertexLayout layout = VertexLayout.builder()
                .add("position", ComponentType.FLOAT32, 3)
                .add("normal", ComponentType.FLOAT32, 3)
                .add("uv", ComponentType.FLOAT32, 2)
                .build();

        assertEquals(32, layout.stride());
        assertEquals(0, layout.find("position").offset());
        assertEquals(12, layout.find("normal").offset());
        assertEquals(24, layout.find("uv").offset());
    }

    @Test
    void rejectsDuplicateAttributes() {
        assertThrows(IllegalArgumentException.class, () ->
                VertexLayout.builder()
                        .add("position", ComponentType.FLOAT32, 3)
                        .add("position", ComponentType.FLOAT32, 3)
                        .build()
        );
    }

    @Test
    void rejectsOutOfRangeIndices() {
        ByteBuffer vertices = ByteBuffer.allocate(3 * LAYOUT.stride());

        ByteBuffer indices = ByteBuffer.allocate(3 * Integer.BYTES)
                .order(ByteOrder.nativeOrder());

        indices.putInt(0).putInt(1).putInt(999).flip();

        assertThrows(IllegalArgumentException.class, () ->
                new MeshData(
                        LAYOUT,
                        vertices,
                        3,
                        indices,
                        IndexType.UINT32,
                        3
                )
        );
    }

    @Test
    void copiesInputBuffers() {
        ByteBuffer vertices = ByteBuffer.allocate(LAYOUT.stride());
        ByteBuffer indices = ByteBuffer.allocate(0);

        MeshData mesh = new MeshData(
                LAYOUT,
                vertices,
                1,
                indices,
                IndexType.UINT16,
                0
        );

        vertices.put(0, (byte) 42);

        assertEquals(0, mesh.vertices().get(0));
        assertTrue(mesh.vertices().isReadOnly());
    }

    @Test
    void rejectsIncorrectVertexBufferSize() {
        ByteBuffer vertices = ByteBuffer.allocate(4);
        ByteBuffer indices = ByteBuffer.allocate(0);

        assertThrows(IllegalArgumentException.class, () ->
                new MeshData(
                        LAYOUT,
                        vertices,
                        1,
                        indices,
                        IndexType.UINT16,
                        0
                )
        );
    }
}
