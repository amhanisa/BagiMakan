package com.amhanisa.bagimakan.Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amhanisa.bagimakan.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class InputImageAdapter extends RecyclerView.Adapter<InputImageAdapter.ImageViewHolder> {

    public List<Uri> imageList;

    public InputImageAdapter(List<Uri> imageUri){
        this.imageList = imageUri;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.input_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri currentImage = imageList.get(position);
        Picasso.get().load(currentImage).fit().centerCrop().into(holder.image);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{

    public ImageView image;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.inputImage);
    }
}
}
