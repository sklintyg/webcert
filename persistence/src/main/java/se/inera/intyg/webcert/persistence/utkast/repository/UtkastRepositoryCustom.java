/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.utkast.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

// CHECKSTYLE:OFF LineLength
public interface UtkastRepositoryCustom extends UtkastFilteredRepositoryCustom {

    /**
     * Should only be used for testability!
     */
    @Query("SELECT DISTINCT(u.enhetsId), u.enhetsNamn FROM Utkast u WHERE u.skickadTillMottagareDatum != null")
    List<Object[]> findAllUnitsWithSentCertificate();

    /**
     * Should only be used for testability!
     *
     * Should return a list of {@link Utkast} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with a {@link UtkastStatus} not in the list. The result is NOT
     * ordered.
     *
     * @return A list of {@link Utkast} matching the search criteria. If no entities are found, this method returns
     * an empty list.
     */
    @Query("SELECT u from Utkast u WHERE u.enhetsId IN (:enhetsIds) AND u.status IN (:statuses)")
    List<Utkast> findByEnhetsIdsAndStatuses(@Param("enhetsIds") List<String> enhetsIds, @Param("statuses") List<UtkastStatus> statuses);

    /**
     * Should return a list of {@link Utkast} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with a {@link UtkastStatus} not in the list.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted intyg entities
     */
    @Query("SELECT new se.inera.intyg.webcert.common.model.GroupableItem(u.intygsId, u.enhetsId, u.patientPersonnummer, u.intygsTyp) FROM Utkast u WHERE u.aterkalladDatum IS NULL AND u.enhetsId IN (:enhetsIds) AND u.status IN (:statuses) AND u.intygsTyp IN (:intygsTyper)")
    List<GroupableItem> getIntygWithStatusesByEnhetsId(@Param("enhetsIds") List<String> enhetsIds,
        @Param("statuses") Set<UtkastStatus> statuses, @Param("intygsTyper") Set<String> intygsTyper);

    /**
     * Returns all {@link Utkast} entities belonging to a certain patient and belonging to one of several careUnit and
     * having selected statuses.
     */
    @Query("SELECT u from Utkast u WHERE u.patientPersonnummer = :patientPnr AND u.enhetsId IN (:enhetsIds) AND u.status IN (:statuses) AND u.intygsTyp IN (:intygsTyper)")
    List<Utkast> findDraftsByPatientAndEnhetAndStatus(@Param("patientPnr") String patientPnr, @Param("enhetsIds") List<String> enhetsIds,
        @Param("statuses") List<UtkastStatus> statuses, @Param("intygsTyper") Set<String> intygsTyper);

    /**
     * Returns all {@link Utkast} entities belonging to a certain patient and belonging to a caregiver and having
     * selected statuses.
     */
    @Query("SELECT u from Utkast u WHERE u.patientPersonnummer = :patientPnr AND u.vardgivarId = :vardgivarId AND u.status IN (:statuses) AND u.intygsTyp IN (:intygsTyper)")
    List<Utkast> findDraftsByPatientAndVardgivareAndStatus(@Param("patientPnr") String patientPnr, @Param("vardgivarId") String vardgivarId,
        @Param("statuses") List<UtkastStatus> statuses, @Param("intygsTyper") Set<String> intygsTyper);


    /**
     * Returns all {@link Utkast} entities with status not DRAFT_LOCKED or SIGNED that were created before skapad.
     */
    @Query("SELECT u from Utkast u WHERE u.status NOT IN(se.inera.intyg.common.support.model.UtkastStatus.DRAFT_LOCKED, se.inera.intyg.common.support.model.UtkastStatus.SIGNED) AND u.skapad <= :skapad")
    List<Utkast> findDraftsByNotLockedOrSignedAndSkapadBefore(@Param("skapad") LocalDateTime skapad);

    /**
     * Remove all relations to Utkast with id intygsId.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Utkast u SET u.relationIntygsId = null, u.relationKod = null WHERE u.relationIntygsId = :intygsId")
    void removeRelationsToDraft(@Param("intygsId") String intygsId);

    /**
     * Returns a list of all unique hsaId and name (of vardperson who edited the draft) which matches the supplied
     * enhetsId.
     *
     * @return A list of Object[] where the first [0] value is the HsaId and the second [1] is the name
     */
    @Query("SELECT DISTINCT u.senastSparadAv.hsaId, u.senastSparadAv.namn FROM Utkast u "
        + "WHERE u.aterkalladDatum IS NULL AND u.enhetsId = :enhetsid AND u.status IN (:statuses) "
        + "ORDER BY u.senastSparadAv.namn ASC")
    List<Object[]> findDistinctLakareFromIntygEnhetAndStatuses(@Param("enhetsid") String enhetsid,
        @Param("statuses") Set<UtkastStatus> statuses);

    /**
     * Return the status of a draft.
     */
    @Query("SELECT u.status from Utkast u WHERE u.intygsId = :intygsId")
    UtkastStatus getIntygsStatus(@Param("intygsId") String intygsId);

    @Query("SELECT u.intygsTyp from Utkast u WHERE u.intygsId = :intygsId")
    String getIntygsTyp(@Param("intygsId") String intygsId);

    /**
     * Return the HSA-ID for the Enhet on which the Utkast was created.
     */
    @Query("SELECT u.enhetsId from Utkast u WHERE u.intygsId = :intygsId")
    String getIntygsVardenhetsHsaId(@Param("intygsId") String intygsId);

    /**
     * Returns (if applicable) the parent relation of the specified intygsId.
     */
    @Query("SELECT new se.inera.intyg.webcert.common.model.WebcertCertificateRelation(u.relationIntygsId, u.relationKod, u.senastSparadDatum, u2.status, u2.aterkalladDatum IS NOT NULL) FROM Utkast u, Utkast u2 WHERE u2.intygsId = u.relationIntygsId AND u.intygsId = :intygsId")
    List<WebcertCertificateRelation> findParentRelation(@Param("intygsId") String intygsId);

    /**
     * Returns the parent relation even if the parent doesn't exists in the database e.g. it only exists in Intygstjanst.
     */
    @Query("SELECT new se.inera.intyg.webcert.common.model.WebcertCertificateRelation(relationIntygsId, relationKod, senastSparadDatum) FROM Utkast WHERE intygsId = :intygsId AND relationIntygsId IS NOT NULL")
    List<WebcertCertificateRelation> findParentRelationWhenParentMissing(@Param("intygsId") String intygsId);

    /**
     * Returns 0..n child relations of the specified intygsId.
     */
    @Query("SELECT new se.inera.intyg.webcert.common.model.WebcertCertificateRelation(u.intygsId, u.relationKod, u.senastSparadDatum, u.status, u.aterkalladDatum IS NOT NULL) FROM Utkast u WHERE u.relationIntygsId = :intygsId ORDER BY u.senastSparadDatum DESC")
    List<WebcertCertificateRelation> findChildRelations(@Param("intygsId") String intygsId);

    /**
     * Returns any existing test certificate´s ids.
     */
    @Query("SELECT u.intygsId from Utkast u WHERE u.isTestIntyg = true")
    List<String> findTestCertificates();

    /**
     * Returns the ids of any test certificates created before the specified datetime.
     */
    @Query("SELECT u.intygsId from Utkast u WHERE u.isTestIntyg = true AND u.skapad <= :createdBefore")
    List<String> findTestCertificatesByCreatedBefore(@Param("createdBefore") LocalDateTime createdBefore);

    /**
     * Returns the ids of any test certificates created after the specified datetime.
     */
    @Query("SELECT u.intygsId from Utkast u WHERE u.isTestIntyg = true AND u.skapad >= :createdAfter")
    List<String> findTestCertificatesByCreatedAfter(@Param("createdAfter") LocalDateTime createdAfter);

    /**
     * Returns the ids of any test certificates created between the specified datetimes.
     */
    @Query("SELECT u.intygsId from Utkast u WHERE u.isTestIntyg = true AND u.skapad <= :createdBefore AND u.skapad >= :createdAfter")
    List<String> findTestCertificatesByCreatedBeforeAndAfter(@Param("createdAfter") LocalDateTime createdAfter,
        @Param("createdBefore") LocalDateTime createdBefore);

    /**
     * Returns the ids of certificates without events that are not being processed.
     */
    @Query(value = "SELECT u.INTYGS_ID FROM INTYG u "
        + "WHERE u.INTYGS_ID NOT IN ("
        + "SELECT ce.CERTIFICATE_ID FROM CERTIFICATE_EVENT ce WHERE ce.CERTIFICATE_ID = u.INTYGS_ID "
        + "UNION ALL "
        + "SELECT cep.CERTIFICATE_ID FROM CERTIFICATE_EVENT_PROCESSED cep WHERE cep.CERTIFICATE_ID = u.INTYGS_ID "
        + "UNION ALL "
        + "SELECT cel.CERTIFICATE_ID FROM CERTIFICATE_EVENT_FAILED_LOAD cel WHERE cel.CERTIFICATE_ID = u.INTYGS_ID ) "
        + "LIMIT :batchSize",
        nativeQuery = true)
    List<String> findCertificatesWithoutEvents(@Param("batchSize") Integer batchSize);
}
