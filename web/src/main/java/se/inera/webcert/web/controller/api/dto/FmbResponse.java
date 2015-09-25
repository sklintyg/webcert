package se.inera.webcert.web.controller.api.dto;

import java.util.Collections;
import java.util.List;

public class FmbResponse {

    private String icd10Code;
    private List<FmbForm> forms;

    public FmbResponse(String icd10Code, List<FmbForm> forms) {
        this.icd10Code = icd10Code;
        this.forms = Collections.unmodifiableList(forms);
    }

    public String getIcd10Code() {
        return icd10Code;
    }

    public List<FmbForm> getForms() {
        return forms;
    }

}
