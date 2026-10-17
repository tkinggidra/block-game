
package blockgame.graphics.camera;

import blockgame.math.Transform;
import org.joml.Matrix4f;

import java.util.Objects;

public final class Camera {

    public enum Projection {
        PERSPECTIVE,
        ORTHOGRAPHIC
    }

    private final Transform transform = new Transform();

    private Projection projection = Projection.PERSPECTIVE;

    private float fieldOfView = (float) Math.toRadians(70.0);
    private float orthographicSize = 10.0f;
    private float nearPlane = 0.1f;
    private float farPlane = 1000.0f;

    public Transform transform() {
        return transform;
    }

    public Camera setProjection(Projection projection) {
        this.projection = Objects.requireNonNull(projection);
        return this;
    }

    public Camera setPerspective(
            float fieldOfView,
            float nearPlane,
            float farPlane
    ) {
        if (!Float.isFinite(fieldOfView)
                || fieldOfView <= 0.0f
                || fieldOfView >= Math.PI
                || !Float.isFinite(nearPlane)
                || !Float.isFinite(farPlane)
                || nearPlane <= 0.0f
                || farPlane <= nearPlane) {
            throw new IllegalArgumentException("Invalid perspective parameters");
        }

        this.projection = Projection.PERSPECTIVE;
        this.fieldOfView = fieldOfView;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;

        return this;
    }

    public Camera setOrthographic(
            float verticalSize,
            float nearPlane,
            float farPlane
    ) {
        if (!Float.isFinite(verticalSize)
                || verticalSize <= 0.0f
                || !Float.isFinite(nearPlane)
                || !Float.isFinite(farPlane)
                || farPlane <= nearPlane) {
            throw new IllegalArgumentException("Invalid orthographic parameters");
        }

        this.projection = Projection.ORTHOGRAPHIC;
        this.orthographicSize = verticalSize;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;

        return this;
    }

    public Matrix4f viewMatrix() {
        return transform.matrix().invert();
    }

    public Matrix4f projectionMatrix(float aspectRatio) {
        if (!Float.isFinite(aspectRatio) || aspectRatio <= 0.0f) {
            throw new IllegalArgumentException("Invalid aspect ratio");
        }

        if (projection == Projection.PERSPECTIVE) {
            return new Matrix4f().perspective(
                    fieldOfView,
                    aspectRatio,
                    nearPlane,
                    farPlane
            );
        }

        float halfHeight = orthographicSize * 0.5f;
        float halfWidth = halfHeight * aspectRatio;

        return new Matrix4f().ortho(
                -halfWidth,
                halfWidth,
                -halfHeight,
                halfHeight,
                nearPlane,
                farPlane
        );
    }
}
