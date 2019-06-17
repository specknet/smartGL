package fr.arnaudguyon.orientteddybear;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class ShakeDetection extends AsyncTask<RingBuffer, Integer, Boolean> {

    private WeakReference<Context> mContext;
    private static final double SUM_ABS_CHANGES_THRESHOLD = 1000.0;

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
}
