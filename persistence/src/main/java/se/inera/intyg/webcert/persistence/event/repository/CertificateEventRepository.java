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
package se.inera.intyg.webcert.persistence.event.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;

public interface CertificateEventRepository extends JpaRepository<CertificateEvent, Long> {

    List<CertificateEvent> findByCertificateId(String certificateId);

    @Query("select ce from CertificateEvent ce where ce.certificateId in :certificateIds")
    List<CertificateEvent> getCertificateEventsByCertificateIds(@Param("certificateIds") List<String> certificateIds);

    default int eraseCertificateEventsByCertificateIds(List<String> certificateIds) {
        final var eventsProcessed = getCertificateEventsByCertificateIds(certificateIds);
        deleteAll(eventsProcessed);
        return eventsProcessed.size();
    }
}