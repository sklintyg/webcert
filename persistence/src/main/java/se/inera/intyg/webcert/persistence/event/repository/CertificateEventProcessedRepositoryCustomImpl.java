package se.inera.intyg.webcert.persistence.event.repository;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CertificateEventProcessedRepositoryCustomImpl implements CertificateEventProcessedRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void saveBatch(List<String> idList) {
        var inserts = idList.stream().map(id -> "('" + id + "')").collect(Collectors.joining(","));
        entityManager.createNativeQuery("INSERT INTO CERTIFICATE_EVENT_PROCESSED VALUES " + inserts + ";").executeUpdate();
    }
}
