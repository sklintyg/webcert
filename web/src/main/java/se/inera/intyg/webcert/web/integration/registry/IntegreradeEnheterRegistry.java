/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.registry;

import java.util.List;
import java.util.Optional;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.web.controller.testability.dto.IntegreradEnhetEntryWithSchemaVersion;

public interface IntegreradeEnheterRegistry {

    void putIntegreradEnhet(IntegreradEnhetEntry entry, boolean schemaVersion1, boolean schemaVersion2);

    boolean isEnhetIntegrerad(String enhetHsaId, String intygType);

    IntegreradEnhet getIntegreradEnhet(String enhetsId);

    List<IntegreradEnhet> getAllIntegreradEnhet();

    void addIfSameVardgivareButDifferentUnits(String orgEnhetsHsaId, IntegreradEnhetEntry newEntry, String intygType);

    void deleteIntegreradEnhet(String hsaId);

    Optional<SchemaVersion> getSchemaVersion(String enhetHsaId, String intygType);

    List<IntegreradEnhetEntryWithSchemaVersion> getIntegreradeVardenheter();
}
