public class Vec3 {

    public float x;
    public float y;
    public float z;

    Vec3() {
        x = 0;
        y = 0;
        z = 0;
    }

    Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "Vec3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
