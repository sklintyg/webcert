package se.inera.webcert.integration.registry;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;

@Service
public class IntegreradeEnheterRegistryImpl implements IntegreradeEnheterRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(IntegreradeEnheterRegistryImpl.class);

    @Autowired
    private IntegreradEnhetRepository integreradEnhetRepository;

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.integration.IntegreradeEnheterService#addIfNotExistsIntegreradEnhet(se.inera.webcert
     * .service.integration.dto.IntegreradEnhetEntry)
     */
    @Override
    @Transactional()
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
     * @see se.inera.webcert.service.integration.IntegreradeEnheterService#isEnhetIntegrerad(java.lang.String)
     */
    @Override
    @Transactional()
    public boolean isEnhetIntegrerad(String enhetHsaId) {
        
        IntegreradEnhet ie = integreradEnhetRepository.findOne(enhetHsaId);
        
        if (ie == null) {
            LOG.debug("Unit {} is not in the registry of integrated units", enhetHsaId);
            return false;
        }

        // update entity with control date;
        ie.setSenasteKontrollDatum(LocalDateTime.now());
        integreradEnhetRepository.save(ie);

        return true;
    }
}
