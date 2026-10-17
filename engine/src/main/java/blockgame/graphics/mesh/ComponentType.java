package blockgame.graphics.mesh;

public enum ComponentType {
    FLOAT32(4),
    UINT8(1),
    UINT16(2),
    UINT32(4),
    INT8(1),
    INT16(2),
    INT32(4);

    private final int size;

    ComponentType(int size) {
        this.size = size;
    }

    public int size() {
        return size;
    }
}