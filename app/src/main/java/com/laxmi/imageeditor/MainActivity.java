package com.laxmi.imageeditor;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
        Button openImageBtn=findViewById(R.id.imgopenbtn);


        recyclerView = findViewById(R.id.listView);
        imageList = new ArrayList<>(); // Your list of Bitmaps
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        getLastWork();
        // Initialize and set the adapter


        openImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()){
                    openImagePicker();
                }else{
                    requestPermission();
                }
            }
        });
        // Initialize the ActivityResultLauncher for permission requests
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
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
                        Toast.makeText(this, "Permission(s) required for this app.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private boolean checkPermission() {
        // Check for READ_EXTERNAL_STORAGE permission
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        // Check for WRITE_EXTERNAL_STORAGE permission
        int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Return true if both permissions are granted; otherwise, return false.
        return readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED;
    }
    // Function to request permissions and launch the result activity
    public void requestPermission() {
        String[] permissionsToRequest = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        permissionLauncher.launch(permissionsToRequest);
    }
    private void getLastWork() {

        File file;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), getString(R.string.app_folder_name));
        } else {
            // For Android 10 and below
            file = new File(Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_folder_name));
        }

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
