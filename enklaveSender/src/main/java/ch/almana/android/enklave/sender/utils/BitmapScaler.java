package ch.almana.android.enklave.sender.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Created by vogtp on 7/13/14.
 *
 */
public class BitmapScaler extends AsyncTask<ImageView, Void, Bitmap> {

    private static final int MAX_IMAGE_SIZE = 800;

    private ImageView imageView;
    private boolean hasNewSize = Logger.DEBUG;

    @Override
    protected Bitmap doInBackground(ImageView... params) {
        imageView = params[0];
        final BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable == null){
            return null;
        }
        return scaleImage(drawable.getBitmap());
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (hasNewSize && bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    private Bitmap scaleImage(Bitmap bitmap) {
        // Raw height and width of image
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        int inSampleSize = 1;

        int reqHeight = MAX_IMAGE_SIZE;//imageView.getHeight();
        int reqWidth =  MAX_IMAGE_SIZE;//imageView.getWidth();
            Logger.w("Got image  " + width + "/" + height + " should be " + reqHeight + "/" + reqWidth);
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            hasNewSize = true;
            Logger.w("Warn scaling image from " + width + "/" + height + " to" + halfWidth + "/" + halfHeight);
            return Bitmap.createScaledBitmap(bitmap, halfWidth, halfHeight, false);
        }
        return bitmap;
    }

}