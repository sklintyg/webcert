package se.inera.webcert.persistence.intyg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;

public interface IntygRepositoryCustom {
    /**
     * Should return a list of {@link Intyg} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with {@link IntygsStatus.SIGNED}. The result is NOT ordered.
     *
     * @param enhetsIds
     * @return A list of {@link Intyg} matching the search criteria. If no entities are found, this method returns
     *         an empty list.
     */
    @Query("SELECT i FROM Intyg i WHERE i.enhetsId IN (:idList) AND i.status <> 'SIGNED'")
    List<Intyg> findUnsignedByEnhetsId(@Param("idList") List<String> enhetsIds);

    /**
     * Should return a count of {@link Intyg} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with {@link IntygsStatus.SIGNED}.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted intyg entities
     * 
     * @return A count of {@link Intyg} matching the search criteria.
     */
    @Query("SELECT count(i) FROM Intyg i WHERE i.enhetsId IN (:idList) AND i.status <> 'SIGNED'")
    Long countUnsignedForEnhetsIds(@Param("idList") List<String> enhetsIds);
    
    
    

}
