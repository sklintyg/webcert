package se.inera.intyg.webcert.persistence.privatlakaravtal.repository;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.GodkantAvtal;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

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
