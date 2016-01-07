package com.timappweb.timapp.views;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;


import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.LinkedList;


public class HorizontalRecyclerView extends TagRecyclerView {


    public HorizontalRecyclerView(Context context) {
        super(context);
        this.init();
    }

    public HorizontalRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public HorizontalRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    protected void init(){
        this.setHasFixedSize(true);

        this.setAdapter(new HorizontalTagsAdapter(getContext(), new LinkedList<Tag>()));

        GridLayoutManager manager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        this.setLayoutManager(manager);
        this.scrollToEnd();
    }
}