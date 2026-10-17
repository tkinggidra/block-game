package blockgame.graphics.mesh;

import java.util.Objects;

public record VertexAttribute(
        String name,
        ComponentType type,
        int components,
        boolean normalized
) {
    public VertexAttribute {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");

        if (name.isBlank()) {
            throw new IllegalStateException("Attribute name cannot be blank");
        }

        if (components < 1 || components > 4) {
            throw new IllegalStateException(
                    "Attribute must have between 1 and 4 components"
            );
        }

        if (normalized && type == ComponentType.FLOAT32) {
            throw new IllegalArgumentException(
                    "Floating-point attributes cannot be normalized"
            );
        }
    }

    public int Size() {
        return Math.multiplyExact(type.size(), components);
    }
}
