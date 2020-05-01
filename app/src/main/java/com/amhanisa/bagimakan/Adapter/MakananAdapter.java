package com.amhanisa.bagimakan.Adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amhanisa.bagimakan.Model.Makanan;
import com.amhanisa.bagimakan.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class MakananAdapter extends FirestoreRecyclerAdapter<Makanan, MakananAdapter.MakananViewHolder> {

    private onItemClickListener listener;

    public MakananAdapter(@NonNull FirestoreRecyclerOptions<Makanan> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MakananViewHolder holder, int position, @NonNull Makanan model) {
        holder.namaMakanan.setText(model.getName());
        holder.userName.setText("Pemilik : " + model.getUserName());
        holder.lokasiMakanan.setText(model.getLokasi());
        holder.jumlahMakanan.setText("Jumlah : " + model.getJumlah().toString());
        long timeInMillis = model.getDate().getTime();
        holder.tanggalMakanan.setText(DateUtils.getRelativeTimeSpanString(timeInMillis));
        Picasso.get().load(model.getImageUrl()).placeholder(R.drawable.ic_image_black_24dp).fit().centerCrop().into(holder.imageMakanan);
    }

    @NonNull
    @Override
    public MakananViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.makanan_item, parent, false);
        return new MakananViewHolder(view);
    }

    public class MakananViewHolder extends RecyclerView.ViewHolder {

        public TextView namaMakanan;
        public TextView lokasiMakanan;
        public TextView jumlahMakanan;
        public TextView tanggalMakanan;
        public TextView userName;
        public ImageView imageMakanan;

        public MakananViewHolder(@NonNull View itemView) {
            super(itemView);

            namaMakanan = itemView.findViewById(R.id.txtNamaMakananCard);
            lokasiMakanan = itemView.findViewById(R.id.txtLokasiCard);
            jumlahMakanan = itemView.findViewById(R.id.txtJumlahMakananCard);
            tanggalMakanan = itemView.findViewById(R.id.txtDateMakananCard);
            userName = itemView.findViewById(R.id.txtUserNameCard);
            imageMakanan = itemView.findViewById(R.id.imageMakananCard);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClicked(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface onItemClickListener{
        void onItemClicked(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(onItemClickListener listener){
        this.listener = listener;
    }
}
