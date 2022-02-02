package com.atiqur.pidtuner.utils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.atiqur.pidtuner.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class ToolbarHelper {

    ToolbarHelper(Toolbar toolbar, CollapsingToolbarLayout collapsingToolbarLayout, AppCompatActivity activity, String title) {
        toolbar.setTitleTextColor(activity.getColor(R.color.white));
        activity.setSupportActionBar(toolbar);

        activity.getSupportActionBar().setTitle(title);


        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setExpandedTitleColor(activity.getColor(R.color.white));
            collapsingToolbarLayout.setCollapsedTitleTextColor(activity.getColor(R.color.white));
        }

    }

    public static com.atiqur.pidtuner.utils.ToolbarHelper create(Toolbar toolbar, CollapsingToolbarLayout collapsingToolbarLayout, AppCompatActivity activity, String title) {
        return new com.atiqur.pidtuner.utils.ToolbarHelper(toolbar, collapsingToolbarLayout, activity, title);
    }

}
