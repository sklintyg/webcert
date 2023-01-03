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
package se.inera.intyg.webcert.persistence.fragasvar.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus;

// CHECKSTYLE:OFF LineLength
public interface FragaSvarRepositoryCustom extends FragaSvarFilteredRepositoryCustom {

    /**
     * Should return a list of {@link FragaSvar} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with
     * {@link se.inera.intyg.webcert.persistence.fragasvar.model.Status.CLOSED}. The result is NOT ordered.
     *
     * @return A list of {@link FragaSvar} matching the search criteria. If no entities are found, this method returns
     * an empty list.
     */
    @Query("SELECT fs FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList) AND fs.status <> 'CLOSED'")
    List<FragaSvar> findByEnhetsId(@Param("idList") List<String> enhetsIds);

    /**
     * Should return a count of {@link FragaSvar} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with
     * {@link se.inera.intyg.webcert.persistence.fragasvar.model.Status.CLOSED}.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted fraga svar entities
     * @return A count of {@link FragaSvar} matching the search criteria.
     */
    @Query("SELECT count(fs) FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList) AND fs.status <> 'CLOSED'")
    Long countUnhandledForEnhetsIds(@Param("idList") List<String> enhetsIds);

    /**
     * Should return a list that contains GroupableItems for the given parameters for later aggregation into number of
     * items per unit.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted fraga svar entities.
     * @return A list that contains an array with fragasvar represented by id, enhets id and personnummer.
     */
    @Query("SELECT new se.inera.intyg.webcert.common.model.GroupableItem(fs.internReferens, fs.vardperson.enhetsId, "
        + "fs.intygsReferens.patientId, fs.intygsReferens.intygsTyp) FROM FragaSvar fs "
        + "WHERE fs.vardperson.enhetsId IN (:idList) AND fs.status <> 'CLOSED' AND fs.intygsReferens.intygsTyp IN (:intygsTyper)")
    List<GroupableItem> getUnhandledWithEnhetIdsAndIntygstyper(@Param("idList") List<String> enhetsIds,
        @Param("intygsTyper") Set<String> intygsTyper);

    /**
     * Returns a list of all unique hsaId and name (of vardperson who signed the certificate the FragaSvar is linked to)
     * where matches the supplied id.
     *
     * @return A list of Object[] where the first [0] value is the HsaId and the second [1] is the name
     */
    @Query("SELECT DISTINCT fs.vardperson.hsaId, fs.vardperson.namn FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList) "
        + "ORDER BY fs.vardperson.namn ASC")
    List<Object[]> findDistinctFragaSvarHsaIdByEnhet(@Param("idList") List<String> enhetsIds);

    /**
     * Returns a list of FragaSvarStatus object for all FragaSvar belonging to an intyg.
     */
    @Query("SELECT NEW se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus(fs.internReferens, fs.frageStallare, "
        + "fs.svarsText, fs.status) FROM FragaSvar fs WHERE fs.intygsReferens.intygsId = :intygsId")
    List<FragaSvarStatus> findFragaSvarStatusesForIntyg(@Param("intygsId") String intygsId);

    /**
     * Should return a list of {@link FragaSvar} entities in the repository related to the specified intygsId.
     *
     * @return A list of {@link FragaSvar} matching the search criteria. If no entities are found, this method returns
     * an empty list.
     */
    List<FragaSvar> findByIntygsReferensIntygsId(String intygsId);

    /**
     * Should return a {@link FragaSvar} matching the search criteria.
     */
    FragaSvar findByExternReferens(String externReferens);

    /**
     * Should return a {@link FragaSvar} matching the search criteria.
     */
    List<FragaSvar> findByExternReferensLike(String externReferens);

    /**
     * Should return a {@link FragaSvar} matching the search criteria.
     */
    List<FragaSvar> findByFrageTextLike(String frageText);

    /**
     * Should return a {@link FragaSvar} matching the search criteria.
     */
    List<FragaSvar> findBySvarsTextLike(String svarsText);
}
