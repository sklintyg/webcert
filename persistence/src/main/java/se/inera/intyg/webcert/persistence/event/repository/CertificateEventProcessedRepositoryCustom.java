package se.inera.intyg.webcert.persistence.event.repository;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface CertificateEventProcessedRepositoryCustom {

    @Transactional
    void saveBatch(List<String> idList);
}
