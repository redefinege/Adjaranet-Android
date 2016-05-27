package api;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import model.Actor;
import model.Director;
import model.Movie;

public class MovieListDeserializer implements JsonDeserializer<List<Movie>> {
    // MOVIE
    public static final String ID = "id";
    public static final String TITLE_EN = "title_en";
    public static final String TITLE_KA = "title_ge";
    public static final String POSTER = "poster";
    public static final String IMDB_RATING = "imdb";
    public static final String IMDB_ID = "imdb_id";
    public static final String IMDB_VOTES = "imdb_votes";
    public static final String RELEASE_YEAR = "release_date";
    public static final String DESCRIPTION = "description";
    public static final String VIEW_COUNT = "views";
    public static final String DURATION = "duration";
    public static final String SEASON = "season";
    public static final String EPISODE = "episode";
    public static final String LANGUAGES = "lang";
    public static final String ACTORS = "actors";
    public static final String DIRECTORS = "directors";
    // ACTOR
    public static final String ACTOR__ID = "id";
    public static final String ACTOR__NAME = "actor";
    // DIRECTOR
    public static final String DIRECTOR__ID = "id";
    public static final String DIRECTOR__NAME_EN = "name_eng";
    public static final String DIRECTOR__NAME_KA = "name_ka";

    @Override
    public List<Movie> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final List<Movie> list = new ArrayList<>();
        final JsonArray array = json.isJsonArray() ? json.getAsJsonArray() : new JsonArray();

        for (int i = 0; i < array.size(); i++) {
            final Movie movie = new Movie();
            final JsonObject movieObject = array.get(i).getAsJsonObject();

            movie.setId(GsonUtils.getNullStringAsEmpty(movieObject, ID));
            movie.setTitleEn(GsonUtils.getNullStringAsEmpty(movieObject, TITLE_EN));
            movie.setTitleKa(GsonUtils.getNullStringAsEmpty(movieObject, TITLE_KA));
            movie.setPoster(GsonUtils.getNullStringAsEmpty(movieObject, POSTER));
            movie.setImdbRating(GsonUtils.getNullStringAsEmpty(movieObject, IMDB_RATING));
            movie.setImdbId(GsonUtils.getNullStringAsEmpty(movieObject, IMDB_ID));
            movie.setImdbVotes(GsonUtils.getNullStringAsEmpty(movieObject, IMDB_VOTES));
            movie.setReleaseYear(GsonUtils.getNullStringAsEmpty(movieObject, RELEASE_YEAR));
            movie.setDescription(GsonUtils.getNullStringAsEmpty(movieObject, DESCRIPTION));
            movie.setViewCount(GsonUtils.getNullStringAsEmpty(movieObject, VIEW_COUNT));
            movie.setDuration(GsonUtils.getNullStringAsEmpty(movieObject, DURATION));
            movie.setSeason(GsonUtils.getNullStringAsEmpty(movieObject, SEASON));
            movie.setEpisode(GsonUtils.getNullStringAsEmpty(movieObject, EPISODE));
            movie.setLanguages(GsonUtils.getCommaSeparatedList(movieObject, LANGUAGES));
            movie.setActors(getActorsList(movieObject, ACTORS));
            movie.setDirectors(getDirectorsList(movieObject, DIRECTORS));
            list.add(movie);
        }

        return list;
    }

    private List<Actor> getActorsList(JsonObject object, String member) {
        final List<Actor> list = new ArrayList<>();
        if (object.has(member)) {
            final JsonElement element = object.get(member);
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonElement el = array.get(i);
                    if (el.isJsonObject()) {
                        final JsonObject item = el.getAsJsonObject();
                        final Actor actor = new Actor();
                        actor.setId(GsonUtils.getNullStringAsEmpty(item, ACTOR__ID));
                        actor.setName(GsonUtils.getNullStringAsEmpty(item, ACTOR__NAME));
                        list.add(actor);
                    }
                }
            }
        }

        return list;
    }

    private List<Director> getDirectorsList(JsonObject object, String member) {
        final List<Director> list = new ArrayList<>();
        if (object.has(member)) {
            final JsonElement element = object.get(member);
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonElement el = array.get(i);
                    if (el.isJsonObject()) {
                        final JsonObject item = el.getAsJsonObject();
                        final Director director = new Director();
                        director.setId(GsonUtils.getNullStringAsEmpty(item, DIRECTOR__ID));
                        director.setNameEn(GsonUtils.getNullStringAsEmpty(item, DIRECTOR__NAME_EN));
                        director.setNameKa(GsonUtils.getNullStringAsEmpty(item, DIRECTOR__NAME_KA));
                        list.add(director);
                    }
                }
            }
        }

        return list;
    }
}
