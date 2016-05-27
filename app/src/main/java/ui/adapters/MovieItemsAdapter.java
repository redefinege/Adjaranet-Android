package ui.adapters;

import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ge.redefine.adjaranet.R;
import helpers.ResourcesProvider;
import model.FavoriteRM;
import model.Movie;
import network.VolleySingleton;
import ui.helpers.CustomNetworkImageView;

public class MovieItemsAdapter extends RecyclerView.Adapter<MovieItemsAdapter.ViewHolder> {
    private List<Movie> mDataset = new ArrayList<>();
    private OnMovieInteractionListener mMovieListener;
    private String transitionName;

    public MovieItemsAdapter(OnMovieInteractionListener movieListener) {
        this(movieListener, null);
    }

    public MovieItemsAdapter(OnMovieInteractionListener movieListener, String transitionName) {
        mMovieListener = movieListener;
        this.transitionName = transitionName;
    }

    public void update(List<Movie> dataset) {
        mDataset = dataset;
        notifyDataSetChanged();
    }

    public void add(List<Movie> dataset) {
        mDataset.addAll(dataset);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Movie movie = mDataset.get(position);
        holder.posterImageView.setImageUrl(movie.getPoster(), VolleySingleton.getInstance().getImageLoader());
        holder.setDuration(movie.getDuration());
        holder.setEpisodeInfo(movie.getSeason(), movie.getEpisode());
        holder.setTitleEnText(movie.getTitleEn());
        holder.setTitleKaText(movie.getTitleKa());
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout cardView;
        public CustomNetworkImageView posterImageView;
        public TextView durationTextView;
        public TextView seasonTextView;
        public TextView episodeTextView;
        public ImageButton moreMenu;
        public PopupMenu popupMenu;
        public TextView titleEnTextView;
        public TextView titleKaTextView;

        public ViewHolder(final View itemView) {
            super(itemView);

            cardView = (RelativeLayout) itemView.findViewById(R.id.item_movie_card);
            posterImageView = (CustomNetworkImageView) itemView.findViewById(R.id.item_movie_poster);
            durationTextView = (TextView) itemView.findViewById(R.id.item_movie_duration);
            seasonTextView = (TextView) itemView.findViewById(R.id.item_movie_season);
            episodeTextView = (TextView) itemView.findViewById(R.id.item_movie_episode);
            moreMenu = (ImageButton) itemView.findViewById(R.id.item_movie_more);
            popupMenu = new PopupMenu(itemView.getContext(), moreMenu);
            titleEnTextView = (TextView) itemView.findViewById(R.id.item_movie_title_en);
            titleKaTextView = (TextView) itemView.findViewById(R.id.item_movie_text_title_ka);

            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.movie_item_menu, popupMenu.getMenu());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && transitionName != null
                    && !transitionName.isEmpty()) {
                posterImageView.setTransitionName(transitionName);
            }

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMovieListener.onMovieClicked(mDataset.get(getAdapterPosition()), posterImageView);
                }
            });

            moreMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MenuItem actionFavorite = popupMenu.getMenu().getItem(0);
                    Movie model = mDataset.get(getAdapterPosition());
                    boolean isFavorite = FavoriteRM.isFavorite(model.getId());
                    actionFavorite.setTitle(ResourcesProvider.getFavoriteMenuTitle(isFavorite));
                    popupMenu.show();
                }
            });

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_action_favorite: {
                            Movie movie = mDataset.get(getAdapterPosition());
                            mMovieListener.onFavoriteClicked(movie);
                            break;
                        }
                    }
                    return true;
                }
            });
        }

        public void setDuration(String duration) {
            if (duration.isEmpty() || duration.replace("0", "").equals(":")) {
                durationTextView.setVisibility(View.GONE);
                return;
            }
            durationTextView.setText(duration);
        }

        public void setEpisodeInfo(String season, String episode) {
            if (season.isEmpty() || episode.isEmpty()
                    || season.equals("0") || episode.equals("0")) {
                seasonTextView.setVisibility(View.INVISIBLE);
                episodeTextView.setVisibility(View.INVISIBLE);
                return;
            }

            seasonTextView.setVisibility(View.VISIBLE);
            episodeTextView.setVisibility(View.VISIBLE);

            seasonTextView.setText(String.format(
                    String.format(ResourcesProvider.getSeasonInfoText(), season)
            ));
            episodeTextView.setText(String.format(
                    String.format(ResourcesProvider.getEpisodeInfoText(), episode)
            ));
        }

        public void setTitleEnText(String text) {
            titleEnTextView.setText(text);
            titleEnTextView.setHorizontallyScrolling(true);
            titleEnTextView.setSelected(true);
        }

        public void setTitleKaText(String text) {
            titleKaTextView.setText(text);
            titleKaTextView.setHorizontallyScrolling(true);
            titleKaTextView.setSelected(true);
        }
    }
}
