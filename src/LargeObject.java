import java.util.Random;

public class LargeObject {

    private static Random r = new Random(23423423L);

    boolean x, y, z, w;
    int i, j, k;
    float a, b, c, d, e;
    double g, h;
    long m, n, o, p;

    // total 4 + 3*4 + 5*4 + 2*8 + 4*8 = 84 bytes

    LargeObject() {

    }

    void init() {
        x = r.nextBoolean();
        y = r.nextBoolean();
        z = r.nextBoolean();
        w = r.nextBoolean();

        i = r.nextInt();
        j = r.nextInt();
        k = r.nextInt();

        a = r.nextFloat();
        b = r.nextFloat();
        c = r.nextFloat();
        d = r.nextFloat();
        e = r.nextFloat();

        g = r.nextDouble();
        h = r.nextDouble();

        m = r.nextLong();
        n = r.nextLong();
        o = r.nextLong();
        p = r.nextLong();
    }


    @Override
    public String toString() {
        return "LargeObject{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                ", i=" + i +
                ", j=" + j +
                ", k=" + k +
                ", a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                ", e=" + e +
                ", g=" + g +
                ", h=" + h +
                ", m=" + m +
                ", n=" + n +
                ", o=" + o +
                ", p=" + p +
                '}';
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (x ? 1 : 0);
        result = 31 * result + (y ? 1 : 0);
        result = 31 * result + (z ? 1 : 0);
        result = 31 * result + (w ? 1 : 0);
        result = 31 * result + i;
        result = 31 * result + j;
        result = 31 * result + k;
        result = 31 * result + (a != +0.0f ? Float.floatToIntBits(a) : 0);
        result = 31 * result + (b != +0.0f ? Float.floatToIntBits(b) : 0);
        result = 31 * result + (c != +0.0f ? Float.floatToIntBits(c) : 0);
        result = 31 * result + (d != +0.0f ? Float.floatToIntBits(d) : 0);
        result = 31 * result + (e != +0.0f ? Float.floatToIntBits(e) : 0);
        temp = Double.doubleToLongBits(g);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(h);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (m ^ (m >>> 32));
        result = 31 * result + (int) (n ^ (n >>> 32));
        result = 31 * result + (int) (o ^ (o >>> 32));
        result = 31 * result + (int) (p ^ (p >>> 32));
        return result;
    }
}
