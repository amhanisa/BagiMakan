package com.amhanisa.bagimakan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amhanisa.bagimakan.Adapter.ChatAdapter;
import com.amhanisa.bagimakan.Model.Chat;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class RoomChatActivity extends AppCompatActivity {

    private EditText inputChat;
    private Button btnSendChat;

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;

    private String ROOM_ID;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inputChat = findViewById(R.id.inputChat);
        btnSendChat = findViewById(R.id.btnSendChat);

        Intent getIntent = getIntent();

        if (getIntent.hasExtra("roomId")) {
            ROOM_ID = getIntent.getStringExtra("roomId");
            Log.e("CHAT", ROOM_ID);
        } else {
            finish();
        }

        db.collection("users").document(user.getUid())
                .collection("room")
                .document(ROOM_ID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ActionBar actionBar = getSupportActionBar();
                            actionBar.setTitle(documentSnapshot.getString("partnerName"));

                            Log.e("CHAT", documentSnapshot.getString("partnerName"));
                        } else {
                            Log.e("CHAT", "GAGAL dapat data");
                        }


                    }
                });

        setupRecyclerView();

        btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChat();
            }
        });
    }

    private void setupRecyclerView() {
        Query query = db.collection("room").document(ROOM_ID)
                .collection("chat").orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class)
                .build();

        chatAdapter = new ChatAdapter(options);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);
    }

    private void sendChat() {
        String chat = inputChat.getText().toString();
        if (chat.trim().isEmpty()) {
            return;
        }

        Chat sendChat = new Chat(user.getUid(), chat, new Date());

        inputChat.setText("");

        db.collection("room").document(ROOM_ID)
                .collection("chat")
                .add(sendChat).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.e("CHAT", "send success");
            }
        });

        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerViewChat.smoothScrollToPosition(positionStart);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        chatAdapter.stopListening();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
