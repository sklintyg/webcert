/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.privatlakaravtal.repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * Created by eriklupander on 2015-08-05.
 */
public class AvtalRepositoryImpl implements AvtalRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Integer getLatestAvtalVersion() {
        try {
            Integer latestAvtalVersion = entityManager.createQuery("SELECT MAX(a.avtalVersion) FROM Avtal a", Integer.class)
                    .getSingleResult();
            return latestAvtalVersion == null ? -1 : latestAvtalVersion;
        } catch (NoResultException e) {
            return -1;
        }
    }
}
