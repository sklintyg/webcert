/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.intygstjanststub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;
import se.inera.intyg.schemas.contract.Personnummer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author marced
 */
@Component
public class IntygStore {
    private static final Logger LOG = LoggerFactory.getLogger(IntygStore.class);

    private Map<String, CertificateHolder> intyg = new ConcurrentHashMap<>();
    private Map<String, String> contentTemplates = new ConcurrentHashMap<>();

    public void addIntyg(CertificateHolder metaType) {
        LOG.debug("IntygStore: adding intyg " + metaType.getId() + " to store.");
        if (intyg.containsKey(metaType.getId())) {
            LOG.debug("IntygStore: Not adding "  + metaType.getId() + " to store. Is already present.");
            return;
        }
        intyg.put(metaType.getId(), metaType);
    }

    public Map<String, CertificateHolder> getAllIntyg() {
        return intyg;
    }

    public List<CertificateHolder> getIntygForEnhetAndPersonnummer(final List<String> enhetsIds, final String personnummer) {
        Personnummer pnr = createPnr(personnummer);
        return intyg.values().stream()
                .filter(s -> enhetsIds.contains(s.getCareUnitId()) && pnr.equals(s.getCivicRegistrationNumber()))
                .collect(Collectors.toList());
    }

    public CertificateHolder getIntygForCertificateId(String certificateId) {
        return intyg.get(certificateId);
    }

    public void addStatus(String extension, CertificateStateHolder status) {
        CertificateHolder intygResponse = intyg.get(extension);
        if (intygResponse != null) {
            intygResponse.getCertificateStates().add(status);
        }
    }

    public void clear() {
        intyg.clear();
    }

    public void addContentTemplate(String fileName, String contentTemplate) {
        contentTemplates.put(fileName, contentTemplate);
    }

    public String getContentTemplate(String fileName) {
        return contentTemplates.get(fileName);
    }

    private Personnummer createPnr(String personId) {
        return Personnummer
                .createValidatedPersonnummer(personId)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

}
