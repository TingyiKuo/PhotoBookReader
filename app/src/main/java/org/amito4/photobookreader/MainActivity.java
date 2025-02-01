package org.amito4.photobookreader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.Arrays;
import android.content.Context;
import androidx.documentfile.provider.DocumentFile;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FOLDER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickFolder();
    }

    private void pickFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_FOLDER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FOLDER_REQUEST && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            
            // Grant permission to access the URI
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Load images from the selected folder
                loadImagesFromFolder(uri);
            }
        }
    }

    private void loadImagesFromFolder(Uri uri) {
        DocumentFile directory = DocumentFile.fromTreeUri(this, uri);
        
        if (directory != null && directory.isDirectory()) {
            List<Uri> imageUris = new ArrayList<>();
            
            for (DocumentFile file : directory.listFiles()) {
                if (file.isFile() && file.getType() != null && file.getType().equals("image/png")) {
                    imageUris.add(file.getUri());
                }
            }
            
            if (!imageUris.isEmpty()) {
                openImage(imageUris, 0); // Open the first image
            } else {
                Log.e("MainActivity", "No PNG images found in the selected folder.");
            }
        } else {
            Log.e("MainActivity", "The selected URI is not a valid directory.");
        }
    }

    private void openImage(List<Uri> imageUris, int index) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putParcelableArrayListExtra("imageUris", new ArrayList<>(imageUris));
        intent.putExtra("currentIndex", index);
        startActivity(intent);
    }
} 