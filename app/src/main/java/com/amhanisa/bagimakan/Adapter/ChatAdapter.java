package com.amhanisa.bagimakan.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amhanisa.bagimakan.Model.Chat;
import com.amhanisa.bagimakan.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ChatAdapter extends FirestoreRecyclerAdapter<Chat, ChatAdapter.ChatViewHolder> {

    public static final int MSG_LEFT = 1;
    public static final int MSG_RIGHT = 2;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ChatAdapter(@NonNull FirestoreRecyclerOptions<Chat> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Chat model) {
        holder.chat.setText(model.getMessage());

        SimpleDateFormat format = new SimpleDateFormat("E, d HH:mm");
        holder.timestamp.setText(format.format(model.getTimestamp()));
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_LEFT){
             View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new ChatViewHolder(view);
        } else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new ChatViewHolder(view);
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        public TextView chat;
        public TextView timestamp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            chat = itemView.findViewById(R.id.textChat);
            timestamp = itemView.findViewById(R.id.timestampChat);
        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(getItem(position).getSender().equals(user.getUid())){
            return MSG_RIGHT;
        } else{
            return MSG_LEFT;
        }
    }
}
