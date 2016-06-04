package ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ge.redefine.adjaranet.R;
import model.Episode;
import model.EpisodeList;

public class EpisodeItemsAdapter extends RecyclerView.Adapter<EpisodeItemsAdapter.EpisodeItemsVH> {
    private Context mContext;
    private OnEpisodeClickListener mListener;
    private EpisodeList mDataset;
    private Integer mCurrentEpisode = -1;

    public EpisodeItemsAdapter(Context context, OnEpisodeClickListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void update(EpisodeList dataset, Integer currentEpisode) {
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
        Episode episode = mDataset.getEpisodeByIndex(position);
        holder.episodeNumber.setText(String.valueOf(mDataset.getEpisodeNumberByIndex(position)));
        holder.episodeTitle.setText(episode.getNameEn());
        if (mCurrentEpisode.equals(mDataset.getEpisodeNumberByIndex(position))) {
            holder.setSelected();
        } else {
            holder.setNormal();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.getSize();
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
