package se.inera.intyg.webcert.persistence.utkast.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;

@Transactional(value = "jpaTransactionManager", readOnly = true)
public interface UtkastRepositoryCustom extends UtkastFilteredRepositoryCustom {
    /**
     * Should return a list of {@link Utkast} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with a {@link UtkastStatus} not in the list. The result is NOT ordered.
     *
     * @param enhetsIds
     * @return A list of {@link Utkast} matching the search criteria. If no entities are found, this method returns
     * an empty list.
     */
    @Query("SELECT u from Utkast u WHERE u.enhetsId IN (:enhetsIds) AND u.status IN (:statuses)")
    List<Utkast> findByEnhetsIdsAndStatuses(@Param("enhetsIds") List<String> enhetsIds, @Param("statuses") List<UtkastStatus> statuses);

    /**
     * Should return a count of {@link Utkast} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with a {@link UtkastStatus} not in the list.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted intyg entities
     * @return A count of {@link Utkast} matching the search criteria.
     */
    @Query("SELECT u.enhetsId, count(u) FROM Utkast u WHERE u.enhetsId IN (:enhetsIds) AND u.status IN (:statuses) GROUP BY u.enhetsId")
    List<Object[]> countIntygWithStatusesGroupedByEnhetsId(@Param("enhetsIds") List<String> enhetsIds, @Param("statuses") List<UtkastStatus> statuses);

    /**
     * Returns all {@link Utkast} entities belonging to a certain patient and belonging to one of several careUnit and having selected statuses.
     *
     * @param patientPnr
     * @param enhetsIds
     * @param statuses
     * @return
     */
    @Query("SELECT u from Utkast u WHERE u.patientPersonnummer = :patientPnr AND u.enhetsId IN (:enhetsIds) AND u.status IN (:statuses) AND u.intygsTyp IN (:allowedIntygTypes)")
    List<Utkast> findDraftsByPatientAndEnhetAndStatus(@Param("patientPnr") String patientPnr, @Param("enhetsIds") List<String> enhetsIds, @Param("statuses") List<UtkastStatus> statuses, @Param("allowedIntygTypes") Set<String> allowedIntygTypes);

    /**
     * Returns a list of all unique hsaId and name (of vardperson who edited the draft) which matches the supplied enhetsId.
     *
     * @param enhetsid
     * @return A list of Object[] where the first [0] value is the HsaId and the second [1] is the name
     */
    @Query("SELECT DISTINCT u.senastSparadAv.hsaId, u.senastSparadAv.namn FROM Utkast u "
            + "WHERE u.enhetsId = :enhetsid AND u.status IN (:statuses) "
            + "ORDER BY u.senastSparadAv.namn ASC")
    List<Object[]> findDistinctLakareFromIntygEnhetAndStatuses(@Param("enhetsid") String enhetsid, @Param("statuses") List<UtkastStatus> statuses);

    /**
     * Return the status of a draft.
     * @param intygsId
     * @return
     */
    @Query("SELECT u.status from Utkast u WHERE u.intygsId = :intygsId")
    UtkastStatus getIntygsStatus(@Param("intygsId") String intygsId);

    /**
     * Return the HSA-ID for the Enhet on which the Utkast was created.
     * @param intygsId
     * @return
     */
    @Query("SELECT u.enhetsId from Utkast u WHERE u.intygsId = :intygsId")
    String getIntygsVardenhetsHsaId(@Param("intygsId") String intygsId);
}
