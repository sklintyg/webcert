/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.utkast.repository;

import com.google.common.base.Strings;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtkastRepositoryImpl implements UtkastFilteredRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public List<Utkast> filterIntyg(UtkastFilter filter, Set<String> authorizedIntygstyper) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Utkast> cq = cb.createQuery(Utkast.class);
        Root<Utkast> root = cq.from(Utkast.class);

        cq.where(createPredicate(filter, cb, root, authorizedIntygstyper));
        cq.orderBy(cb.desc(root.get("senastSparadDatum")));

        TypedQuery<Utkast> query = entityManager.createQuery(cq);

        if (filter.hasPageSizeAndStartFrom()) {
            query.setMaxResults(filter.getPageSize());
            query.setFirstResult(filter.getStartFrom());
        }

        return query.getResultList();
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public int countFilterIntyg(UtkastFilter filter, Set<String> authorizedIntygstyper) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Utkast> root = cq.from(Utkast.class);
        cq.select(cb.count(root));

        cq.where(createPredicate(filter, cb, root, authorizedIntygstyper));

        Query query = entityManager.createQuery(cq);

        return ((Long) query.getSingleResult()).intValue();
    }

    private Predicate createPredicate(UtkastFilter filter, CriteriaBuilder builder, Root<Utkast> root, Set<String> authorizedIntygstyper) {

        Predicate pred = builder.conjunction();
        pred = builder.and(pred, builder.equal(root.get("enhetsId"), filter.getUnitHsaId()));

        if (!Strings.isNullOrEmpty(filter.getSavedByHsaId())) {
            pred = builder.and(pred, builder.equal(root.get("senastSparadAv").get("hsaId"), filter.getSavedByHsaId()));
        }

        if (!filter.getStatusList().isEmpty()) {
            pred = builder.and(pred, root.<UtkastStatus>get("status").in(filter.getStatusList()));
        }

        if (filter.getNotified() != null) {
            pred = builder.and(pred, builder.equal(root.<Boolean>get("vidarebefordrad"), filter.getNotified()));
        }

        if (filter.getSavedFrom() != null) {
            pred = builder.and(pred, builder.greaterThanOrEqualTo(root.<LocalDateTime>get("senastSparadDatum"), filter.getSavedFrom()));
        }

        if (filter.getSavedTo() != null) {
            pred = builder.and(pred, builder.lessThanOrEqualTo(root.<LocalDateTime>get("senastSparadDatum"), filter.getSavedTo()));
        }

        pred = builder.and(pred, root.<String>get("intygsTyp").in(authorizedIntygstyper != null ? authorizedIntygstyper : new HashSet<>()));
        return pred;
    }

}
