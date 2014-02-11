package se.inera.webcert.modules.api.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class CreateNewIntygModuleResponse {

    private String intygsId;

    private String intygsTyp;

    @JsonRawValue
    private String contents;

    public CreateNewIntygModuleResponse() {

    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getIntygsTyp() {
        return intygsTyp;
    }

    public void setIntygsTyp(String intygsTyp) {
        this.intygsTyp = intygsTyp;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
    
}
