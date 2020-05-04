package com.amhanisa.bagimakan.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.amhanisa.bagimakan.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ViewImageAdapter extends PagerAdapter {

    Context context;
    List<String> imageUri;

    public ViewImageAdapter(Context context, List<String> imageUri){
        this.context = context;
        this.imageUri = imageUri;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        Picasso.get()
                .load(imageUri.get(position))
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_image_black_24dp)
                .into(imageView);
        container.addView(imageView);

        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageUri.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

}
