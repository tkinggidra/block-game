package blockgame.math;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

public final class Transform {
    private final Vector3f position = new Vector3f();
    private final Quaternionf rotation = new Quaternionf().identity();
    private final Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);

    public Transform() { }

    public Transform setPosition(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    public Transform setRotation(Quaternionf rotation) {
        this.rotation.set(Objects.requireNonNull(rotation));
        return this;
    }

    public Transform setScale(float x, float y, float z) {
        scale.set(x, y, z);
        return this;
    }

    public Vector3f position() {
        return new Vector3f(position);
    }

    public Quaternionf rotation() {
        return new Quaternionf(rotation);
    }

    public Vector3f scale() {
        return new Vector3f(scale);
    }

    public Matrix4f matrix() {
        return new Matrix4f()
                .translationRotateScale(position, rotation, scale);
    }
}
