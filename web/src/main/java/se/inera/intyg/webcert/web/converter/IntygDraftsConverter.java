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
package se.inera.intyg.webcert.web.converter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.StatusKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygSource;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.IntygsStatus;

@Component
public class IntygDraftsConverter {

    private static final Logger LOG = LoggerFactory.getLogger(IntygDraftsConverter.class);

    private static final Comparator<ListIntygEntry> INTYG_ENTRY_DATE_COMPARATOR_DESC = (ie1, ie2) -> ie2.getLastUpdatedSigned()
            .compareTo(ie1.getLastUpdatedSigned());

    private static final Comparator<IntygsStatus> INTYG_STATUS_COMPARATOR = (c1, c2) -> c1.getTidpunkt().compareTo(c2.getTidpunkt());

    private static final List<String> ARCHIVED_STATUSES = Arrays.asList(StatusKod.DELETE.name(), StatusKod.RESTOR.name());

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    public static List<ListIntygEntry> merge(List<ListIntygEntry> intygList, List<Utkast> utkastList) {

        LOG.debug("Merging intyg, signed {}, drafts {}", intygList.size(), utkastList.size());

        return Stream.concat(
                intygList.stream(),
                utkastList.stream()
                        .map(IntygDraftsConverter::convertUtkastToListIntygEntry))
                .sorted(INTYG_ENTRY_DATE_COMPARATOR_DESC)
                .collect(Collectors.toList());
    }

    public static List<ListIntygEntry> convertUtkastsToListIntygEntries(List<Utkast> utkastList, Comparator<ListIntygEntry> comparator) {
        return utkastList.stream()
                .map(IntygDraftsConverter::convertUtkastToListIntygEntry)
                .sorted((comparator == null ? INTYG_ENTRY_DATE_COMPARATOR_DESC : comparator))
                .collect(Collectors.toList());
    }

    public static ListIntygEntry convertUtkastToListIntygEntry(Utkast utkast) {

        ListIntygEntry entry = new ListIntygEntry();
        entry.setIntygId(utkast.getIntygsId());
        entry.setIntygType(utkast.getIntygsTyp());
        entry.setIntygTypeVersion(utkast.getIntygTypeVersion());
        entry.setSource(IntygSource.WC);
        entry.setUpdatedSignedBy(resolvedSignedBy(utkast));
        entry.setLastUpdatedSigned(utkast.getSenastSparadDatum());
        entry.setPatientId(utkast.getPatientPersonnummer());
        entry.setVidarebefordrad(utkast.getVidarebefordrad());
        entry.setStatus(resolveStatus(utkast));
        entry.setVersion(utkast.getVersion());

        return entry;
    }

    public static CertificateState findLatestStatus(List<IntygsStatus> intygStatuses) {
        return intygStatuses.stream()
                .filter(s -> !ARCHIVED_STATUSES.contains(s.getStatus().getCode()))
                .max(INTYG_STATUS_COMPARATOR)
                .map(s -> StatusKod.valueOf(s.getStatus().getCode()).toCertificateState())
                .orElse(CertificateState.UNHANDLED);
    }

    /**
     * If either the hsaId of the SkapadAv or SenastSparadAv matches the signing hsaId,
     * we return the Name instead of the HSA ID.
     */
    private static String resolvedSignedBy(Utkast utkast) {
        if (utkast.getSignatur() == null) {
            return utkast.getSenastSparadAv().getNamn();
        } else if (utkast.getSkapadAv() != null && utkast.getSkapadAv().getHsaId().equals(utkast.getSignatur().getSigneradAv())) {
            return utkast.getSkapadAv().getNamn();
        } else if (utkast.getSenastSparadAv() != null
                && utkast.getSenastSparadAv().getHsaId().equals(utkast.getSignatur().getSigneradAv())) {
            return utkast.getSenastSparadAv().getNamn();
        } else {
            return utkast.getSignatur().getSigneradAv();
        }
    }

    public static String resolveStatus(Utkast draft) {
        if (draft.getAterkalladDatum() != null && draft.getStatus().equals(UtkastStatus.DRAFT_LOCKED)) {
            return draft.getStatus().name() + "_" + CertificateState.CANCELLED.name();
        }
        if (draft.getAterkalladDatum() != null) {
            return CertificateState.CANCELLED.name();
        }
        if (draft.getSkickadTillMottagareDatum() != null) {
            return CertificateState.SENT.name();
        }
        if (draft.getSignatur() != null && draft.getSignatur().getSigneringsDatum() != null) {
            return CertificateState.RECEIVED.name();
        }
        return draft.getStatus().name();
    }

    public List<ListIntygEntry> convertIntygToListIntygEntries(List<Intyg> intygList, List<ListIntygEntry> webcertIntyg) {
        return intygList.stream()
                .map(intyg -> convertIntygToListIntygEntry(intyg,
                        webcertIntyg.stream().filter(it ->
                                it.getIntygId().equals(intyg.getIntygsId().getExtension())).findFirst().orElse(null)))
                .sorted(INTYG_ENTRY_DATE_COMPARATOR_DESC)
                .collect(Collectors.toList());
    }

    private ListIntygEntry convertIntygToListIntygEntry(Intyg source, ListIntygEntry altSource) {
        ListIntygEntry entry = new ListIntygEntry();
        entry.setIntygId(source.getIntygsId().getExtension());
        entry.setIntygType(moduleRegistry.getModuleIdFromExternalId(source.getTyp().getCode()));
        //TODO: sanitycheck of version format for intyg origination from IT?
        entry.setIntygTypeVersion(source.getVersion());
        entry.setSource(IntygSource.IT);

        if (altSource == null) {
            entry.setStatus(findLatestStatus(source.getStatus()).name());
        } else {
            entry.setStatus(determineMostRelevantStatus(findLatestStatus(source.getStatus()),
                    CertificateState.valueOf(altSource.getStatus())));
        }

        entry.setUpdatedSignedBy(source.getSkapadAv().getFullstandigtNamn());
        entry.setLastUpdatedSigned(source.getSigneringstidpunkt());
        entry.setPatientId(createPnr(source.getPatient().getPersonId().getExtension()));
        return entry;
    }

    /**
     * This looks funky, it is because a corner case exists where statuses in Intygstjänsten might not be up
     * to date with the actual situation because of the asynchronous nature of the connection between IT and WC.
     * Thus, we need to establish a hierarchy of precedence between statuses depending on their origin.
     */
    private String determineMostRelevantStatus(CertificateState itStatus, CertificateState wcStatus) {
        if (wcStatus == CertificateState.CANCELLED) {
            return wcStatus.name();
        }
        if (itStatus == CertificateState.CANCELLED || itStatus == CertificateState.SENT) {
            return itStatus.name();
        }
        if (wcStatus == CertificateState.SENT) {
            return wcStatus.name();
        }
        return itStatus.name();
    }

    private Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

}
