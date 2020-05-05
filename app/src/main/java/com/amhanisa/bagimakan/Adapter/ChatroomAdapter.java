package com.amhanisa.bagimakan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amhanisa.bagimakan.Model.Chatroom;
import com.amhanisa.bagimakan.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatroomAdapter extends FirestoreRecyclerAdapter<Chatroom, ChatroomAdapter.ChatroomViewHolder> {

    onChatroomClickListener listener;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ChatroomAdapter(@NonNull FirestoreRecyclerOptions<Chatroom> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomViewHolder holder, int position, @NonNull Chatroom model) {
        holder.partnerName.setText(model.getPartnerName());
    }

    @NonNull
    @Override
    public ChatroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
        return new ChatroomViewHolder(view);
    }

    public class ChatroomViewHolder extends RecyclerView.ViewHolder{

        public TextView partnerName;

        public ChatroomViewHolder(@NonNull View itemView) {
            super(itemView);

            partnerName = itemView.findViewById(R.id.txtChatroomName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onChatrooomClicked(getSnapshots().getSnapshot(position).getId());
                    }
                }
            });
        }
    }

    public interface onChatroomClickListener{
        void onChatrooomClicked(String roomId);
    }

    public void setOnChatroomClicked(onChatroomClickListener listener){
        this.listener = listener;
    }
}
