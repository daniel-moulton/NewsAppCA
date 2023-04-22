package com.example.newsappca;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // Get references to views
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView authorTextView = findViewById(R.id.authorTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView contentTextView = findViewById(R.id.contentTextView);
        ImageView imageView = findViewById(R.id.imageView);

        // Get data from intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String description = intent.getStringExtra("description");
        String content = intent.getStringExtra("content");
        String imageUrl = intent.getStringExtra("image");

        // Set data to views
        titleTextView.setText(title);
        authorTextView.setText(author);
        descriptionTextView.setText(description);
        contentTextView.setText(content);
        Picasso.get().load(imageUrl).error(R.drawable.placeholder).into(imageView);
    }
}