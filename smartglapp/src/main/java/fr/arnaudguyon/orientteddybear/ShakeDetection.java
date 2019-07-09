package fr.arnaudguyon.orientteddybear;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class ShakeDetection extends AsyncTask<RingBuffer, Integer, Boolean> {

    private WeakReference<Context> mContext;
    private static final double SUM_ABS_CHANGES_THRESHOLD = 150.0;
    private static final double MEDIAN_OF_MAX_THRESHOLD = 500.0;
    private static final double MAGNITUDE_THRESHOLD = 650.0



            ;

    public ShakeDetection(Context mContext) {
        this.mContext = new WeakReference<>(mContext);
    }

    @Override
    protected Boolean doInBackground(RingBuffer... buffer) {
//        Context context = mContext.get();
//        MediaPlayer mp = MediaPlayer.create(context, R.raw.sheep);
//        mp.start();
//        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            public void onCompletion(MediaPlayer mp) {
//                mp.release();
//
//            };
//        });

        RingBuffer data = buffer[0];

        data = normalise(data);

        if (sumOfAbsoluteChanges(data) >= SUM_ABS_CHANGES_THRESHOLD) {
            Context context = mContext.get();
            MediaPlayer mp = MediaPlayer.create(context, R.raw.toy_squeaks);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }

                ;
            });
        }
        return false;
    }

    protected void onPostExecute(boolean result) {
//        if (result) {
//            Context context = mContext.get();
//            MediaPlayer mp = MediaPlayer.create(context, R.raw.sheep);
//            mp.start();
//            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                public void onCompletion(MediaPlayer mp) {
//                    mp.release();
//                };
//            });
//        }
    }

    public static double sumOfAbsoluteChanges(RingBuffer buffer) {
        double[] out = new double[buffer.elements.length - 1];
        double sum = 0.0;
        for (int i = 0; i < buffer.elements.length - 1; i++) {
            out[i] = Math.abs(buffer.elements[i + 1].getAccel_y() - buffer.elements[i].getAccel_y());
        }

        for (double element :
                out) {
            sum += element;
        }

        return sum;
    }

    public static double magnitude(RingBuffer buffer) {
        double sum = 0.0;

        for (int i = 0; i < buffer.elements.length; i++) {
//            sum += Math.sqrt(Math.pow(buffer.elements[i].getAccel_x(), 2) + Math.pow(buffer.elements[i].getAccel_y(), 2) +
//                    Math.pow(buffer.elements[i].getAccel_z(), 2));
            sum += Math.abs(buffer.elements[i].getAccel_y());
        }

        return sum;
    }

    public static double medianOfMax(RingBuffer buffer) {
        double[] max = new double[buffer.elements.length];
        double median = 0.0;

        for (int i = 0; i < buffer.elements.length; i++) {
            max[i] = Math.max(buffer.elements[i].getGyro_x(), Math.max(buffer.elements[i].getGyro_y(), buffer.elements[i].getGyro_z()));
        }

        Arrays.sort(max);

        if (max.length % 2 == 0) {
            median = (max[max.length / 2] + max[max.length / 2 - 1]) / 2;
        } else {
            median = (max[max.length / 2]);
        }

        return median;
    }

    public static RawData findMean(RingBuffer buffer) {
        RawData means = new RawData(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        for (int i = 0; i < buffer.elements.length; i++) {
            means.setAccel_x(means.getAccel_x() + buffer.elements[i].getAccel_x());
            means.setAccel_y(means.getAccel_y() + buffer.elements[i].getAccel_y());
            means.setAccel_z(means.getAccel_z() + buffer.elements[i].getAccel_z());
            means.setGyro_x(means.getGyro_x() + buffer.elements[i].getGyro_x());
            means.setGyro_y(means.getGyro_y() + buffer.elements[i].getGyro_y());
            means.setGyro_z(means.getGyro_z() + buffer.elements[i].getGyro_z());
        }

        means.setAccel_x(means.getAccel_x() / buffer.elements.length);
        means.setAccel_y(means.getAccel_y() / buffer.elements.length);
        means.setAccel_z(means.getAccel_z() / buffer.elements.length);
        means.setGyro_x(means.getGyro_x() / buffer.elements.length);
        means.setGyro_y(means.getGyro_y() / buffer.elements.length);
        means.setGyro_z(means.getGyro_z() / buffer.elements.length);

        return means;
    }

    public static RingBuffer normalise(RingBuffer buffer) {
        RawData means = findMean(buffer);
        RingBuffer data = buffer;

        for (int i = 0; i < buffer.elements.length; i++) {
            data.elements[i].setAccel_x(buffer.elements[i].getAccel_x() / means.getAccel_x());
            data.elements[i].setAccel_y(buffer.elements[i].getAccel_y() / means.getAccel_y());
            data.elements[i].setAccel_z(buffer.elements[i].getAccel_z() / means.getAccel_z());
            data.elements[i].setGyro_x(buffer.elements[i].getGyro_x() / means.getGyro_x());
            data.elements[i].setGyro_y(buffer.elements[i].getGyro_y() / means.getGyro_y());
            data.elements[i].setGyro_z(buffer.elements[i].getGyro_z() / means.getGyro_z());
        }

        return data;
    }
}
