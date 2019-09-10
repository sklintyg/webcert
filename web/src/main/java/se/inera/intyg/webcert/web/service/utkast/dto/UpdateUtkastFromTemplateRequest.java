/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.utkast.dto;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;

public class UpdateUtkastFromTemplateRequest extends AbstractCreateCopyRequest {

    private String draftId;
    private String draftType;
    private String version;

    public UpdateUtkastFromTemplateRequest(
        String fromIntygsId,
        String fromIntygsType,
        String toIntygsId,
        String toIntygsType,
        Patient patient,
        HoSPersonal hosPerson) {

        super(fromIntygsId, toIntygsType, patient, hosPerson, fromIntygsType);
        this.draftId = toIntygsId;
        this.draftType = toIntygsType;
        this.version = version;
    }

    public String getDraftId() {
        return draftId;
    }

    public String getDraftType() {
        return draftType;
    }

    public String getVersion() {
        return version;
    }
}
