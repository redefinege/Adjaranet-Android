package model;

import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;
import org.parceler.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.realm.MovieRealmProxy;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import model.helpers.RealmListParcelConverter;

@SuppressWarnings("unused")
@Parcel(implementations = { MovieRealmProxy.class },
        value = Parcel.Serialization.BEAN,
        analyze = { Movie.class })
public class Movie extends RealmObject {
    public static final String ID = "id";
    public static final String TITLE_EN = "titleEn";
    public static final String TITLE_KA = "titleKa";
    public static final String DURATION = "duration";
    public static final String DESCRIPTION = "description";
    public static final String RELEASE_YEAR = "releaseYear";
    public static final String VIEW_COUNT = "viewCount";
    public static final String SEASON = "season";
    public static final String EPISODE = "episode";
    public static final String POSTER = "poster";
    public static final String IMDB_RATING = "imdbRating";
    public static final String IMDB_ID = "imdbId";
    public static final String IMDB_VOTES = "imdbVotes";
    public static final String LANGUAGES = "languages";
    public static final String QUALITIES = "qualities";
    public static final String PLAYBACK_URL = "playbackUrl";
    public static final String ACTORS = "actors";
    public static final String DIRECTORS = "directors";

    @PrimaryKey
    String id;                              // movie id
    String titleEn;                         // english title
    String titleKa;                         // georgian title
    String duration;                        // movie duration
    String description;                     // movie description
    String releaseYear;                     // movie release year
    String viewCount;                       // view count
    String season;                          // season number
    String episode;                         // episode number
    String poster;                          // movie poster url
    String imdbRating;                      // imdb rating
    String imdbId;                          // imdb id
    String imdbVotes;                       // imdb votes
    @Ignore
    String languages;                       // comma separated list of languages
    @Ignore
    String qualities;                       // comma separated list of qualities
    @Ignore
    String playbackUrl;                     // movie playback url
    RealmList<Actor> actors;                // list of movie actors
    RealmList<Director> directors;          // list of movie directors

    public Movie() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getTitleKa() { return titleKa; }
    public void setTitleKa(String titleKa) { this.titleKa = titleKa; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) {
        if (duration.isEmpty()) {
            this.duration = "0:00";
        } else {
            this.duration = duration;
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        description = description.replaceAll("(?i)&quot;", "\"");
        description = description.replaceAll("(?i)&amp;", "&");
        this.description = description;
    }

    public String getReleaseYear() { return releaseYear; }
    public void setReleaseYear(String releaseYear) { this.releaseYear = releaseYear; }

    public String getViewCount() { return viewCount; }
    public void setViewCount(String viewCount) {
        if (viewCount.isEmpty()) {
            this.viewCount = "0";
        } else {
            this.viewCount = viewCount;
        }
    }

    public String getSeason() { return season; }
    @Transient
    public int getSeasonAsInt() {
        try {
            return Integer.parseInt(season);
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }
    public void setSeason(String season) { this.season = season; }

    public String getEpisode() { return episode; }
    public int getEpisodeAsInt() {
        try {
            return Integer.parseInt(episode);
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }
    public void setEpisode(String episode) { this.episode = episode; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public String getImdbVotes() { return imdbVotes; }
    public void setImdbVotes(String imdbVotes) { this.imdbVotes = imdbVotes; }

    public String getLanguages() { return languages; }
    @Transient
    public List<String> getLanguagesAsList() {
        if (languages == null) {
            return new ArrayList<>();
        }

        String[] languagesArray = languages.split(",");
        List<String> languagesList = new ArrayList<>();
        Collections.addAll(languagesList, languagesArray);
        return languagesList;
    }
    public void setLanguages(String languages) { this.languages = languages; }
    @Transient
    public void setLanguages(List<String> languages) {
        String languageString = "";
        for (String language : languages) {
            languageString += language + ",";
        }

        if (languageString.endsWith(",")) {
            languageString = languageString.substring(0, languageString.length() - 1);
        }

        this.languages = languageString;
    }

    public String getQualities() { return qualities; }
    @Transient
    public List<String> getQualitiesAsList() {
        if (qualities == null) {
            return new ArrayList<>();
        }

        String[] qualitiesArray = qualities.split(",");
        List<String> qualitiesList = new ArrayList<>();
        Collections.addAll(qualitiesList, qualitiesArray);
        return qualitiesList;
    }
    public void setQualities(String qualities) { this.qualities = qualities; }
    @Transient
    public void setQualities(List<String> qualities) {
        String qualitiesString = "";
        for (String quality : qualities) {
            qualitiesString += quality + ",";
        }

        if (qualitiesString.endsWith(",")) {
            qualitiesString = qualitiesString.substring(0, qualitiesString.length() - 1);
        }

        this.qualities = qualitiesString;
    }

    public String getPlaybackUrl() { return playbackUrl; }
    public void setPlaybackUrl(String playbackUrl) {
        if (!playbackUrl.contains("://")) {
            playbackUrl = "http://" + playbackUrl;
        }
        this.playbackUrl = playbackUrl;
    }

    public RealmList<Actor> getActors() { return actors; }
    @ParcelPropertyConverter(RealmListParcelConverter.class)
    public void setActors(RealmList<Actor> actors) { this.actors = actors; }
    @Transient
    public void setActors(List<Actor> actors) {
        this.actors = new RealmList<>();
        this.actors.addAll(actors);
    }

    public RealmList<Director> getDirectors() { return directors; }
    @ParcelPropertyConverter(RealmListParcelConverter.class)
    public void setDirectors(RealmList<Director> directors) { this.directors = directors; }
    @Transient
    public void setDirectors(List<Director> directors) {
        this.directors = new RealmList<>();
        this.directors.addAll(directors);
    }

    @Transient
    public String getPlaybackUrl(int langIndex, int qualityIndex) {
        String language = getLanguagesAsList().get(langIndex);
        String quality = getQualitiesAsList().get(qualityIndex);

        if (language == null || quality == null) {
            return "";
        }

        String seriesString = "";
        if (isEpisode()) {
            String seasonString = String.format(Locale.US, "%02d", getSeasonAsInt());
            String episodeString = String.format(Locale.US, "%02d", getEpisodeAsInt());
            seriesString = seasonString + "_" + episodeString + "_";
        }

        return getPlaybackUrl()
                + getId() + "_"
                + seriesString
                + language + "_"
                + quality + ".mp4";
    }

    @Transient
    public String getFullTitle() {
        String separator = " - ";
        if (titleEn.isEmpty() || titleKa.isEmpty()) {
            separator = "";
        }
        return titleEn + separator + titleKa;
    }

    @Transient
    public boolean isEpisode() {
        int episode = getEpisodeAsInt();
        int season = getSeasonAsInt();

        return episode > 0 && season > 0;
    }
}
