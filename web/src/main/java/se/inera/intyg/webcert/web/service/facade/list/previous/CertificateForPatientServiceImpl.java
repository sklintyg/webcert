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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@Service(value = "certificatesForPatientServiceImpl")
public class CertificateForPatientServiceImpl implements CertificateForPatientService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateForPatientServiceImpl.class);
    private final Cache certificatesForPatientCache;
    private final ObjectMapper objectMapper;
    private final IntygService intygService;
    private final UtkastService utkastService;
    private final WebCertUserService webCertUserService;

    public CertificateForPatientServiceImpl(Cache certificatesForPatientCache, ObjectMapper objectMapper, IntygService intygService,
        UtkastService utkastService, WebCertUserService webCertUserService) {
        this.certificatesForPatientCache = certificatesForPatientCache;
        this.objectMapper = objectMapper;
        this.intygService = intygService;
        this.utkastService = utkastService;
        this.webCertUserService = webCertUserService;
    }

    @Override
    public List<ListIntygEntry> get(ListFilter filter, Personnummer patientId, List<String> units) {
        final var certificatesFromWC = getCertificatesFromWC(patientId, units);
        final var certificatesFromIT = getCertificatesFromIT(patientId, units);
        return IntygDraftsConverter.merge(certificatesFromIT, certificatesFromWC);
    }

    private List<Utkast> getCertificatesFromWC(Personnummer patientId, List<String> units) {
        final var certificatesFromWC = utkastService.findUtkastByPatientAndUnits(patientId, units);
        LOG.debug("UtkastService returned {} certificates", certificatesFromWC.size());
        return certificatesFromWC;
    }

    private List<ListIntygEntry> getCertificatesFromIT(Personnummer patientId, List<String> units) {
        final var key = generateKey(patientId, units);
        final var certificatesFromIT = deserialize(
            certificatesForPatientCache.get(key, () -> {
                final var certificates = intygService.listIntyg(units, patientId).getLeft();
                LOG.debug("'{}' certificates from IntygService was put in cache for key '{}'", certificates.size(), key);
                return objectMapper.writeValueAsString(certificates);
            })
        );
        LOG.debug("IntygsService returned {} certificates from cache for key '{}'", certificatesFromIT.size(), key);
        return certificatesFromIT;
    }

    private String generateKey(Personnummer patientId, List<String> units) {
        return Integer.toString(
            patientId.getPersonnummerHash()
                .concat(webCertUserService.getUser().getPersonId())
                .concat(units.toString())
                .hashCode()
        );
    }

    private List<ListIntygEntry> deserialize(String jsonData) {
        try {
            return objectMapper.readValue(
                jsonData,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ListIntygEntry.class)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
