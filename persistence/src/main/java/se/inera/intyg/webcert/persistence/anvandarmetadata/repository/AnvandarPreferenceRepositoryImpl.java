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
package se.inera.intyg.webcert.persistence.anvandarmetadata.repository;

import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by eriklupander on 2015-08-05.
 */
public class AnvandarPreferenceRepositoryImpl implements AnvandarPreferenceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, String> getAnvandarPreference(String hsaId) {
        List<AnvandarPreference> anvandarMetadataList = entityManager.createQuery("SELECT am FROM AnvandarPreference am WHERE am.hsaId = :hsaId", AnvandarPreference.class)
                .setParameter("hsaId", hsaId)
                .getResultList();

        Map<String, String> map = new HashMap<>();
        for (AnvandarPreference am : anvandarMetadataList) {
            map.put(am.getKey(), am.getValue());
        }
        return map;
    }

    @Override
    public boolean exists(String hsaId, String key) {
        Number number = entityManager.createQuery("SELECT COUNT(am) FROM AnvandarPreference am WHERE am.hsaId = :hsaId AND am.key = :key", Number.class)
                .setParameter("hsaId", hsaId)
                .setParameter("key", key)
                .getSingleResult();

        return number.longValue() > 0L;
    }

    @Override
    public AnvandarPreference findByHsaIdAndKey(String hsaId, String key) {
        try {
            return entityManager.createQuery("SELECT am FROM AnvandarPreference am WHERE am.hsaId = :hsaId AND am.key = :key", AnvandarPreference.class)
                    .setParameter("hsaId", hsaId)
                    .setParameter("key", key)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (NonUniqueResultException nure) {
            throw new IllegalStateException("Query for AnvandarPreference returned multiple records, should never occur. hsaId: " + hsaId + ", key: " + key);
        }
    }
}
