package com.example.bagimakan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bagimakan.Model.Request;
import com.example.bagimakan.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class RequestAdapter extends FirestoreRecyclerAdapter<Request, RequestAdapter.RequestViewHolder> {

    onRequestClickListener listener;

    public RequestAdapter(@NonNull FirestoreRecyclerOptions<Request> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Request model) {
        holder.txtUsername.setText(model.getUserName());
        holder.txtJumlah.setText(model.getJumlah().toString());
        holder.txtStatus.setText(model.getStatus());
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
        return new RequestViewHolder(view);
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        public TextView txtUsername;
        public TextView txtStatus;
        public TextView txtJumlah;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUsername = itemView.findViewById(R.id.txtRequestUserName);
            txtStatus = itemView.findViewById(R.id.txtRequestStatus);
            txtJumlah = itemView.findViewById(R.id.txtRequestJumlah);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onRequestClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface onRequestClickListener{
        void onRequestClicked(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnRequestClicked(onRequestClickListener listener){
        this.listener = listener;
    }

}
