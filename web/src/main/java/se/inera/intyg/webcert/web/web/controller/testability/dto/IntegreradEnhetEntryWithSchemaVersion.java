/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.testability.dto;

import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;

/**
 * DTO for testability purposes, conveniently listing djupintegrerade vårdenheter entries.
 *
 * @author eriklupander
 */
public class IntegreradEnhetEntryWithSchemaVersion extends IntegreradEnhetEntry {

    private String schemaVersion;

    public IntegreradEnhetEntryWithSchemaVersion() {
        super();
    }

    public IntegreradEnhetEntryWithSchemaVersion(IntegreradEnhet integreradEnhet) {
        super(integreradEnhet.getEnhetsId(), integreradEnhet.getEnhetsNamn(), integreradEnhet.getVardgivarId(),
                integreradEnhet.getVardgivarNamn());
        this.schemaVersion = integreradEnhet.isSchemaVersion3() ? "2.0" : "1.0";
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
}
