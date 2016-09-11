/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

    @ApiModelProperty(name = "forms")
    private List<FmbForm> forms;

    public FmbResponse(String icd10Code, String icd10Description, List<FmbForm> forms) {
        this.icd10Code = icd10Code;
        this.icd10Description = icd10Description;
        this.forms = Collections.unmodifiableList(forms);
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
}
