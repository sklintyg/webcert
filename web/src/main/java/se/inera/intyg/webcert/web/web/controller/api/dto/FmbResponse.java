package se.inera.intyg.webcert.web.web.controller.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collections;
import java.util.List;

@ApiModel(description = "An ICD10 code and its associated form data")
public class FmbResponse {

    @ApiModelProperty(name = "ICD10 code", dataType = "String")
    private String icd10Code;

    @ApiModelProperty(name = "forms")
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
