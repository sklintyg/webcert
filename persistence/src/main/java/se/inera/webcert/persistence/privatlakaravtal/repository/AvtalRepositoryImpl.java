package se.inera.webcert.persistence.privatlakaravtal.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.persistence.privatlakaravtal.model.GodkantAvtal;

/**
 * Created by eriklupander on 2015-08-05.
 */
public class AvtalRepositoryImpl implements AvtalRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(AvtalRepositoryImpl.class);

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
