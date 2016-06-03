package api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import model.Actor;
import model.Director;
import model.Episode;
import model.Genre;
import model.Series;

public class SeriesDeserializer implements JsonDeserializer<Series> {
    public static final String CASTING = "cast";
    public static final String DIRECTORS = "director";
    public static final String DESCRIPTION = "desc";
    public static final String GENRES = "genres";
    public static final String URL = "file_url";
    public static final String EPISODE__NAME_EN = "name";
    public static final String EPISODE__NAME_KA = "name_geo";
    public static final String EPISODE__LANGUAGE = "lang";
    public static final String EPISODE__QUALITY = "quality";

    @Override
    public Series deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final Series series = new Series();
        final JsonObject object = json.isJsonObject() ? json.getAsJsonObject() : new JsonObject();

        if (object.has(URL)) {
            String url = object.get(URL).getAsString();
            series.setUrl(url);
        }

        if (object.has(CASTING)) {
            List<Actor> actors = new ArrayList<>();
            JsonObject casting = object.get(CASTING).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : casting.entrySet()) {
                Actor actor = new Actor();
                actor.setId(entry.getKey());
                actor.setName(entry.getValue().getAsString());
                actors.add(actor);
            }
            series.setCasting(actors);
        }

        if (object.has(DIRECTORS)) {
            List<Director> directors = new ArrayList<>();
            JsonObject directorsObject = object.get(DIRECTORS).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : directorsObject.entrySet()) {
                Director director = new Director();
                director.setId(entry.getKey());
                director.setNameEn(entry.getValue().getAsString());
                directors.add(director);
            }
            series.setDirectors(directors);
        }

        if (object.has(GENRES)) {
            List<Genre> genres = new ArrayList<>();
            JsonObject genresObject = object.get(GENRES).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : genresObject.entrySet()) {
                Genre genre = new Genre();
                genre.setId(entry.getKey());
                genre.setName(entry.getValue().getAsString());
                genres.add(genre);
            }
            series.setGenres(genres);
        }

        TreeMap<Integer, List<Episode>> episodesMap = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            int season;
            try {
                season = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException ignored) {
                continue;
            }

            if (season <= 0) {
                continue;
            }

            if (!entry.getValue().isJsonObject()) {
                continue;
            }

            List<Episode> episodes = new ArrayList<>();
            JsonObject seasonObject = entry.getValue().getAsJsonObject();
            for (Map.Entry<String, JsonElement> episodeEntry : seasonObject.entrySet()) {
                Episode episode = new Episode();
                JsonObject episodeObject = episodeEntry.getValue().getAsJsonObject();
                episode.setId(episodeEntry.getKey());
                episode.setNameEn(episodeObject.get(EPISODE__NAME_EN).getAsString());
                episode.setNameKa(episodeObject.get(EPISODE__NAME_KA).getAsString());
                episode.setLanguage(episodeObject.get(EPISODE__LANGUAGE).getAsString());
                episode.setQuality(episodeObject.get(EPISODE__QUALITY).getAsString());
                episodes.add(episode);
            }
            episodesMap.put(season, episodes);
        }
        series.setEpisodesMap(episodesMap);

        return series;
    }
}
