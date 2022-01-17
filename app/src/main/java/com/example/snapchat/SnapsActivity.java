package com.example.snapchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Snapshot;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SnapsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ArrayList<DataSnapshot> snaps = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.createSnap){
            Intent intent = new Intent(getApplicationContext(), CreateSnapActivity.class);
            startActivity(intent);

        }else if (item.getItemId() == R.id.logout){
            finish();
            mAuth.signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snaps);

        mAuth = FirebaseAuth.getInstance();
        ListView snapsListView = findViewById(R.id.snapsListView);
        final ArrayList snapsArrayList = new ArrayList();
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, snapsArrayList);
        snapsListView.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("snaps").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                snapsArrayList.add(snapshot.child("from").getValue().toString());
                snaps.add(snapshot);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int indexSnap = 0;
                for (DataSnapshot dataSnapshot : snaps){
                    if (dataSnapshot.getKey().equals(snapshot.getKey())){
                        snapsArrayList.remove(indexSnap);
                        snaps.remove(indexSnap);
                    }
                    indexSnap++;
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        snapsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ViewSnapActivity.class);
                intent.putExtra("imageName", snaps.get(position).child("imageName").getValue().toString());
                intent.putExtra("imageURL", snaps.get(position).child("imageURL").getValue().toString());
                intent.putExtra("message", snaps.get(position).child("message").getValue().toString());
                intent.putExtra("snapKey", snaps.get(position).getKey());

                startActivity(intent);
            }
        });
    }
}