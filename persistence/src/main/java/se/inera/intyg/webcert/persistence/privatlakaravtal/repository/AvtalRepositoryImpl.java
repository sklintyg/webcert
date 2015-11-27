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
            Integer latestAvtalVersion = entityManager.createQuery("SELECT MAX(a.avtalVersion) FROM Avtal a", Integer.class).getSingleResult();
            return latestAvtalVersion == null ? -1 : latestAvtalVersion;
        } catch (NoResultException e) {
            return -1;
        }
    }
}
