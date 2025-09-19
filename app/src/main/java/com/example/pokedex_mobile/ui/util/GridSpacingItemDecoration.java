package com.example.pokedex_mobile.ui.util;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int halfSpace;
    public GridSpacingItemDecoration(int spacePx) { this.halfSpace = spacePx / 2; }
    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = halfSpace;
        outRect.right = halfSpace;
        outRect.top = halfSpace;
        outRect.bottom = halfSpace;
    }
}
