package se.inera.intyg.webcert.web.web.controller.api.dto;

import se.inera.intyg.webcert.persistence.fmb.model.FmbType;

import java.util.Collections;
import java.util.List;

public class FmbContent {

    private FmbType heading;
    private String text;
    private List<String> list;

    public FmbContent(FmbType heading, String text) {
        this.heading = heading;
        this.text = text;
    }

    public FmbContent(FmbType heading, List<String> list) {
        this.heading = heading;
        this.list = Collections.unmodifiableList(list);
    }

    public FmbType getHeading() {
        return heading;
    }

    public String getText() {
        return text;
    }

    public List<String> getList() {
        return list;
    }

}
