package model;

import org.parceler.Parcel;

@SuppressWarnings("unused")
@Parcel
public class Episode {
    String id;
    String nameEn;
    String nameKa;
    String language;
    String quality;

    public Episode() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameKa() {
        return nameKa;
    }

    public void setNameKa(String nameKa) {
        this.nameKa = nameKa;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}
