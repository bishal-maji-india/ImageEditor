package com.laxmi.imageeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;

// EditImageActivity.java

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditImageActivity extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap originalBitmap;
    private Bitmap flippedBitmap;
    private boolean isFlippedVertical = false; // Flag to track vertical flip state
    private boolean isFlippedHorizontal = false; // Flag to track horizontal flip state

    private Button save,flip_left,flip_right;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);

        imageView = findViewById(R.id.imageEdit);

        save=findViewById(R.id.save);
        flip_left=findViewById(R.id.flip_left);
        flip_right=findViewById(R.id.flip_right);
        flip_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             flipVertical();
            }
        });
        flip_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            flipHorizontal();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToGallery();
            }
        });
        Uri selectedImageUri = getIntent().getData();
        if (selectedImageUri != null) {
            originalBitmap = loadImageFromUri(selectedImageUri);
            imageView.setImageBitmap(originalBitmap);
        }
    }

    public void flipVertical() {
        if (originalBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.preScale(1.0f, -1.0f); // Flip vertically

            flippedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(flippedBitmap);

            isFlippedVertical = !isFlippedVertical;
        }
    }

    public void flipHorizontal() {
        if (originalBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.preScale(-1.0f, 1.0f); // Flip horizontally

            flippedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(flippedBitmap);

            isFlippedHorizontal = !isFlippedHorizontal;
        }
    }
    private void saveToGallery() {
        // Get the current Bitmap from the ImageView
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap editedBitmap = drawable.getBitmap();

        String parentPath = "";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            parentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + getString(R.string.app_folder_name);
        } else {
            // For Android 10 and below
            parentPath = Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_folder_name);
        }

        File parentFile = new File(parentPath);
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        File imageFile = new File(parentFile, "Edited" + System.currentTimeMillis() + ".png");

        try {
      FileOutputStream outputStream = new FileOutputStream(imageFile);
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(this, "Image saved to " + imageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception and show an error message to the user
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }




    private Bitmap loadImageFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
