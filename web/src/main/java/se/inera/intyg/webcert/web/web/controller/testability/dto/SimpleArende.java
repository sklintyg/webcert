package se.inera.intyg.webcert.web.web.controller.testability.dto;

/**
 * Created by eriklupander on 2016-11-21.
 */
public class SimpleArende {

    private String id;
    private String title;

    public SimpleArende(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
