package se.inera.webcert.persistence.intyg.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;

public class IntygRepositoryImpl implements IntygFilteredRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Intyg> filterIntyg(IntygFilter filter) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Intyg> query = builder.createQuery(Intyg.class);
        Root<Intyg> root = query.from(Intyg.class);
        
        query.where(createPredicate(filter, builder, root));
        
        return entityManager.createQuery(query).getResultList();
    }

    private Predicate createPredicate(IntygFilter filter, CriteriaBuilder builder, Root<Intyg> root) {
        
        Predicate pred = builder.conjunction();
        
        pred = builder.and(pred, builder.equal(root.get("enhetsId"), filter.getUnitHsaId()));
        
        if (StringUtils.isNotEmpty(filter.getSavedByHsaId())) {
            pred = builder.and(pred, builder.equal(root.get("senastSparadAv").get("hsaId"), filter.getSavedByHsaId()));
        }
        
        if (!filter.getStatusList().isEmpty()) {
            pred = builder.and(pred, root.<IntygsStatus>get("status").in(filter.getStatusList()));
        }
        
        if (filter.getVidarebefordrad() != null) {
            pred = builder.and(pred, builder.equal(root.<Boolean>get("vidarebefordrad"), filter.getVidarebefordrad())) ;
        }
        
        if (filter.getChangedFrom() != null) {
            pred = builder.and(pred, builder.greaterThanOrEqualTo(root.<LocalDate>get("senastSparadDatum"), filter.getChangedFrom())) ;
        }

        if (filter.getChangedTo() != null) {
            pred = builder.and(pred, builder.lessThanOrEqualTo(root.<LocalDate>get("senastSparadDatum"), filter.getChangedTo())) ;
        }
        
        return pred;
    }

}
