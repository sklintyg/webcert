package se.inera.intyg.webcert.web.integration.registry;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;

@Service
public class IntegreradeEnheterRegistryImpl implements IntegreradeEnheterRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(IntegreradeEnheterRegistryImpl.class);

    @Autowired
    private IntegreradEnhetRepository integreradEnhetRepository;

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.integration.IntegreradeEnheterService#addIfNotExistsIntegreradEnhet(se.inera.intyg.webcert.web
     * .service.integration.dto.IntegreradEnhetEntry)
     */
    @Override
    @Transactional("jpaTransactionManager")
    public boolean addIfNotExistsIntegreradEnhet(IntegreradEnhetEntry entry) {

        String enhetsId = entry.getEnhetsId();

        if (isEnhetIntegrerad(enhetsId)) {
            LOG.debug("Unit {} is already registered", enhetsId);
            return false;
        }

        IntegreradEnhet integreradEnhet = new IntegreradEnhet();
        integreradEnhet.setEnhetsId(enhetsId);
        integreradEnhet.setEnhetsNamn(entry.getEnhetsNamn());
        integreradEnhet.setVardgivarId(entry.getVardgivareId());
        integreradEnhet.setVardgivarNamn(entry.getVardgivareNamn());

        IntegreradEnhet savedIntegreradEnhet = integreradEnhetRepository.save(integreradEnhet);

        LOG.debug("Added unit to registry: {}", savedIntegreradEnhet.toString());

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.integration.IntegreradeEnheterService#isEnhetIntegrerad(java.lang.String)
     */
    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public boolean isEnhetIntegrerad(String enhetsHsaId) {
        IntegreradEnhetEntry ie = getIntegreradEnhetEntry(enhetsHsaId);
        return (ie != null);
    }

    @Override
    @Transactional("jpaTransactionManager")
    public void addIfSameVardgivareButDifferentUnits(String orgEnhetsHsaId, IntegreradEnhetEntry newEntry) {

        IntegreradEnhetEntry orgEntry = getIntegreradEnhetEntry(orgEnhetsHsaId);

        if ((orgEntry != null) && (orgEntry.compareTo(newEntry) != 0)) {
            addIfNotExistsIntegreradEnhet(newEntry);
        }
    }

    private IntegreradEnhetEntry getIntegreradEnhetEntry(String enhetsHsaId) {

        IntegreradEnhet enhet = integreradEnhetRepository.findOne(enhetsHsaId);

        if (enhet == null) {
            LOG.debug("Unit {} is not in the registry of integrated units", enhetsHsaId);
            return null;
        }

        // update entity with control date;
        enhet.setSenasteKontrollDatum(LocalDateTime.now());
        integreradEnhetRepository.save(enhet);

        return new IntegreradEnhetEntry(enhet.getEnhetsId(), enhet.getEnhetsNamn(), enhet.getVardgivarId(), enhet.getVardgivarNamn());
    }
}
