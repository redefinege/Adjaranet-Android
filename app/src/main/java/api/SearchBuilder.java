package api;

import helpers.ResourcesProvider;

@SuppressWarnings("unused")
public abstract class SearchBuilder {
    private String searchUrl = "";
    private String episode = "0";
    private int display = 15;
    private int offset = 0;
    private String ajax = "1";
    private String startYear = "1900";
    private String endYear = null;
    private String isnew = "0";
    private String needtags = "0";
    private String orderBy = "date";
    private String orderOrder = "data";
    private String orderData = "published";
    private String language = "false";
    private String country = "false";
    private String keyword = "";

    public String getAjax() {
        return ajax;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    public int getDisplay() {
        return display;
    }

    public String getStartYear() {
        return startYear;
    }

    public String getEndYear() {
        if (endYear == null) {
            return String.valueOf(ResourcesProvider.getCurrentYear());
        } else {
            return endYear;
        }
    }

    public int getOffset() {
        return offset;
    }

    public String getIsnew() {
        return isnew;
    }

    public String getNeedtags() {
        return needtags;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getOrderOrder() {
        return orderOrder;
    }

    public String getOrderData() {
        return orderData;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setSearchUrl(String searchUrl) {
        if (searchUrl.substring(searchUrl.length()).equals("/")) {
            searchUrl = searchUrl.substring(0, searchUrl.length() - 1);
        }

        this.searchUrl = searchUrl;
    }

    public SearchBuilder setDisplay(int display) {
        this.display = display;
        return this;
    }

    public SearchBuilder setStartYear(String startYear) {
        this.startYear = startYear;
        return this;
    }

    public SearchBuilder setEndYear(String endYear) {
        this.endYear = endYear;
        return this;
    }

    public SearchBuilder setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public SearchBuilder setIsnew(String isnew) {
        this.isnew = isnew;
        return this;
    }

    public SearchBuilder setNeedtags(String needtags) {
        this.needtags = needtags;
        return this;
    }

    public SearchBuilder setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public SearchBuilder setOrderOrder(String orderOrder) {
        this.orderOrder = orderOrder;
        return this;
    }

    public SearchBuilder setOrderData(String orderData) {
        this.orderData = orderData;
        return this;
    }

    public SearchBuilder setLanguage(String language) {
        this.language = language;
        return this;
    }

    public SearchBuilder setCountry(String country) {
        this.country = country;
        return this;
    }

    public SearchBuilder setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    SearchBuilder() {
    }

    public String getUrl() {
        return searchUrl
                + "?ajax=" + getAjax()
                + "&display=" + getDisplay()
                + "&startYear=" + getStartYear()
                + "&endYear=" + getEndYear()
                + "&offset=" + getOffset()
                + "&isnew=" + getIsnew()
                + "&needtags=" + getNeedtags()
                + "&orderBy=" + getOrderBy()
                + "&order[order]=" + getOrderOrder()
                + "&order[data]=" + getOrderData()
                + "&language=" + getLanguage()
                + "&country=" + getCountry()
                + "&keyword=" + getKeyword()
                + "&episode=" + getEpisode();
    }
}
