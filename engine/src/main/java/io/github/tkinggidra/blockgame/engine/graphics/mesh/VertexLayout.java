package io.github.tkinggidra.blockgame.engine.graphics.mesh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class VertexLayout {
    public record Element(VertexAttribute attribute, int offset) {}

    private final List<Element> elements;
    private final int stride;

    private VertexLayout(List<VertexAttribute> attributes) {
        if (attributes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vertex layout must contain at least one attribute"
            );
        }

        List<Element> elemnts = new ArrayList<>();
        Set<String> names = new HashSet<>();

        int offset = 0;

        for (VertexAttribute attribute : attributes) {
            Objects.requireNonNull(attribute, "attribute");

            if (!names.add(attribute.name())) {
                throw new IllegalArgumentException(
                        "Duplicate vertex attributes " + attribute.name()
                );
            }

            elemnts.add(new Element(attribute, offset));
            offset = Math.addExact(offset, attribute.Size());
        }

        this.elements = List.copyOf(elemnts);
        this.stride = offset;
    }

    public List<Element> elements() {
        return elements;
    }

    public int stride() {
        return stride;
    }

    public Element find(String name) {
        Objects.requireNonNull(name, "name");

        for (Element element : elements) {
            if (element.attribute().name().equals(name)) {
                return element;
            }
        }

        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<VertexAttribute> attributes = new ArrayList<>();

        private Builder() { }

        public Builder add(String name, ComponentType type, int components) {
            return add(name, type, components, false);
        }

        public Builder add(
                String name,
                ComponentType type,
                int components,
                boolean normalized
        ) {
            attributes.add(
                    new VertexAttribute(name, type, components, normalized)
            );

            return this;
        }

        public Builder add (VertexAttribute attribute) {
            attributes.add(Objects.requireNonNull(attribute, "attribute"));
            return this;
        }

        public VertexLayout build() {
            return new VertexLayout(attributes);
        }
    }
}
