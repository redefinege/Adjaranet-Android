package model;

import io.realm.Realm;

@SuppressWarnings("unused")
public class FavoriteRM extends RMHelper {
    public static Favorite get(Movie movie) {
        return get(movie.getId());
    }

    public static Favorite get(String movieId) {
        Favorite favorite = sRealm.where(Favorite.class)
                .equalTo(Favorite.MOVIE__ID, movieId)
                .findFirst();

        return favorite;
    }

    public static boolean isFavorite(Movie movie) {
        return isFavorite(movie.getId());
    }

    public static boolean isFavorite(String movieId) {
        Favorite favorite = get(movieId);
        return favorite == null ? false : true;
    }

    public static void remove(Movie movie) {
        remove(movie.getId());
    }

    public static void remove(final String movieId) {
        sRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Favorite favorite = get(movieId);
                favorite.deleteFromRealm();
            }
        });
    }

    public static void add(final Movie movie) {
        if (!isFavorite(movie.getId())) {
            sRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Movie movieRealm = realm.copyToRealmOrUpdate(movie);
                    Favorite newFavorite = new Favorite(movieRealm);
                    realm.copyToRealm(newFavorite);
                }
            });
        }
    }

    public static void toggle(Movie movie) {
        if (isFavorite(movie)) remove(movie);
        else add(movie);
    }
}
