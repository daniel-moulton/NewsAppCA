package com.example.newsappca;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    TextView titleView;
    TextView descriptionView;
    TextView sourceView;
    ImageView imageView;

    public ViewHolder(View itemView){
        super(itemView);

        // Initialize the UI elements in the ViewHolder
        titleView=itemView.findViewById(R.id.article_title);
        descriptionView=itemView.findViewById(R.id.article_description);
        sourceView=itemView.findViewById(R.id.article_source);
        imageView=itemView.findViewById(R.id.article_image);
    }
}
