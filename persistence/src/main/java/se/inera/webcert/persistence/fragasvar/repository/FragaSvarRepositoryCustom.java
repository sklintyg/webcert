package se.inera.webcert.persistence.fragasvar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

public interface FragaSvarRepositoryCustom {
    /**
     * Should return a list of {@link FragaSvar} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's.
     * 
     * @param enhetsIds
     * @return A list of {@link FragaSvar} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    @Query("SELECT fs FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList)")
    public List<FragaSvar> findByEnhetsId(@Param("idList") List<String> enhetsIds);

    /**
     * Should return a list of {@link FragaSvar} entities in the repository related to the specified intygsId.
     * 
     * @param intygsId
     * @return A list of {@link FragaSvar} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    public List<FragaSvar> findByIntygsReferensIntygsId(String intygsId);

}
