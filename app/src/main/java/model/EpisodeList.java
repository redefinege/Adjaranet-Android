package model;

import org.parceler.Parcel;
import org.parceler.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@SuppressWarnings("unused")
@Parcel
public class EpisodeList {
    TreeMap<Integer, Episode> episodeList;

    public TreeMap<Integer, Episode> getEpisodeList() {
        return episodeList;
    }

    public void setEpisodeList(TreeMap<Integer, Episode> episodeList) {
        this.episodeList = episodeList;
    }

    @Transient
    public int getSize() {
        return episodeList == null ? 0 : episodeList.size();
    }

    @Transient
    public Integer getEpisodeNumberByIndex(int index) {
        List<Integer> keyList = new ArrayList<>(episodeList.keySet());
        return keyList.get(index);
    }

    @Transient
    public Episode getEpisodeByIndex(int index) {
        List<Integer> keyList = new ArrayList<>(episodeList.keySet());
        return episodeList.get(keyList.get(index));
    }
}
