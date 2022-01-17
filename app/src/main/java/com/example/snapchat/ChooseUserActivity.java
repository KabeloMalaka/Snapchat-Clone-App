package com.example.snapchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChooseUserActivity extends AppCompatActivity {

    ListView chooseUserListView;
    ArrayList<String> uidArrayList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_user);

        chooseUserListView = findViewById(R.id.chooseUserListView);
        final ArrayList<String> usersArrayList = new ArrayList<String>();
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,usersArrayList);
        chooseUserListView.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference().child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String email = snapshot.child("email").getValue().toString();
                usersArrayList.add(email);
                uidArrayList.add(snapshot.getKey());
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        chooseUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent snapInfoIntent = getIntent();
                Map<String, String> snapMap = new HashMap<>();
                snapMap.put("from", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                snapMap.put("imageName", snapInfoIntent.getStringExtra("imageName"));
                snapMap.put("imageURL", snapInfoIntent.getStringExtra("imageURL"));
                snapMap.put("message", snapInfoIntent.getStringExtra("message"));

                FirebaseDatabase.getInstance().getReference().child("users").child(uidArrayList.get(position)).child("snaps").push().setValue(snapMap);

                Intent intent = new Intent(getApplicationContext(), SnapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
