package ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ge.redefine.adjaranet.R;
import model.Movie;

public class SectionsAdapter extends RecyclerView.Adapter<SectionViewHolder> {
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

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.section_layout, parent, false);

        return new SectionViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
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

}
