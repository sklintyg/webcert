package se.inera.webcert.persistence.intyg.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;

public class IntygRepositoryImpl implements IntygFilteredRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Intyg> filterIntyg(IntygFilter filter) {
       
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Intyg> cq = cb.createQuery(Intyg.class);
        Root<Intyg> root = cq.from(Intyg.class);
        
        cq.where(createPredicate(filter, cb, root));
        cq.orderBy(cb.desc(root.get("senastSparadDatum")));
        
        TypedQuery<Intyg> query = entityManager.createQuery(cq);

        if (filter.hasPageSizeAndStartFrom()) {
            query.setMaxResults(filter.getPageSize());
            query.setFirstResult(filter.getStartFrom());
        }
                
        return query.getResultList();
    }

    public int countFilterIntyg(IntygFilter filter) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Intyg> root = cq.from(Intyg.class);
        cq.select(cb.count(root));

        cq.where(createPredicate(filter, cb, root));

        Query query = entityManager.createQuery(cq);

        return ((Long) query.getSingleResult()).intValue();
    }

    private Predicate createPredicate(IntygFilter filter, CriteriaBuilder builder, Root<Intyg> root) {

        Predicate pred = builder.conjunction();

        pred = builder.and(pred, builder.equal(root.get("enhetsId"), filter.getUnitHsaId()));

        if (StringUtils.isNotEmpty(filter.getSavedByHsaId())) {
            pred = builder.and(pred, builder.equal(root.get("senastSparadAv").get("hsaId"), filter.getSavedByHsaId()));
        }

        if (!filter.getStatusList().isEmpty()) {
            pred = builder.and(pred, root.<IntygsStatus> get("status").in(filter.getStatusList()));
        }

        if (filter.getForwarded() != null) {
            pred = builder.and(pred, builder.equal(root.<Boolean> get("vidarebefordrad"), filter.getForwarded()));
        }

        if (filter.getSavedFrom() != null) {
            pred = builder.and(pred,
                    builder.greaterThanOrEqualTo(root.<LocalDate> get("senastSparadDatum"), filter.getSavedFrom()));
        }

        if (filter.getSavedTo() != null) {
            pred = builder.and(pred,
                    builder.lessThanOrEqualTo(root.<LocalDate> get("senastSparadDatum"), filter.getSavedTo()));
        }

        return pred;
    }

}
