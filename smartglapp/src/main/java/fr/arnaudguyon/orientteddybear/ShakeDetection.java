package fr.arnaudguyon.orientteddybear;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class ShakeDetection extends AsyncTask<RingBuffer, Integer, Boolean> {

    private WeakReference<Context> mContext;
    private static final double SUM_ABS_CHANGES_THRESHOLD = 600.0;
    private static final double MEDIAN_OF_MAX_THRESHOLD = 500.0;

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

        if (sumOfAbsoluteChanges(buffer[0]) >= SUM_ABS_CHANGES_THRESHOLD) {
            Context context = mContext.get();
            MediaPlayer mp = MediaPlayer.create(context, R.raw.sheep);
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
            out[i] = Math.abs(buffer.elements[i + 1].getGyro_x() - buffer.elements[i].getGyro_x());
        }

        for (double element :
                out) {
            sum += element;
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
}
