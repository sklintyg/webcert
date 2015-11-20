package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.util.Collections;
import java.util.List;

public class FmbForm {

    private FmbFormName name;
    private List<FmbContent> content;

    public FmbForm(FmbFormName name, List<FmbContent> content) {
        this.name = name;
        this.content = Collections.unmodifiableList(content);
    }

    public FmbFormName getName() {
        return name;
    }

    public List<FmbContent> getContent() {
        return content;
    }

}
