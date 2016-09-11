/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.persistence.arende.repository;

import java.util.List;

import javax.persistence.*;
import javax.persistence.criteria.*;

import org.apache.commons.lang.StringUtils;

import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;

public class ArendeRepositoryImpl implements ArendeFilteredRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Arende> filterArende(Filter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Arende> cq = builder.createQuery(Arende.class);

        Root<Arende> root = cq.from(Arende.class);

        cq.where(createPredicate(filter, builder, root));
        cq.orderBy(builder.desc(root.get("senasteHandelse")));

        TypedQuery<Arende> query = entityManager.createQuery(cq);

        if (filter.hasPageSizeAndStartFrom()) {
            query.setMaxResults(filter.getPageSize());
            query.setFirstResult(filter.getStartFrom());
        }

        return query.getResultList();
    }

    @Override
    public int filterArendeCount(Filter filter) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Arende> root = cq.from(Arende.class);
        cq.select(cb.count(root));

        cq.where(createPredicate(filter, cb, root));

        Query query = entityManager.createQuery(cq);

        return ((Long) query.getSingleResult()).intValue();
    }


    private Predicate createPredicate(Filter filter, CriteriaBuilder builder, Root<Arende> root) {
        Predicate pred = builder.conjunction();

        pred = builder.and(pred, root.get("enhetId").in(filter.getEnhetsIds()));
        pred = builder.and(pred, root.get("intygTyp").in(filter.getIntygsTyper()));
        pred = builder.and(pred, root.get("paminnelseMeddelandeId").isNull());
        pred = builder.and(pred, root.get("svarPaId").isNull());

        if (filter.isQuestionFromFK()) {
            pred = builder.and(pred, builder.equal(root.get("skickatAv"), "FK"));
        }

        if (filter.isQuestionFromWC()) {
            pred = builder.and(pred, builder.equal(root.get("skickatAv"), "WC"));
        }

        if (StringUtils.isNotEmpty(filter.getHsaId())) {
            pred = builder.and(pred, builder.equal(root.get("signeratAv"), filter.getHsaId()));
        }

        if (filter.getVidarebefordrad() != null) {
            pred = builder.and(pred, builder.equal(root.get("vidarebefordrad"), filter.getVidarebefordrad()));
        }

        if (filter.getChangedFrom() != null) {
            pred = builder.and(pred, builder.greaterThanOrEqualTo(root.get("senasteHandelse"), filter.getChangedFrom()));
        }

        if (filter.getChangedTo() != null) {
            pred = builder.and(pred, builder.lessThan(root.get("senasteHandelse"), filter.getChangedTo()));
        }

        if (filter.getReplyLatest() != null) {
            pred = builder.and(pred, builder.lessThanOrEqualTo(root.get("sistaDatumForSvar"), filter.getReplyLatest()));
        }

        switch (filter.getVantarPa()) {
            case ALLA_OHANTERADE:
                pred = builder.and(pred, builder.notEqual(root.get("status"), Status.CLOSED));
                break;
            case HANTERAD:
                pred = builder.and(pred, builder.equal(root.get("status"), Status.CLOSED));
                break;
            case KOMPLETTERING_FRAN_VARDEN:
                pred = builder.and(pred, builder.equal(root.get("status"), Status.PENDING_INTERNAL_ACTION),
                        builder.equal(root.get("amne"), ArendeAmne.KOMPLT));
                break;
            case SVAR_FRAN_VARDEN:
                Predicate careReplyAmnePred = builder.or(builder.equal(root.get("amne"), ArendeAmne.OVRIGT),
                        builder.equal(root.get("amne"), ArendeAmne.ARBTID), builder.equal(root.get("amne"), ArendeAmne.AVSTMN),
                        builder.equal(root.get("amne"), ArendeAmne.KONTKT));
                pred = builder.and(pred, builder.equal(root.get("status"), Status.PENDING_INTERNAL_ACTION), careReplyAmnePred);
                break;
            case SVAR_FRAN_FK:
                pred = builder.and(pred, builder.equal(root.get("status"), Status.PENDING_EXTERNAL_ACTION));
                break;
            case MARKERA_SOM_HANTERAD:
                Predicate amnePred;
                amnePred = builder.and(builder.equal(root.get("status"), Status.PENDING_INTERNAL_ACTION),
                        builder.equal(root.get("amne"), ArendeAmne.PAMINN));

                pred = builder.and(pred, builder.or(amnePred, builder.equal(root.get("status"), Status.ANSWERED)));
                break;
            case ALLA:
            default:
                break;
        }
        return pred;
    }
}
