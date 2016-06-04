package model;

import org.parceler.Parcel;
import org.parceler.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@SuppressWarnings("unused")
@Parcel
public class Series {
    TreeMap<Integer, EpisodeList> episodesMap;
    List<Actor> casting;
    List<Genre> genres;
    List<Director> directors;
    String description;
    String url;

    public Series() {
    }

    @Transient
    public Episode getEpisode(Integer season, Integer episode) {
        if (!episodesMap.containsKey(season)) {
            return null;
        }

        EpisodeList episodeList = episodesMap.get(season);
        if (!episodeList.getEpisodeList().containsKey(episode)) {
            return null;
        }

        return episodeList.getEpisodeList().get(episode);
    }

    @Transient
    public List<Integer> getSeasonList() {
        List<Integer> seasonList = new ArrayList<>(episodesMap.keySet());

        return seasonList;
    }

    @Transient
    public Integer getSeasonNumberByIndex(int index) {
        List<Integer> seasonList = new ArrayList<>(episodesMap.keySet());
        return seasonList.get(index);
    }

    public TreeMap<Integer, EpisodeList> getEpisodesMap() {
        return episodesMap;
    }

    public List<Actor> getCasting() {
        return casting;
    }

    public List<Director> getDirectors() {
        return directors;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public void setEpisodesMap(TreeMap<Integer, EpisodeList> episodesMap) {
        this.episodesMap = episodesMap;
    }

    public void setCasting(List<Actor> casting) {
        this.casting = casting;
    }

    public void setDirectors(List<Director> directors) {
        this.directors = directors;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
