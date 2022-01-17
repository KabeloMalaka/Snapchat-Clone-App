package com.example.snapchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ViewSnapActivity extends AppCompatActivity {

    ImageView viewImageView;
    TextView viewMessageTextView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_snap);

        viewImageView = findViewById(R.id.viewImageView);
        viewMessageTextView = findViewById(R.id.viewMessageTextView);

        intent = getIntent();

        viewMessageTextView.setText(intent.getStringExtra("message"));

        ImageDownloader downloadImage = new ImageDownloader();
        Bitmap bitmap;
        try{
            bitmap = downloadImage.execute(intent.getStringExtra("imageURL")).get();
            viewImageView.setImageBitmap(bitmap);
            Toast.makeText(this, "Image downloaded", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Image not found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("snaps").child(intent.getStringExtra("snapKey")).removeValue();
        FirebaseStorage.getInstance().getReference().child("images").child(intent.getStringExtra("imageName")).delete();
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);

                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }
    }
}