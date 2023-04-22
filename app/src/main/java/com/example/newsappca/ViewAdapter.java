package com.example.newsappca;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ViewAdapter extends RecyclerView.Adapter<ViewHolder> {

    // Context of the activity that contains the RecyclerView
    private final Context context;

    // List of news articles to display in the RecyclerView
    private final List<NewsArticle> articles;

    public ViewAdapter(Context context, List<NewsArticle> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates a layout from XML and returns a new ViewHolder that holds the view
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
    }

    // Called by RecyclerView to display the data at the specified position
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Gets the NewsArticle object at the current position in the list
        NewsArticle currentArticle = articles.get(position);

        // Sets the title textView to the title of the news article
        holder.titleView.setText(currentArticle.getTitle());

        // Sets the source textView to the author and publication date of the news article
        if (currentArticle.getAuthor() != null) {
            holder.sourceView.setText(currentArticle.getAuthor() + " | " + currentArticle.getPubDate());
        }

        // Sets the description textView to the description of the news article
        if (currentArticle.getDescription() != null) {
            holder.descriptionView.setText(currentArticle.getDescription());
        } else {
            holder.descriptionView.setText("Description unavailable");
        }

        // Loads the image of the news article using Picasso and sets it in the imageView
        String imgUrl = currentArticle.getUrlToImage();
        if (!(imgUrl.isEmpty())) {
            Picasso.get().load(imgUrl).error(R.drawable.placeholder).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder);
        }

        // onClick listener for when an article is clicked
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, WebActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("title", currentArticle.getTitle());
            intent.putExtra("author", currentArticle.getAuthor());
            intent.putExtra("description", currentArticle.getDescription());
            intent.putExtra("content", currentArticle.getContent());
            intent.putExtra("image", currentArticle.getUrlToImage());

            context.startActivity(intent);
        });
    }

    // Returns the total number of items in the data set held by the adapter
    @Override
    public int getItemCount() {
        return articles.size();
    }
}
