package ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ge.redefine.adjaranet.R;
import model.Episode;

public class EpisodeItemsAdapter extends RecyclerView.Adapter<EpisodeItemsAdapter.EpisodeItemsVH> {
    private Context mContext;
    private OnEpisodeClickListener mListener;
    private List<Episode> mDataset;
    private int mCurrentEpisode = -1;

    public EpisodeItemsAdapter(Context context, OnEpisodeClickListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void update(List<Episode> dataset, int currentEpisode) {
        mDataset = dataset;
        mCurrentEpisode = currentEpisode;
        notifyDataSetChanged();
    }

    @Override
    public EpisodeItemsVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.episode_item_layout, parent, false);

        return new EpisodeItemsVH(v);
    }

    @Override
    public void onBindViewHolder(EpisodeItemsVH holder, int position) {
        holder.episodeNumber.setText(String.valueOf(position + 1));
        holder.episodeTitle.setText(mDataset.get(position).getNameEn());
        if (position == mCurrentEpisode) {
            holder.setSelected();
        } else {
            holder.setNormal();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public class EpisodeItemsVH extends RecyclerView.ViewHolder {
        public LinearLayout episodeLayout;
        public TextView episodeNumber;
        public TextView episodeTitle;

        public EpisodeItemsVH(View itemView) {
            super(itemView);

            episodeLayout = (LinearLayout) itemView.findViewById(R.id.episode_layout);
            episodeNumber = (TextView) itemView.findViewById(R.id.episode_number);
            episodeTitle = (TextView) itemView.findViewById(R.id.episode_title);

            episodeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onEpisodeClicked(getAdapterPosition());
                }
            });
        }

        public void setSelected() {
            episodeNumber.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextDark));
            episodeTitle.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextDark));
        }

        public void setNormal() {
            episodeNumber.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextGray));
            episodeTitle.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextGray));
        }
    }
}
