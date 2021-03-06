package com.timappweb.timapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.List;

public class HorizontalTagsAdapter extends RecyclerView.Adapter<HorizontalTagsAdapter.MyViewHolder> {
    private String TAG = "HorizontalTagsAdapter";

    protected LayoutInflater inflater;
    private List<Tag> mDataTags;
    private Context context;

    private int textColor;
    private int backgroundColor;
    private boolean isBold;
    private Float textSize;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public HorizontalTagsAdapter(Context context) {
        this.context = context;
        this.mDataTags = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_tag_horizontal, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Tag current = mDataTags.get(position);
        holder.textView.setText("#" + current.name);

        if (itemAdapterClickListener != null){
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemAdapterClickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.mDataTags.size();
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public void setDummyData() {
        mDataTags.add(new Tag("plongée"));
        mDataTags.add(new Tag("chillmusic"));
        mDataTags.add(new Tag("festival"));
        notifyDataSetChanged();
    }

    public void add(Tag tag) {
        mDataTags.add(tag);
        notifyDataSetChanged();
    }

    public void setData(List<Tag> data) {
        if(data!=null) {
            this.mDataTags = new ArrayList<>(data);
            notifyDataSetChanged();
        }
    }

    public Tag get(int position) {
        return this.mDataTags.get(position);
    }

    public List<Tag> getData() {
        return this.mDataTags;
    }

    public int getPositionFromTag(Tag tag) {
        int i = 0;
        for (Tag item : mDataTags) {
            i++;
            if(item == tag) return i;
        }
        return -1;
    }

    public void remove(Tag tag) {
        this.mDataTags.remove(tag);
        this.notifyDataSetChanged();
    }

    public void clearData() {
        this.mDataTags.clear();
        this.notifyDataSetChanged();
    }

    public void setColors(int textColor, int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public void settextStyle(boolean isBold) {
        this.isBold = isBold;
    }

    public void settextSize(Float textSize) {
        this.textSize = textSize;
    }

    public int getMinTags() {
        return ConfigurationProvider.rules().posts_min_tag_number;
    }

    /*
    public ArrayList<String> getStringsFromTags() {
        ArrayList<String> res = new ArrayList<String>();
        for (Tag tag : this.mDataTags) {
            res.add(tag.getName());
        }
        return res;
    }
    public ArrayList<Tag> getTagsFromStrings(ArrayList<String> tagsString) {
        ArrayList<Tag> tagsList = new ArrayList<Tag>();
        for(String s : tagsString ) {
            Tag tag = new Tag(s);
            tagsList.add(tag);
        }
        return tagsList;
    }
    */

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.item_horizontal_tag);
            view.setBackgroundColor(backgroundColor);
            textView.setTextColor(textColor);
            //Can't change text size, because it will insert unwanted margins in the UI.
            //textView.setTextSize(Util.convertPixelsToDp(textSize, context));
            //TODO : delete TextSize attribute for the view
            if(!isBold) {
                textView.setTypeface(Typeface.DEFAULT);
            } else {
                textView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }
    }
}
