package se.inera.webcert.persistence.fragasvar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

public interface FragaSvarRepositoryCustom extends FragaSvarFilteredRepositoryCustom{
    /**
     * Should return a list of {@link FragaSvar} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with {@link se.inera.webcert.persistence.fragasvar.model.Status.CLOSED}. The result is NOT ordered.
     *
     * @param enhetsIds
     * @return A list of {@link FragaSvar} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    @Query("SELECT fs FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList) AND fs.status <> 'CLOSED'")
    List<FragaSvar> findByEnhetsId(@Param("idList") List<String> enhetsIds);


    /**
     * Should return a list of {@link FragaSvar} entities in the repository related to the specified intygsId.
     *
     * @param intygsId
     * @return A list of {@link FragaSvar} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    List<FragaSvar> findByIntygsReferensIntygsId(String intygsId);

    /**
     * Should return a {@link FragaSvar} matching the search criteria.
     * @param externReferens
     * @return
     */
    FragaSvar findByExternReferens(String externReferens);

}
