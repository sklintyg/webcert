package se.inera.webcert.persistence.intyg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;

public interface IntygRepositoryCustom extends IntygFilteredRepositoryCustom {
    /**
     * Should return a list of {@link Intyg} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with a {@link IntygsStatus} not in the list. The result is NOT ordered.
     *
     * @param enhetsIds
     * @return A list of {@link Intyg} matching the search criteria. If no entities are found, this method returns
     * an empty list.
     */
    @Query("SELECT i from Intyg i WHERE i.enhetsId IN (:enhetsIds) AND i.status IN (:statuses)")
    List<Intyg> findByEnhetsIdsAndStatuses(@Param("enhetsIds") List<String> enhetsIds, @Param("statuses") List<IntygsStatus> statuses);

    /**
     * Should return a count of {@link Intyg} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with a {@link IntygsStatus} not in the list.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted intyg entities
     * @return A count of {@link Intyg} matching the search criteria.
     */
    @Query("SELECT i.enhetsId, count(i) FROM Intyg i WHERE i.enhetsId IN (:enhetsIds) AND i.status IN (:statuses) GROUP BY i.enhetsId")
    List<Object[]> countIntygWithStatusesGroupedByEnhetsId(@Param("enhetsIds") List<String> enhetsIds, @Param("statuses") List<IntygsStatus> statuses);

    /**
     * Returns all {@link Intyg} entities belonging to a certain patient and belonging to one of several careUnit and having selected statuses.
     *
     * @param patientPnr
     * @param enhetsIds
     * @param statuses
     * @return
     */
    @Query("SELECT i from Intyg i WHERE i.patientPersonnummer = :patientPnr AND i.enhetsId IN (:enhetsIds) AND i.status IN (:statuses)")
    List<Intyg> findDraftsByPatientAndEnhetAndStatus(@Param("patientPnr") String patientPnr, @Param("enhetsIds") List<String> enhetsIds, @Param("statuses") List<IntygsStatus> statuses);

    /**
     * Returns a list of all unique hsaId and name (of vardperson who edited the certificate) which matches the supplied enhetsId.
     *
     * @param enhetsid
     * @return A list of Object[] where the first [0] value is the HsaId and the second [1] is the name
     */
    @Query("SELECT DISTINCT i.senastSparadAv.hsaId, i.senastSparadAv.namn FROM Intyg i "
            + "WHERE i.enhetsId = :enhetsid AND i.status IN (:statuses) "
            + "ORDER BY i.senastSparadAv.namn ASC")
    List<Object[]> findDistinctLakareFromIntygEnhetAndStatuses(@Param("enhetsid") String enhetsid, @Param("statuses") List<IntygsStatus> statuses);

    /**
     * Return the status of a draft
     * @param intygsId
     * @return
     */
    @Query("SELECT i.status from Intyg i WHERE i.intygsId = :intygsId")
    IntygsStatus getIntygsStatus(@Param("intygsId") String intygsId);
}
