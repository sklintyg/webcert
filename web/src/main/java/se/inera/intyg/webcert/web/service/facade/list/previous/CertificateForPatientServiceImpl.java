/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.list.previous;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygSource;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@Component(value = "certificatesForPatientServiceImpl")
public class CertificateForPatientServiceImpl implements CertificateForPatientService {

    private final Cache certificatesForPatientCache;
    private final ObjectMapper objectMapper;
    private final IntygService intygService;
    public static final Logger LOG = LoggerFactory.getLogger(CertificateForPatientServiceImpl.class);


    public CertificateForPatientServiceImpl(Cache certificatesForPatientCache, ObjectMapper objectMapper, IntygService intygService) {
        this.certificatesForPatientCache = certificatesForPatientCache;
        this.objectMapper = objectMapper;
        this.intygService = intygService;
    }

    @Override
    public List<ListIntygEntry> get(String key, List<Utkast> drafts, Personnummer patientId, List<String> units)
        throws JsonProcessingException {
        if (certificatesForPatientCache.get(key, String.class) == null) {
            final var certificates = getCertificates(patientId, units);
            final var listIntygEntries = IntygDraftsConverter.merge(certificates.getLeft(), drafts);
            certificatesForPatientCache.put(key, objectMapper.writeValueAsString(listIntygEntries));
            return listIntygEntries;
        }
        final var jsonData = certificatesForPatientCache.get(key, String.class);
        final var utkastsToListIntygEntries = IntygDraftsConverter.convertUtkastsToListIntygEntries(drafts);
        final var certificatesForPatient = getCachedCertificatesForPatient(jsonData);
        final var entries = new ArrayList<>(utkastsToListIntygEntries);

        for (ListIntygEntry entry : certificatesForPatient) {
            if (!entries.contains(entry) && entry.getSource().equals(IntygSource.IT)) {
                entries.add(entry);
            }
        }
        certificatesForPatientCache.put(key, objectMapper.writeValueAsString(entries));
        return entries;
    }

    private List<ListIntygEntry> getCachedCertificatesForPatient(String jsonData) throws JsonProcessingException {
        return objectMapper.readValue(jsonData,
            objectMapper.getTypeFactory().constructCollectionType(List.class, ListIntygEntry.class));
    }

    private Pair<List<ListIntygEntry>, Boolean> getCertificates(Personnummer patientId, List<String> units) {
        final var certificates = intygService.listIntyg(units, patientId);
        LOG.debug("Got #{} intyg", certificates.getLeft().size());
        return certificates;
    }
}
