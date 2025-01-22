/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;

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
        return merge(certificatesFromWC, certificatesFromIT);
    }

    private List<ListIntygEntry> getCertificatesFromWC(Personnummer patientId, List<String> units) {
        final var certificatesFromWC = utkastService.findUtkastByPatientAndUnits(patientId, units);
        LOG.debug("UtkastService returned {} certificates", certificatesFromWC.size());
        final var relationsMap = getRelationsMap(certificatesFromWC);
        return certificatesFromWC.stream()
            .map(IntygDraftsConverter::convertUtkastToListIntygEntry)
            .map(entry -> decorateWithRelation(entry, relationsMap))
            .collect(Collectors.toList());
    }

    private List<ListIntygEntry> getCertificatesFromIT(Personnummer patientId, List<String> units) {
        final var key = generateKey(patientId, units);
        final var certificatesFromIT = deserialize(
            certificatesForPatientCache.get(key, () -> {
                final var certificates = intygService.listIntygFromIT(units, patientId);
                LOG.debug("'{}' certificates from IntygService was put in cache for key '{}'", certificates.size(), key);
                return objectMapper.writeValueAsString(certificates);
            })
        );
        LOG.debug("IntygsService returned {} certificates from cache for key '{}'", certificatesFromIT.size(), key);
        return certificatesFromIT;
    }

    private Map<String, FrontendRelations> getRelationsMap(List<Utkast> utkasts) {
        final Map<String, FrontendRelations> relationsMap = new HashMap<>();
        getRelevantRelations(utkasts)
            .forEach(relation -> {
                    final var frontendRelations = relationsMap.computeIfAbsent(relation.getIntygsId(), id -> new FrontendRelations());
                    if (RelationKod.ERSATT.equals(relation.getRelationKod())) {
                        frontendRelations.setReplacedByIntyg(relation);
                    }
                    if (RelationKod.KOMPLT.equals(relation.getRelationKod())) {
                        frontendRelations.setComplementedByIntyg(relation);
                    }
                }
            );
        return relationsMap;
    }

    private ListIntygEntry decorateWithRelation(ListIntygEntry entry, Map<String, FrontendRelations> relationsMap) {
        entry.getRelations().setLatestChildRelations(relationsMap.getOrDefault(entry.getIntygId(), new FrontendRelations()));
        return entry;
    }

    private List<WebcertCertificateRelation> getRelevantRelations(List<Utkast> utkasts) {
        final var relevantRelationCodes = List.of(RelationKod.ERSATT, RelationKod.KOMPLT);
        return utkasts.stream()
            .filter(utkast -> utkast.getRelationIntygsId() != null && relevantRelationCodes.contains(utkast.getRelationKod())
                && utkast.getStatus() == UtkastStatus.SIGNED && utkast.getAterkalladDatum() == null)
            .map(this::getWebcertCertificateRelation)
            .collect(Collectors.toList());
    }

    private WebcertCertificateRelation getWebcertCertificateRelation(Utkast utkast) {
        return new WebcertCertificateRelation(
            utkast.getRelationIntygsId(),
            utkast.getRelationKod(),
            utkast.getSkapad(),
            utkast.getStatus(),
            utkast.getAterkalladDatum() != null
        );
    }

    private String generateKey(Personnummer patientId, List<String> units) {
        return Integer.toString(
            patientId.getPersonnummerHash()
                .concat(webCertUserService.getUser().getHsaId())
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

    private static List<ListIntygEntry> merge(List<ListIntygEntry> certificatesFromWC, List<ListIntygEntry> certificatesFromIT) {
        return Stream.concat(
                certificatesFromIT.stream().filter(fromIT -> keepITCertificate(certificatesFromWC, fromIT)),
                certificatesFromWC.stream().filter(fromWC -> keepWCCertificate(certificatesFromIT, fromWC)))
            .sorted(comparing(ListIntygEntry::getLastUpdatedSigned, reverseOrder()))
            .collect(Collectors.toList());
    }

    private static boolean keepITCertificate(List<ListIntygEntry> certificatesFromWC, ListIntygEntry fromIT) {
        return certificatesFromWC.stream()
            .filter(fromWC ->
                matchingIds(fromIT, fromWC)
                    && !isCancelled(fromIT)
                    && (!isOnlySentInIT(fromIT, fromWC) || isOnlySentInIT(fromIT, fromWC) && isCancelled(fromWC))
            )
            .findAny()
            .isEmpty();
    }

    private static boolean keepWCCertificate(List<ListIntygEntry> certificatesFromIT, ListIntygEntry fromWC) {
        return certificatesFromIT.stream()
            .filter(fromIT ->
                matchingIds(fromWC, fromIT)
                    && (isCancelled(fromIT) || (isOnlySentInIT(fromIT, fromWC) && !isCancelled(fromWC)))
            )
            .findAny()
            .isEmpty();
    }

    private static boolean matchingIds(ListIntygEntry fromIt, ListIntygEntry fromWc) {
        return fromIt.getIntygId().equalsIgnoreCase(fromWc.getIntygId());
    }

    private static boolean isCancelled(ListIntygEntry entry) {
        return entry.getStatus().equalsIgnoreCase(CertificateState.CANCELLED.name());
    }

    private static boolean isOnlySentInIT(ListIntygEntry fromIT, ListIntygEntry fromWC) {
        return isSent(fromIT) && !isSent(fromWC);
    }

    private static boolean isSent(ListIntygEntry entry) {
        return entry.getStatus().equalsIgnoreCase(CertificateState.SENT.name());
    }
}
