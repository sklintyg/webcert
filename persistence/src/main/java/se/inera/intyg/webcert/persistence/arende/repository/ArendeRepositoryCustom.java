/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.inera.intyg.webcert.persistence.arende.model.Arende;

public interface ArendeRepositoryCustom extends ArendeFilteredRepositoryCustom {

    /**
     * Should return a list that contains an array with enhets id and the number of unhandled {@link Arende} questions for that enhet.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted fraga svar entities.
     * @param intygsTyper Set of intygstyper that arendens related intyg must be of.
     * @return A list that contains an array with enhets id and the number of unhandled for that enhet.
     */
    @Query("SELECT DISTINCT a.enhetId, count(a.enhetId) FROM Arende a WHERE a.enhetId IN (:idList) AND a.status <> 'CLOSED' AND a.amne <> 'PAMINN' AND a.svarPaId = null AND a.intygTyp IN (:intygsTyper) GROUP BY a.enhetId")
    List<Object[]> countUnhandledGroupedByEnhetIdsAndIntygstyper(@Param("idList") List<String> enhetsIds, @Param("intygsTyper") Set<String> intygsTyper);

    /**
     * List all unique signing doctors for the supplied units.
     *
     * @return a list of names
     */
    @Query("SELECT DISTINCT signeratAv, signeratAvName FROM Arende WHERE enhetId IN (:idList) ORDER BY signeratAv ASC")
    List<Object[]> findSigneratAvByEnhet(@Param("idList") List<String> enhetsIds);

    /**
     * List {@link Arende} entities in the repository with an enhet matching one of the
     * supplied list of id's, that are not of status
     * {@link se.inera.intyg.webcert.persistence.fragasvar.model.Status.CLOSED}. The result is NOT ordered.
     *
     * @return A list of {@link Arende} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    @Query("SELECT a FROM Arende AS a WHERE a.enhetId IN (:idList) AND a.status <> 'CLOSED'")
    List<Arende> findByEnhet(@Param("idList") List<String> enhetsIds);

    /**
     * Should return a list of {@link Arende} entities in the repository related to the specified intygsId.
     *
     * @param intygsId
     * @return A list of {@link Arende} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    List<Arende> findByIntygsId(String intygsId);

    /**
     * Returns the {@link Arende} with the sought for meddelandeId.
     * @param meddelandeId the meddelandeId which specifies the {@link Arende}
     * @return the {@link Arende}
     */
    Arende findOneByMeddelandeId(String meddelandeId);

    /**
     * Returns all answers to the {@link Arende} of the given meddelandeId.
     *
     * @param svarPaId the meddelandeId to find answers to
     * @return a list of {@link Arende} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    List<Arende> findBySvarPaId(String svarPaId);

    /**
     * Returns all reminders to the {@link Arende} of the given meddelandeId.
     *
     * @param paminnelseMeddelandeId the meddelandeId to find reminders to
     * @return a list of {@link Arende} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    List<Arende> findByPaminnelseMeddelandeId(String paminnelseMeddelandeId);

}
