package com.myapp2.rebecca.guardianresearchtool;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


//A NewsAdapter knows how to create a list item layout for each news piece in the data source
//(a list of news objects)

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private static final String LOG_TAG = NewsAdapter.class.getSimpleName();

    private List<News> newsItems = new ArrayList<>();
    private Context context;

    public NewsAdapter(List<News> newsItems, Context context) {
        this.newsItems = newsItems;
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewsAdapter.ViewHolder holder, int position) {
        holder.headlineTextView.setText(newsItems.get(position).getmHeadline());
        holder.dateTextView.setText(newsItems.get(position).getmDate());
        holder.sectionTextView.setText(newsItems.get(position).getmSection());

        String imageUrl = newsItems.get(position).getmNewsThumb();

        Glide.with(context).load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.no_image_found)
                .centerCrop()
                .into(holder.thumbImageView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.headline)
        TextView headlineTextView;
        @BindView(R.id.date)
        TextView dateTextView;
        @BindView(R.id.section)
        TextView sectionTextView;
        @BindView(R.id.thumbnail)
        ImageView thumbImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            News story = newsItems.get(getAdapterPosition());
            Uri newsUri = Uri.parse(story.getUrl());
            Intent webUrlIntent = new Intent(Intent.ACTION_VIEW, newsUri);
            v.getContext().startActivity(webUrlIntent);
        }
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }
}
