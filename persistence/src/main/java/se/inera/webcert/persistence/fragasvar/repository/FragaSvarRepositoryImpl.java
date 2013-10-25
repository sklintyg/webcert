package se.inera.webcert.persistence.fragasvar.repository;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Pageable;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Status;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
            pred = builder.and(pred, builder.greaterThanOrEqualTo(root.<LocalDate>get("senasteHandelse"), filter.getChangedFrom())) ;
        }

        if (filter.getChangedTo() != null) {
            pred = builder.and(pred, builder.lessThanOrEqualTo(root.<LocalDate>get("senasteHandelse"), filter.getChangedTo())) ;
        }
        if (filter.getReplyLatest() != null) {
            pred = builder.and(pred, builder.lessThanOrEqualTo(root.<LocalDate>get("sistaDatumForSvar"), filter.getReplyLatest())) ;
        }


        switch(filter.getVantarPa()){
            case ALLA_OHANTERADE:
                pred = builder.and(pred, builder.notEqual(root.<Status>get("status"), Status.CLOSED)) ;
                break;
            case HANTERAD:
                pred = builder.and(pred, builder.equal(root.<Status>get("status"), Status.CLOSED)) ;
                break;
            case KOMPLETTERING_FRAN_VARDEN:
                pred = builder.and(pred, builder.equal(root.<Status>get("status"), Status.PENDING_INTERNAL_ACTION), builder.equal(root.<Amne>get("amne"), Amne.KOMPLETTERING_AV_LAKARINTYG)) ;
                break;
            case SVAR_FRAN_VARDEN:
                Predicate careReplyAmnePred;
                careReplyAmnePred = builder.or(builder.equal(root.<Amne>get("amne"), Amne.OVRIGT), builder.equal(root.<Amne>get("amne"), Amne.ARBETSTIDSFORLAGGNING), builder.equal(root.<Amne>get("amne"), Amne.AVSTAMNINGSMOTE), builder.equal(root.<Amne>get("amne"), Amne.KONTAKT));

                pred = builder.and(pred, builder.equal(root.<Status>get("status"), Status.PENDING_INTERNAL_ACTION),careReplyAmnePred) ;
                break;
            case SVAR_FRAN_FK:
                pred = builder.and(pred, builder.equal(root.<Status>get("status"), Status.PENDING_EXTERNAL_ACTION), builder.notEqual(root.<Amne>get("amne"), Amne.MAKULERING_AV_LAKARINTYG)) ;

                break;
            case MARKERA_SOM_HANTERAD:
                Predicate amnePred1;
                amnePred1 = builder.and(builder.equal(root.<Status>get("status"), Status.PENDING_INTERNAL_ACTION), builder.equal(root.<Amne>get("amne"), Amne.MAKULERING_AV_LAKARINTYG) );

                Predicate amnePred2;
                amnePred2 = builder.and(builder.equal(root.<Status>get("status"), Status.PENDING_INTERNAL_ACTION), builder.equal(root.<Amne>get("amne"), Amne.PAMINNELSE)) ;

                pred = builder.and(pred, builder.or(amnePred1, amnePred2, builder.equal(root.<Status>get("status"), Status.ANSWERED))) ;
                break;
            case ALLA:
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
        query.orderBy(builder.desc(root.get("senasteHandelse")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<FragaSvar> filterFragaSvar(FragaSvarFilter filter, Pageable pages) {

        builder = entityManager.getCriteriaBuilder();
        query = builder.createQuery(FragaSvar.class);

        root = query.from(FragaSvar.class);

        query.where(createPredicate(filter));
        query.orderBy(builder.desc(root.get("senasteHandelse")));
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
