package org.amito4.photobookreader;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity implements ScaleGestureDetector.OnScaleGestureListener {
    private String TAG = "PhotoBookViewer";
    private ImageView imageView;
    private ArrayList<Uri> imageUris;
    private int currentIndex;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector; // GestureDetector for tap events
    private float scale = 1f; // Initial scale factor
    private float lastTouchX;
    private float lastTouchY;
    private float dX, dY; // Offsets for dragging
    private SeekBar seekBar; // SeekBar for navigating images
    private TextView pageInfoTextView;

    private boolean isDragging;
    private boolean mScaleInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imageView = findViewById(R.id.imageView);
        seekBar = findViewById(R.id.seekBar);
        pageInfoTextView = findViewById(R.id.pageInfoTextView);

        // Get the image URIs and current index from the intent
        Intent intent = getIntent();
        imageUris = intent.getParcelableArrayListExtra("imageUris");
        currentIndex = intent.getIntExtra("currentIndex", 0);

        // Display the current image
        displayImage(imageUris.get(currentIndex));

        // Initialize ScaleGestureDetector
        scaleGestureDetector = new ScaleGestureDetector(this, this);
        // Initialize GestureDetector
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                // Handle single tap event
                Log.d(TAG, "Single tap detected");

                if (isDragging) {
                    // If dragging, do not navigate
                } else {
                    // Only check for swipes if not dragging
                    if (event.getX() < imageView.getWidth() / 2) {
                        showPreviousImage();
                    } else {
                        showNextImage();
                    }
                }

                return true;
            }
        });

        // Set up SeekBar
        seekBar.setMax(imageUris.size());
        seekBar.setProgress(currentIndex + 1); // +1 because progress is 1-based
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentIndex = progress - 1; // Update currentIndex based on SeekBar progress
                    displayImage(imageUris.get(currentIndex));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: Handle when the user starts dragging the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Optional: Handle when the user stops dragging the SeekBar
            }
        });
    }

    private Bitmap getBitmapFromContentUri(Context context, Uri uri) {
        InputStream inputStream = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            } else {
                return null;
            }
        } catch (Exception e) {
            // Handle exceptions, such as logging the error
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close(); // Remember to close the InputStream
                } catch (Exception e) {
                    e.printStackTrace(); // Handle the exception if closing fails
                }
            }
        }
    }

    private void displayImage(Uri imageUri) {
        // Load the image from the URI
        Bitmap bitmap = getBitmapFromContentUri(this, imageUri);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleX(scale);
        imageView.setScaleY(scale);
        updateSeekBar();
    }

    private void updateSeekBar() {
        seekBar.setProgress(currentIndex + 1); // +1 because progress is 1-based
        pageInfoTextView.setText((currentIndex + 1) + " / " + imageUris.size());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event); // Pass the touch event to the ScaleGestureDetector
        gestureDetector.onTouchEvent(event); // Pass the touch event to the GestureDetector

        if (mScaleInProgress) {
            // If scaling is in progress, skip page turning
            Log.d(TAG, "Pinch Event: Skip Page Turning");
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();
                dX = imageView.getTranslationX();
                dY = imageView.getTranslationY();
                break;

            case MotionEvent.ACTION_MOVE:
                float newX = dX + event.getRawX() - lastTouchX;
                float newY = dY + event.getRawY() - lastTouchY;
                imageView.setTranslationX(newX);
                imageView.setTranslationY(newY);
                isDragging = true; // Set dragging flag
                break;

            case MotionEvent.ACTION_UP:
                isDragging = false; // Reset dragging flag
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void showNextImage() {
        if (currentIndex < imageUris.size() - 1) {
            currentIndex++;
            displayImage(imageUris.get(currentIndex));
        } else {
            // Optionally, loop back to the first image
            currentIndex = 0;
            displayImage(imageUris.get(currentIndex));
        }
    }

    private void showPreviousImage() {
        if (currentIndex > 0) {
            currentIndex--;
            displayImage(imageUris.get(currentIndex));
        } else {
            // Optionally, loop back to the last image
            currentIndex = imageUris.size() - 1;
            displayImage(imageUris.get(currentIndex));
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scale *= detector.getScaleFactor(); // Update the scale factor
        scale = Math.max(0.1f, Math.min(scale, 5.0f)); // Limit the scale factor
        imageView.setScaleX(scale);
        imageView.setScaleY(scale);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mScaleInProgress = true;
        return true; // Return true to indicate that we want to handle the scaling
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mScaleInProgress = false;
        // You can add any additional logic here if needed when scaling ends
    }
} 