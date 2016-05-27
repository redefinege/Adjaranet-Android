package model;

import io.realm.RealmObject;

@SuppressWarnings("unused")
public class Favorite extends RealmObject {
    public static final String MOVIE = "movie";
    public static final String MOVIE__ID = MOVIE + "." + Movie.ID;

    Movie movie;

    public Favorite() {
    }

    public Favorite(Movie movie) {
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
