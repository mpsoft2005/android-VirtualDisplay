package com.ben.testvirtualdisplay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.util.Random;

public class MainActivity extends Activity implements ImageReader.OnImageAvailableListener {

    final String TAG = "MainActivity";

    Random mRandom = new Random();

    DisplayManager mDisplayManager;
    DisplayMetrics mDisplayRealMetrics;

    int mVirtualDispalyWidth = 512;
    int mVirtualDisplayHeight = 512;
    VirtualDisplay mVirtualDisplay;

    ImageReader mImageReader;
    MyPresentation mMyPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void setupImageReader() {
        mImageReader = ImageReader.newInstance(mVirtualDispalyWidth, mVirtualDisplayHeight, PixelFormat.RGBA_8888, 4);
        mImageReader.setOnImageAvailableListener(this, new Handler(getMainLooper()));
    }

    private void setupVirtualDisplay() {

        // Virtual Display
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        mDisplayRealMetrics = new DisplayMetrics();

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(mDisplayRealMetrics);

        mVirtualDisplay = mDisplayManager.createVirtualDisplay("Test Virtual Display",
                mVirtualDispalyWidth, mVirtualDisplayHeight, mDisplayRealMetrics.densityDpi,
                mImageReader.getSurface(), DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                null, null
        );

        Log.w(TAG, String.format("create virtual display %dx%d (%d)", mVirtualDispalyWidth, mVirtualDisplayHeight, mDisplayRealMetrics.densityDpi));

        if (mVirtualDisplay != null) {
            Log.i(TAG, "create virtual display success");

            if (mMyPresentation != null) {
                mMyPresentation.dismiss();
            }

            mMyPresentation = new MyPresentation(this, mVirtualDisplay.getDisplay());
            mMyPresentation.show();
        } else {
            Log.i(TAG, "create virtual display fail");
        }
    }

    public Bitmap acquireNextImage(ImageReader imageReader) {
        long captureBeginTime = System.currentTimeMillis();
        Image image = imageReader.acquireNextImage();

        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane plane0 = image.getPlanes()[0];
        final ByteBuffer buffer = plane0.getBuffer();
        int pixelStride = plane0.getPixelStride();
        int rowStride = plane0.getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        image.close();
        return bitmap;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.i(TAG, "onImageAvailable");

        Bitmap bitmap = acquireNextImage(reader);

        ImageView imageView = findViewById(R.id.image_view);
        imageView.setImageBitmap(bitmap);
    }

    void onClickButton(View v) {
        Log.i(TAG, "onClickButton");

        Button button = (Button)v;
        button.setText(String.format("Random %d", mRandom.nextInt(512)));

        setupImageReader();
        setupVirtualDisplay();
    }
}
