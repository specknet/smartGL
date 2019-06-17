package fr.arnaudguyon.orientteddybear;

public class RawData {

    double accel_x;
    double accel_y;
    double accel_z;

    double gyro_x;
    double gyro_y;
    double gyro_z;

    public RawData(double accel_x, double accel_y, double accel_z, double gyro_x, double gyro_y, double gyro_z) {
        this.accel_x = accel_x;
        this.accel_y = accel_y;
        this.accel_z = accel_z;
        this.gyro_x = gyro_x;
        this.gyro_y = gyro_y;
        this.gyro_z = gyro_z;
    }

    public double getAccel_x() {
        return accel_x;
    }

    public double getAccel_y() {
        return accel_y;
    }

    public double getAccel_z() {
        return accel_z;
    }

    public double getGyro_x() {
        return gyro_x;
    }

    public double getGyro_y() {
        return gyro_y;
    }

    public double getGyro_z() {
        return gyro_z;
    }
}
