package api;

@SuppressWarnings("unused")
public class QuickSearchModel {
    private String id;
    private String titleEn;
    private String titleKa;

    public String getId() {
        return id;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public String getTitleKa() {
        return titleKa;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public void setTitleKa(String titleKa) {
        this.titleKa = titleKa;
    }

    public QuickSearchModel() {
    }
}
