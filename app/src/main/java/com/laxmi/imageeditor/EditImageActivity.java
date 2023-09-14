package com.laxmi.imageeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
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
import android.provider.MediaStore;
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
import java.io.OutputStream;

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
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap editedBitmap = drawable.getBitmap();

        String relativeLocation = Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_folder_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Edited" + System.currentTimeMillis() + ".jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, relativeLocation);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri imageUri = getContentResolver().insert(externalContentUri, values);

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            if (outputStream != null) {
                outputStream.close();
            }
            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
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
