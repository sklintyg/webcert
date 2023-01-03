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
package se.inera.intyg.webcert.persistence.arende.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;

public interface ArendeDraftRepository extends CrudRepository<ArendeDraft, Long> {

    /**
     * Finds all the {@linkplain ArendeDraft} related to a certificate with id intygId.
     *
     * @param intygId The id of the certificate we are interested in.
     * @return {@linkplain List} of the drafts related to certificate
     */
    List<ArendeDraft> findByIntygId(String intygId);

    /**
     * Finds single {@linkplain ArendeDraft} with intygdId and questionId.
     */
    ArendeDraft findByIntygIdAndQuestionId(String intygId, String questionId);

    @Query("select ad from ArendeDraft ad where ad.intygId in :certificateIds")
    List<ArendeDraft> getArendeDraftsByCertificateIds(@Param("certificateIds") List<String> certificateIds);

    default int eraseArendeDraftsByCertificateIds(List<String> certificateIds) {
        final var arendeDrafts = getArendeDraftsByCertificateIds(certificateIds);
        deleteAll(arendeDrafts);
        return arendeDrafts.size();
    }
}
