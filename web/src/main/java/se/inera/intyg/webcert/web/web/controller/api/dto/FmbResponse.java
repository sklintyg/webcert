/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Collections;
import java.util.List;

@ApiModel(description = "An ICD10 code and its associated form data")
public final class FmbResponse {

    @ApiModelProperty(name = "ICD10 code", dataType = "String")
    private String icd10Code;

    @ApiModelProperty(name = "ICD10 description", dataType = "String")
    private String icd10Description;

    @ApiModelProperty(name = "FMB diagnose title", dataType = "String")
    private String diagnosTitle;

    @ApiModelProperty(name = "FMB related diagnose codes", dataType = "String")
    private String relatedDiagnoses;

    @ApiModelProperty(name = "Reference description", dataType = "String")
    private String referenceDescription;

    @ApiModelProperty(name = "Reference Link", dataType = "String")
    private String referenceLink;

    @ApiModelProperty(name = "forms")
    private List<FmbForm> forms;

    public FmbResponse() {
    }

    private FmbResponse(
        final String icd10Code,
        final String icd10Description,
        final String diagnosTitle,
        final String relatedDiagnoses,
        final String referenceDescription,
        final String referenceLink,
        final List<FmbForm> forms) {
        this.icd10Code = icd10Code;
        this.icd10Description = icd10Description;
        this.diagnosTitle = diagnosTitle;
        this.relatedDiagnoses = relatedDiagnoses;
        this.referenceDescription = referenceDescription;
        this.referenceLink = referenceLink;
        this.forms = Collections.unmodifiableList(forms);

    }

    public static FmbResponse of(
        final String icd10Code,
        final String icd10Description,
        final String diagnosTitle,
        final String relatedDiagnoses,
        final String referenceDescription,
        final String referenceLink,
        final List<FmbForm> forms) {
        return new FmbResponse(icd10Code, icd10Description, diagnosTitle, relatedDiagnoses, referenceDescription, referenceLink, forms);
    }

    public String getIcd10Code() {
        return icd10Code;
    }

    public List<FmbForm> getForms() {
        return forms;
    }

    public String getIcd10Description() {
        return icd10Description;
    }

    public String getDiagnosTitle() {
        return diagnosTitle;
    }

    public String getRelatedDiagnoses() {
        return relatedDiagnoses;
    }

    public String getReferenceDescription() {
        return referenceDescription;
    }

    public String getReferenceLink() {
        return referenceLink;
    }

}
