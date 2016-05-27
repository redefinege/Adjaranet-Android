package ui.adapters;

import android.widget.ImageView;

import model.Movie;

public interface OnMovieInteractionListener {
    void onMovieClicked(Movie movie, ImageView imageView);

    void onFavoriteClicked(Movie movie);
}
