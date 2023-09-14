package com.laxmi.imageeditor;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Bitmap> imageList;
    private ImageListAdapter adapter;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private int PICK_IMAGE_REQUEST_CODE=300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button openImageBtn = findViewById(R.id.imgopenbtn);

        recyclerView = findViewById(R.id.listView);
        imageList = new ArrayList<>(); // Your list of Bitmaps
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        getLastWork(); // Initialize and set the adapter

        openImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    openImagePicker();
                } else {
                    requestPermission();
                }
            }
        });

        // Initialize the ActivityResultLauncher for permission requests
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> permissions) {
                        boolean allPermissionsGranted = true;
                        for (Boolean granted : permissions.values()) {
                            if (!granted) {
                                allPermissionsGranted = false;
                                break;
                            }
                        }
                        if (allPermissionsGranted) {
                            // All permissions granted, launch the result activity
                            openImagePicker();
                        } else {
                            // Permission(s) denied, show a message or take appropriate action
                            Toast.makeText(MainActivity.this, "Permission(s) required for this app.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) {
         return true;
        }else{
            String[] permissionsToCheck = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            for (String permission : permissionsToCheck) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }


        return true; // Permissions granted on older Android versions
    }


    // Function to request permissions
    private void requestPermission() {
        String[] permissionsToRequest = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
            };
        }else{
            permissionsToRequest = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
        }
        permissionLauncher.launch(permissionsToRequest);
    }

//    private void getLastWork() {
//
//        File file;
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            // For Android 11 and above
//            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), getString(R.string.app_folder_name));
//        } else {
//            // For Android 10 and below
//            file = new File(Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_folder_name));
//        }
//
//        File[] files = file.listFiles();
//        if (files != null) {
//            int i=0;
//            for (File file1 : files) {
//                if (file1.getPath().endsWith(".png") || file1.getPath().endsWith(".jpg")) {
//                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//                    Bitmap bitmap_work = BitmapFactory.decodeFile(file1.getAbsolutePath(), bmOptions);
//                    imageList.add(bitmap_work);
//                }
//
//            }
//            if(imageList.size()>0){
//                // Check and request permissions if needed here
//                adapter = new ImageListAdapter(MainActivity.this, imageList);
//                recyclerView.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//
//            }
//
//            if (files.length == 0) {
////                tvEmpty.setVisibility(View.VISIBLE);
//            } else {
////                tvEmpty.setVisibility(View.GONE);
//            }
//        }
//    }
    private void getLastWork() {
        File file;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, use MediaStore
            String selection = MediaStore.Images.Media.RELATIVE_PATH + " like ?";
            String[] selectionArgs = new String[]{Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_folder_name)};
            Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            file = new File(externalContentUri.toString());
            String[] projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME
            };

            try {
                Cursor cursor = getContentResolver().query(externalContentUri, projection, selection, selectionArgs, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(contentUri));
                        imageList.add(bitmap);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // For Android 10 and below
            file = new File(Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_folder_name));
            // Rest of your code for Android 10 and below remains the same
            // ...
            File[] files = file.listFiles();
            if (files != null) {
                int i=0;
                for (File file1 : files) {
                    if (file1.getPath().endsWith(".png") || file1.getPath().endsWith(".jpg")) {
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap_work = BitmapFactory.decodeFile(file1.getAbsolutePath(), bmOptions);
                        imageList.add(bitmap_work);
                    }

                }
                if(imageList.size()>0){
                    // Check and request permissions if needed here
                    adapter = new ImageListAdapter(MainActivity.this, imageList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                }

                if (files.length == 0) {
//                tvEmpty.setVisibility(View.VISIBLE);
                } else {
//                tvEmpty.setVisibility(View.GONE);
                }
            }
        }

        if (imageList.size() > 0) {
            adapter = new ImageListAdapter(MainActivity.this, imageList);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }


    public void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            // Pass the selected image URI to the editing activity.
            Intent editIntent = new Intent(this, EditImageActivity.class);
            editIntent.setData(selectedImageUri);
            startActivity(editIntent);
        }
    }

}
