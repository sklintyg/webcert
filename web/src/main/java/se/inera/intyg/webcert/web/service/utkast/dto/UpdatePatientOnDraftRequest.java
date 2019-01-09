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

import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Created by marced on 2016-11-30.
 */
public class UpdatePatientOnDraftRequest {
    private Personnummer personnummer;
    private String draftId;
    private long version;

    public UpdatePatientOnDraftRequest(Personnummer personnummer, String draftId, long version) {
        this.personnummer = personnummer;
        this.draftId = draftId;
        this.version = version;
    }

    public Personnummer getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(Personnummer personnummer) {
        this.personnummer = personnummer;
    }

    public String getDraftId() {
        return draftId;
    }

    public void setDraftId(String draftId) {
        this.draftId = draftId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
