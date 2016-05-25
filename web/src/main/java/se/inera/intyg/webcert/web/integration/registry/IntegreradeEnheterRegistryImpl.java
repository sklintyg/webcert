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

package se.inera.intyg.webcert.web.integration.registry;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.intygstyper.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.intygstyper.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;

@Service
public class IntegreradeEnheterRegistryImpl implements IntegreradeEnheterRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(IntegreradeEnheterRegistryImpl.class);

    @Autowired
    private IntegreradEnhetRepository integreradEnhetRepository;

    private final Set<String> oldIntygTypes = Stream.of(Fk7263EntryPoint.MODULE_ID).collect(Collectors.toSet());
    private final Set<String> blacklisted = Stream.of(TsBasEntryPoint.MODULE_ID, TsDiabetesEntryPoint.MODULE_ID).collect(Collectors.toSet());

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.integration.IntegreradeEnheterService#addIfNotExistsIntegreradEnhet(se.inera.
     * intyg.webcert.web
     * .service.integration.dto.IntegreradEnhetEntry)
     */
    @Override
    @Transactional("jpaTransactionManager")
    public void putIntegreradEnhet(IntegreradEnhetEntry entry, boolean schemaVersion1, boolean schemaVersion2) {

        String enhetsId = entry.getEnhetsId();

        IntegreradEnhet intEnhet = integreradEnhetRepository.findOne(enhetsId);
        if (intEnhet != null) {
            LOG.debug("Updating existing integrerad enhet", enhetsId);
            if (schemaVersion1) {
                intEnhet.setSchemaVersion1(schemaVersion1);
            }
            if (schemaVersion2) {
                intEnhet.setSchemaVersion2(schemaVersion2);
            }
        } else {
            intEnhet = new IntegreradEnhet();
            intEnhet.setEnhetsId(enhetsId);
            intEnhet.setEnhetsNamn(entry.getEnhetsNamn());
            intEnhet.setVardgivarId(entry.getVardgivareId());
            intEnhet.setVardgivarNamn(entry.getVardgivareNamn());
            intEnhet.setSchemaVersion1(schemaVersion1);
            intEnhet.setSchemaVersion2(schemaVersion2);
            LOG.debug("Adding unit to registry: {}", intEnhet.toString());
        }
        integreradEnhetRepository.save(intEnhet);
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.integration.IntegreradeEnheterService#isEnhetIntegrerad(java.lang.String)
     */
    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public boolean isEnhetIntegrerad(String enhetsHsaId, String intygType) {
        Optional<SchemaVersion> schemaVersion = getSchemaVersion(enhetsHsaId, intygType);
        return schemaVersion.isPresent();
    }

    @Override
    @Transactional("jpaTransactionManager")
    public void addIfSameVardgivareButDifferentUnits(String orgEnhetsHsaId, IntegreradEnhetEntry newEntry, String intygType) {
        if (getSchemaVersion(orgEnhetsHsaId, intygType).isPresent()) {
            IntegreradEnhet enhet = getIntegreradEnhet(orgEnhetsHsaId);
            IntegreradEnhetEntry orgEntry = getIntegreradEnhetEntry(enhet);

            if ((orgEntry != null) && (orgEntry.compareTo(newEntry) != 0)) {
                putIntegreradEnhet(newEntry, enhet.isSchemaVersion1(), enhet.isSchemaVersion2());
            }
        }
    }

    @Override
    @Transactional("jpaTransactionManager")
    public void deleteIntegreradEnhet(String enhetsHsaId) {
        IntegreradEnhet unit = integreradEnhetRepository.findOne(enhetsHsaId);
        if (unit != null) {
            integreradEnhetRepository.delete(unit);
            LOG.debug("IntegreradEnhet {} deleted", enhetsHsaId);
        }
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public Optional<SchemaVersion> getSchemaVersion(String enhetsHsaId, String intygType) {
        if (blacklisted.contains(intygType)) {
            return Optional.empty();
        }

        IntegreradEnhet enhet = getIntegreradEnhet(enhetsHsaId);

        if (enhet == null) {
            return Optional.empty();
        }

        if (!oldIntygTypes.contains(intygType)) {
            return enhet.isSchemaVersion2() ? Optional.of(SchemaVersion.VERSION_2) : Optional.empty();
        } else if (!enhet.isSchemaVersion1()) {
            return Optional.empty();
        } else {
            return enhet.isSchemaVersion2() ? Optional.of(SchemaVersion.VERSION_2) : Optional.of(SchemaVersion.VERSION_1);
        }
    }

    private IntegreradEnhet getIntegreradEnhet(String enhetsHsaId) {
        IntegreradEnhet enhet = integreradEnhetRepository.findOne(enhetsHsaId);

        if (enhet == null) {
            LOG.debug("Unit {} is not in the registry of integrated units", enhetsHsaId);
            return null;
        }

        // update entity with control date;
        enhet.setSenasteKontrollDatum(LocalDateTime.now());
        integreradEnhetRepository.save(enhet);

        return enhet;
    }

    private IntegreradEnhetEntry getIntegreradEnhetEntry(IntegreradEnhet enhet) {
        if (enhet == null) {
            return null;
        }
        return new IntegreradEnhetEntry(enhet.getEnhetsId(), enhet.getEnhetsNamn(), enhet.getVardgivarId(), enhet.getVardgivarNamn());
    }
}
