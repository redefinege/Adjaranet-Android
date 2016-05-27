package ui.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import model.Movie;
import ge.redefine.adjaranet.R;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.SectionVH> {
    private List<List<Movie>> mChildDataset = new ArrayList<>();
    private List<String> mSectionHeaderList = new ArrayList<>();
    private OnMovieInteractionListener mListener;

    public SectionsAdapter(List<String> sectionHeaderList, OnMovieInteractionListener listener) {
        mSectionHeaderList = sectionHeaderList;
        mListener = listener;
    }

    public void update(List<String> sectionHeaderList) {
        mSectionHeaderList = sectionHeaderList;
        notifyDataSetChanged();
    }

    /**
     * Update child dataset at given position
     * This is synchronized because it may be accessed from different threads
     *
     * @param position
     * @param dataset
     */
    public synchronized void updateChild(int position, List<Movie> dataset) {
        if (position >= mChildDataset.size()) {
            for (int i = 0; i <= position; i++) {
                mChildDataset.add(new ArrayList<Movie>());
            }
        }
        mChildDataset.set(position, dataset);
        notifyItemChanged(position);
    }

    @Override
    public SectionVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.section_layout, parent, false);

        return new SectionVH(view);
    }

    @Override
    public void onBindViewHolder(SectionVH holder, int position) {
        holder.mHeaderText.setText(mSectionHeaderList.get(position));
        try {
            List<Movie> dataset = mChildDataset.get(position);
            holder.mItemsAdapter.update(dataset);
        } catch (Exception ignored) {
            holder.mItemsAdapter.update(new ArrayList<Movie>());
        }
    }

    @Override
    public int getItemCount() {
        return mSectionHeaderList.size();
    }

    public class SectionVH extends RecyclerView.ViewHolder {
        public TextView mHeaderText;
        public RecyclerView mRecyclerView;
        public LinearLayoutManager mLayoutManager;
        public MovieItemsAdapter mItemsAdapter;

        public SectionVH(View itemView) {
            super(itemView);

            mHeaderText = (TextView) itemView.findViewById(R.id.section_title);
            mRecyclerView = (RecyclerView) itemView.findViewById(R.id.section_recycler_view);

            // setup layout manager
            mLayoutManager = new LinearLayoutManager(itemView.getContext(),
                    LinearLayoutManager.HORIZONTAL, false);
            mLayoutManager.setAutoMeasureEnabled(true);

            // setup adapter
            mItemsAdapter = new MovieItemsAdapter(mListener);

            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mItemsAdapter);
        }
    }


}
