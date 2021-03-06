package com.timappweb.timapp.utils.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 5/20/2016.
 */

public class FragmentGroup<T extends Fragment>{

    private static final String ARG_POSITION = "position";

    Context context;
    List<T> fragments;

    public FragmentGroup(Context context) {
        this.context = context;
        this.fragments = new LinkedList<>();
    }

    public static FragmentGroup createGroup(Context context) {
        return new FragmentGroup(context);
    }

    public T add(T fragment) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, fragments.size());
        fragment.setArguments(args);
        fragments.add(fragment);
        return fragment;
    }

    public List<T> getFragments() {
        return fragments;
    }

    public int size() {
        return fragments.size();
    }
}
