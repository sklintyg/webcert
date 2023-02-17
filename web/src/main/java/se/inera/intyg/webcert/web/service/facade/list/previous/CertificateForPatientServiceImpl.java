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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterPersonIdValue;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygSource;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@Service(value = "certificatesForPatientServiceImpl")
public class CertificateForPatientServiceImpl implements CertificateForPatientService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateForPatientServiceImpl.class);
    private static final String ALGORITHM = "SHA-1";

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
    public List<ListIntygEntry> get(ListFilter filter, Personnummer patientId, List<String> units)
        throws JsonProcessingException {
        final var drafts = utkastService.findUtkastByPatientAndUnits(patientId, units);
        LOG.debug("UtkastService returned {} certificates", drafts.size());
        final var key = getSHA1Hash(base64EncodedString(filter, units));
        if (certificatesForPatientCache.get(key, String.class) == null) {
            LOG.debug("No data was found in cache for key '{}'", key);
            final var certificates = getCertificates(patientId, units);
            final var listIntygEntries = IntygDraftsConverter.merge(certificates.getLeft(), drafts);
            LOG.debug("'{}' certificates was put in cache for key '{}'", listIntygEntries.size(), key);
            certificatesForPatientCache.put(key, objectMapper.writeValueAsString(listIntygEntries));
            return listIntygEntries;
        }
        final var jsonData = certificatesForPatientCache.get(key, String.class);
        final var utkastsToListIntygEntries = IntygDraftsConverter.convertUtkastsToListIntygEntries(drafts);
        final var certificatesForPatient = getCachedCertificatesForPatient(jsonData);
        LOG.debug("Cache returned {} certificates", certificatesForPatient.size());
        final var entries = new ArrayList<>(utkastsToListIntygEntries);

        for (ListIntygEntry entry : certificatesForPatient) {
            if (!entries.contains(entry) && entry.getSource().equals(IntygSource.IT)) {
                entries.add(entry);
            }
        }
        LOG.debug("'{}' certificates was put in cache for key '{}'", entries.size(), key);
        certificatesForPatientCache.put(key, objectMapper.writeValueAsString(entries));
        return entries;
    }

    private List<ListIntygEntry> getCachedCertificatesForPatient(String jsonData) throws JsonProcessingException {
        return objectMapper.readValue(jsonData,
            objectMapper.getTypeFactory().constructCollectionType(List.class, ListIntygEntry.class));
    }

    private Pair<List<ListIntygEntry>, Boolean> getCertificates(Personnummer patientId, List<String> units) {
        final var certificates = intygService.listIntyg(units, patientId);
        LOG.debug("IntygsService returned {} certificates", certificates.getLeft().size());
        return certificates;
    }

    private static String getSHA1Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            for (byte bytes : messageDigest) {
                stringBuilder.append(String.format("%02x", bytes));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String base64EncodedString(ListFilter filter, List<String> units) {
        final var user = webCertUserService.getUser();
        final var patientId = (ListFilterPersonIdValue) filter.getValue("PATIENT_ID");
        return Base64.getEncoder().encodeToString((patientId.getValue() + user.getPersonId() + units).getBytes(StandardCharsets.UTF_8));
    }
}
