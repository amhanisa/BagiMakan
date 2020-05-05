package com.amhanisa.bagimakan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.amhanisa.bagimakan.Adapter.ChatroomAdapter;
import com.amhanisa.bagimakan.Model.Chatroom;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class LobbyChatActivity extends AppCompatActivity {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerView;
    private ChatroomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_chat);

        setupRecyclerView();
    }

    private void setupRecyclerView(){
        Query query = db.collection("users").document(user.getUid())
                .collection("room").orderBy("partnerName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Chatroom> options = new FirestoreRecyclerOptions.Builder<Chatroom>()
                .setQuery(query, Chatroom.class)
                .build();

        adapter = new ChatroomAdapter(options);

        recyclerView = findViewById(R.id.recyclerViewChatroom);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnChatroomClicked(new ChatroomAdapter.onChatroomClickListener() {
            @Override
            public void onChatrooomClicked(String roomId) {
                Intent chatroom = new Intent(LobbyChatActivity.this, RoomChatActivity.class);
                chatroom.putExtra("roomId", roomId);
                startActivity(chatroom);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
