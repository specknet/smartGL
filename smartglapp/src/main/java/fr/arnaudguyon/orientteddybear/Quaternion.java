package fr.arnaudguyon.orientteddybear;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;

public class Quaternion {

    public double w;
    public double x;
    public double y;
    public double z;

    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static double norm(Quaternion q) {
        return Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w);
    }

    public static Quaternion conjugate(Quaternion q) {
        return new Quaternion(q.w, -q.x, -q.y, -q.z);
    }

    public static Quaternion inverse(Quaternion q) {
        return Quaternion.sMultiplication(Quaternion.conjugate(q), 1 / Math.pow(Quaternion.norm(q), 2));
    }

    public static Quaternion qMultiplication(Quaternion q1, Quaternion q2) {
        double w = -q1.x * q2.x - q1.y * q2.y - q1.z * q2.z + q1.w * q2.w;
        double x = q1.x * q2.w + q1.y * q2.z - q1.z * q2.y + q1.w * q2.x;
        double y = -q1.x * q2.z + q1.y * q2.w + q1.z * q2.x + q1.w * q2.y;
        double z = q1.x * q2.y - q1.y * q2.x + q1.z * q2.w + q1.w * q2.z;

        return new Quaternion(w, x, y, z);
    }

    public static Quaternion sMultiplication(Quaternion q, double d) {
        double w = q.w * d;
        double x = q.x * d;
        double y = q.y * d;
        double z = q.z * d;

        return new Quaternion(w, x, y, z);
    }

    public static double[] quaternionToEulerAngles(Quaternion q) {
        double eulerAngles[] = new double[3]; // heading, attitude, bank
        double sqw = Math.pow(q.w, 2);
        double sqx = Math.pow(q.x, 2);
        double sqy = Math.pow(q.y, 2);
        double sqz = Math.pow(q.z, 2);
        double unit = sqx + sqy + sqz + sqw;
        double test = q.x * q.y + q.z * q.w;
        if (test > 0.499 * unit) {
            eulerAngles[0] = 2 * atan2(q.x, q.w);
            eulerAngles[1] = Math.PI / 2;
            eulerAngles[2] = 0;
        } else if (test < -0.499 * unit) {
            eulerAngles[0] = -2 * atan2(q.x, q.w);
            eulerAngles[1] = -Math.PI / 2;
            eulerAngles[2] = 0;
        } else {
            eulerAngles[0] = atan2(2 * q.y * q.w - 2 * q.x * q.z, sqx - sqy - sqz + sqw);
            eulerAngles[1] = asin(2 * test / unit);
            eulerAngles[2] = atan2(2 * q.x * q.w - 2 * q.y * q.z, -sqx + sqy - sqz + sqw);
        }

        return eulerAngles;
    }
}
