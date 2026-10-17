package blockgame.graphics.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public final class MeshData {
    private final VertexLayout layout;
    private final ByteBuffer vertices;
    private final ByteBuffer indices;
    private final IndexType indexType;
    private final int vertexCount;
    private final int indexCount;

    public MeshData(
            VertexLayout layout,
            ByteBuffer vertices,
            int vertexCount,
            ByteBuffer indices,
            IndexType indexType,
            int indexCount
    ) {
        this.layout = Objects.requireNonNull(layout, "layout");
        this.indexType = Objects.requireNonNull(indexType, "indexType");

        Objects.requireNonNull(vertices, "vertices");
        Objects.requireNonNull(indices, "indices");

        if (vertexCount < 0 || indexCount < 0) {
            throw new IllegalArgumentException(
                    "Vertex and index counts cannot be negative"
            );
        }

        if (indexCount % 3 != 0) {
            throw new IllegalArgumentException(
                    "Triangle index count must be divisible by 3"
            );
        }

        int expectedVertexBytes = Math.multiplyExact(
                vertexCount,
                layout.stride()
        );

        int expectedIndexBytes = Math.multiplyExact(
                indexCount,
                indexType.size()
        );

        if (vertices.remaining() != expectedVertexBytes) {
            throw new IllegalArgumentException(
                    "Vertex buffer size does not match vertex count and layout"
            );
        }

        if (indices.remaining() != expectedIndexBytes) {
            throw new IllegalArgumentException(
                    "Index buffer size does not match index count and index type"
            );
        }

        validateIndices(indices, indexType, vertexCount);

        this.vertices = copy(vertices);
        this.indices = copy(indices);
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
    }

    private static void validateIndices(
            ByteBuffer indices,
            IndexType indexType,
            int vertexCount
    ) {
        ByteBuffer buffer = indices.duplicate()
                .order(ByteOrder.nativeOrder());

        int indexSize = indexType.size();

        for (int offset = buffer.position();
             offset < buffer.limit();
             offset += indexSize) {

            long index = switch (indexType) {
                case UINT16 -> Short.toUnsignedInt(buffer.getShort(offset));
                case UINT32 -> Integer.toUnsignedLong(buffer.getInt(offset));
            };

            if (index >= vertexCount) {
                throw new IllegalArgumentException(
                        "Index " + index +
                                " references a vertex outside the mesh"
                );
            }
        }
    }

    private static ByteBuffer copy(ByteBuffer source) {
        ByteBuffer input = source.duplicate();

        ByteBuffer result = ByteBuffer.allocateDirect(input.remaining())
                .order(ByteOrder.nativeOrder());

        result.put(input);
        result.flip();

        return result.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    public VertexLayout layout() {
        return layout;
    }

    public ByteBuffer vertices() {
        return vertices.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    public ByteBuffer indices() {
        return indices.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    public IndexType indexType() {
        return indexType;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int indexCount() {
        return indexCount;
    }

    public int triangleCount() {
        return indexCount / 3;
    }
}
