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

package se.inera.intyg.webcert.persistence.privatlakaravtal.repository;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.webcert.persistence.privatlakaravtal.model.GodkantAvtal;

/**
 * Created by eriklupander on 2015-08-05.
 */
public class GodkantAvtalRepositoryImpl implements GodkantAvtalRepositoryCustom {

    private static final Logger LOG = LoggerFactory.getLogger(GodkantAvtalRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void approveAvtal(String hsaId, Integer avtalVersion) {

        if (userHasApprovedAvtal(hsaId, avtalVersion)) {
            LOG.info("User with hsaId '" + hsaId + "' has already approved privatlakaravtal version '" + avtalVersion + "'");
            return;
        }

        GodkantAvtal godkantAvtal = new GodkantAvtal();
        godkantAvtal.setHsaId(hsaId);
        godkantAvtal.setAvtalVersion(avtalVersion);
        godkantAvtal.setGodkandDatum(LocalDateTime.now());
        entityManager.persist(godkantAvtal);
    }

    @Override
    public boolean userHasApprovedAvtal(String hsaId, Integer avtalVersion) {
        try {
            GodkantAvtal godkantAvtal = entityManager.createQuery("SELECT ga FROM GodkantAvtal ga WHERE ga.hsaId = :hsaId AND ga.avtalVersion = :avtalVersion", GodkantAvtal.class)
                    .setParameter("hsaId", hsaId)
                    .setParameter("avtalVersion", avtalVersion)
                    .getSingleResult();
            return godkantAvtal != null;
        } catch (NoResultException e) {
            return false;
        } catch (NonUniqueResultException e) {
            // This should never occur if we set up our constraints correctly.
            return true;
        }
    }

    @Override
    public void removeUserApprovement(String hsaId, Integer avtalVersion) {
        try {
            GodkantAvtal godkantAvtal = entityManager.createQuery("SELECT ga FROM GodkantAvtal ga WHERE ga.hsaId = :hsaId AND ga.avtalVersion = :avtalVersion", GodkantAvtal.class)
                    .setParameter("hsaId", hsaId)
                    .setParameter("avtalVersion", avtalVersion)
                    .getSingleResult();

            if (godkantAvtal != null) {
                entityManager.remove(godkantAvtal);
            }
        } catch (NoResultException e) {
            LOG.warn("Could not remove GodkantAvtal for user with hsaId '" + hsaId + "', avtal version '" + avtalVersion + "'. No approval found.");
        }
    }

    @Override
    public void removeAllUserApprovments(String hsaId) {
        List<GodkantAvtal> godkandaAvtal = entityManager.createQuery("SELECT ga FROM GodkantAvtal ga WHERE ga.hsaId = :hsaId", GodkantAvtal.class)
                .setParameter("hsaId", hsaId)
                .getResultList();

        for (GodkantAvtal godkantAvtal : godkandaAvtal) {
            entityManager.remove(godkantAvtal);
        }
    }
}
