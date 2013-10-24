package se.inera.webcert.persistence.fragasvar.repository;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Pageable;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by pehr on 10/21/13.
 */
public class FragaSvarRepositoryImpl implements FragaSvarFilteredRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    CriteriaBuilder builder;
    CriteriaQuery<FragaSvar> query;
    Root<FragaSvar> root;


    private Predicate createPredicate(FragaSvarFilter filter){
        Predicate pred= builder.conjunction();

        pred = builder.and(pred, builder.equal(root.get("vardperson").get("enhetsId"), filter.getEnhetsId()));

        if (filter.isQuestionFromFK()){
            pred = builder.and(pred, builder.equal(root.get("frageStallare"), "FK"));
        }
        if (filter.isQuestionFromWC()){
            pred = builder.and(pred, builder.equal(root.get("frageStallare"), "WC"));
        }

        if(filter.getHsaId()!=null&&!filter.getHsaId().isEmpty()){
            pred = builder.and(pred, builder.equal(root.get("vardperson").get("hsaId"), filter.getHsaId()));
        }
        if (filter.getVidarebefordrad() != null) {
            pred = builder.and(pred, builder.equal(root.<Boolean>get("vidarebefordrad"), filter.getVidarebefordrad())) ;
        }
        if (filter.getChangedFrom() != null) {
            pred = builder.and(pred, builder.greaterThan(root.<LocalDate>get("senasteHandelse"), filter.getChangedFrom())) ;
        }

        if (filter.getChangedTo() != null) {
            pred = builder.and(pred, builder.lessThan(root.<LocalDate>get("senasteHandelse"), filter.getChangedTo())) ;
        }
        if (filter.getReplyLatest() != null) {
            pred = builder.and(pred, builder.lessThan(root.<LocalDate>get("sistaDatumForSvar"), filter.getReplyLatest())) ;
        }


        switch(filter.getShowStatus()){
            case ALL_OPEN:
                pred = builder.and(pred, builder.notEqual(root.<Status>get("status"), Status.CLOSED)) ;
                break;
            case CLOSED:
                pred = builder.and(pred, builder.equal(root.<Status>get("status"), Status.CLOSED)) ;
                break;
            case ADDED_DATA_FROM_CARE:
                pred = builder.and(pred, builder.equal(root.<Status>get("status"), Status.PENDING_INTERNAL_ACTION)) ;
                pred = builder.and(pred, builder.equal(root.<Amne>get("amne"), Amne.KOMPLETTERING_AV_LAKARINTYG)) ;
                break;
            case REPLY_FROM_CARE:
                Predicate careReplyAmnePred;
                careReplyAmnePred = builder.or(builder.equal(root.<Amne>get("amne"), Amne.OVRIGT), builder.equal(root.<Amne>get("amne"), Amne.ARBETSTIDSFORLAGGNING), builder.equal(root.<Amne>get("amne"), Amne.AVSTAMNINGSMOTE), builder.equal(root.<Amne>get("amne"), Amne.KONTAKT) );

                pred = builder.and(pred, builder.equal(root.<Status>get("status"), Status.PENDING_INTERNAL_ACTION),careReplyAmnePred) ;
                break;
            case REPLY_FROM_FK:
                pred = builder.and(pred, builder.equal(root.<Status>get("status"), Status.PENDING_EXTERNAL_ACTION)) ;
                pred = builder.and(pred, builder.notEqual(root.<Amne>get("amne"), Amne.MAKULERING_AV_LAKARINTYG)) ;
                break;
            case MARK_AS_HANDLED:
                Predicate amnePred1= builder.conjunction();
                amnePred1 = builder.and(amnePred1, builder.equal(root.<Amne>get("status"), Status.PENDING_EXTERNAL_ACTION), builder.equal(root.<Amne>get("amne"), Amne.MAKULERING_AV_LAKARINTYG) );

                Predicate amnePred2= builder.conjunction();
                amnePred2 = builder.and(amnePred2, builder.equal(root.<Amne>get("status"), Status.PENDING_INTERNAL_ACTION), builder.equal(root.<Amne>get("amne"), Amne.PAMINNELSE)) ;

                pred = builder.and(pred, builder.or(amnePred1, amnePred2,builder.equal(root.<Status>get("status"), Status.CLOSED) )) ;
                break;
            case ALL:
            default:
                break;
        }
        return pred;
    }

    @Override
    public List<FragaSvar> filterFragaSvar(FragaSvarFilter filter) {

        builder = entityManager.getCriteriaBuilder();
        query = builder.createQuery(FragaSvar.class);

        root = query.from(FragaSvar.class);

        query.where(createPredicate(filter));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<FragaSvar> filterFragaSvar(FragaSvarFilter filter, Pageable pages) {

        builder = entityManager.getCriteriaBuilder();
        query = builder.createQuery(FragaSvar.class);

        root = query.from(FragaSvar.class);


        query.where(createPredicate(filter));

        return entityManager.createQuery(query).setMaxResults(pages.getPageSize()).setFirstResult(pages.getPageNumber()).getResultList();
    }

    @Override
    public int filterCountFragaSvar(FragaSvarFilter filter) {
        builder = entityManager.getCriteriaBuilder();
        query = builder.createQuery(FragaSvar.class);

        root = query.from(FragaSvar.class);

        query.where(createPredicate(filter));

        return entityManager.createQuery(query).getResultList().size();
    }

}
