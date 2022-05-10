package se.inera.intyg.webcert.web.service.facade.user;

public class UserTab {
    private String title;
    private String url;
    private long number;

    public UserTab(String title, String url, long number) {
        this.title = title;
        this.url = url;
        this.number = number;
    }

    public UserTab(String title, String url) {
        this.title = title;
        this.url = url;
        this.number = 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }
}
