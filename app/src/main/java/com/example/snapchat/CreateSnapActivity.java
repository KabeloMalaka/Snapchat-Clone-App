package com.example.snapchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class CreateSnapActivity extends AppCompatActivity {

    ImageView snapImageView;
    EditText messageEditText;
    String imageName = UUID.randomUUID().toString() + ".jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_snap);

        snapImageView = findViewById(R.id.snapImageView);
        messageEditText = findViewById(R.id.messageEditText);
    }

    public void getPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public void chooseImage(View view){
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            getPhoto();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getPhoto();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImage = data.getData();

        if (requestCode == 1 && resultCode == RESULT_OK && data != null){
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                snapImageView.setImageBitmap(bitmap);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void nextClicked(View view){
        // Get the data from an ImageView as bytes
        snapImageView.setDrawingCacheEnabled(true);
        snapImageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) snapImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

//        UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("Images").child(imageName).putBytes(data);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle unsuccessful uploads
//                Toast.makeText(CreateSnapActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                Uri uri = taskSnapshot.getUploadSessionUri();
//                Toast.makeText(CreateSnapActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
//
//                Intent intent = new Intent(getApplicationContext(),ChooseUserActivity.class);
//                intent.putExtra("imageURL", uri.toString());
//                intent.putExtra("imageName", imageName);
//                intent.putExtra("message", messageEditText.getText().toString());
//                startActivity(intent);
//            }
//        });
        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("Images").child(imageName);
        UploadTask uploadTask = ref.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    Intent intent = new Intent(getApplicationContext(),ChooseUserActivity.class);
                    intent.putExtra("imageURL", uri.toString());
                    intent.putExtra("imageName", imageName);
                    intent.putExtra("message", messageEditText.getText().toString());
                    startActivity(intent);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }
}