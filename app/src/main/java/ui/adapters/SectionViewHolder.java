package ui.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ge.redefine.adjaranet.R;

public class SectionViewHolder extends RecyclerView.ViewHolder {
    TextView mHeaderText;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    MovieItemsAdapter mItemsAdapter;

    SectionViewHolder(View itemView, OnMovieInteractionListener mListener) {
        super(itemView);

        mHeaderText = itemView.findViewById(R.id.section_title);
        mRecyclerView = itemView.findViewById(R.id.section_recycler_view);

        // setup layout manager
        mLayoutManager = new SectionViewHolder.LayoutManager(itemView.getContext(),
                LinearLayoutManager.HORIZONTAL, false);

        // setup adapter
        mItemsAdapter = new MovieItemsAdapter(mListener);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mItemsAdapter);
    }

    static class LayoutManager extends LinearLayoutManager {
        LayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public boolean isAutoMeasureEnabled() {
            return true;
        }
    }
}
