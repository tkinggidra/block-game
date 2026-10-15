
package io.github.tkinggidra.blockgame.engine.graphics.mesh;

public enum IndexType {
    UINT16(2),
    UINT32(4);

    private final int size;

    IndexType(int size) {
        this.size = size;
    }

    public int size() {
        return size;
    }
}
