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
package se.inera.intyg.webcert.web.service.notification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;

@Component
public class FragorOchSvarCreatorImpl implements FragorOchSvarCreator {

    private static final String FRAGESTALLARE_FK = FrageStallare.FORSAKRINGSKASSAN.getKod();
    private static final String FRAGESTALLARE_WEBCERT = FrageStallare.WEBCERT.getKod();

    private static final Logger LOG = LoggerFactory.getLogger(FragorOchSvarCreatorImpl.class);

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    @Autowired
    private ArendeRepository arendeRepository;

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.notification.FragorOchSvarCreator#
     * createFragorOchSvar(java.lang.String)
     */
    @Override
    public FragorOchSvar createFragorOchSvar(String intygsId) {
        FragorOchSvar fs = performCount(fragaSvarRepository.findFragaSvarStatusesForIntyg(intygsId));

        LOG.debug("Created FragorOchSvar ({}) for intyg {}", fs.toString(), intygsId);

        return fs;
    }

    @Override
    public Pair<ArendeCount, ArendeCount> createArenden(String intygsId, String intygstyp) {
        Pair<ArendeCount, ArendeCount> fragor;

        if (Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(intygstyp)) {
            fragor = performFragorCount(fragaSvarRepository.findFragaSvarStatusesForIntyg(intygsId));
        } else {
            fragor = performArendeCount(arendeRepository.findByIntygsId(intygsId));
        }

        LOG.debug("Created SkickadeFragor ({}) for intyg {}", fragor.getLeft().toString(), intygsId);
        LOG.debug("Created MottagnaFragor ({}) for intyg {}", fragor.getRight().toString(), intygsId);

        return fragor;
    }

    private Pair<ArendeCount, ArendeCount> performFragorCount(List<FragaSvarStatus> fsStatuses) {
        int skickadeFragorTotalt = 0;
        int skickadeFragorBesvarade = 0;
        int skickadeFragorEjBesvarade = 0;
        int skickadeFragorHanterade = 0;
        int mottagnaFragorTotalt = 0;
        int mottagnaFragorBesvarade = 0;
        int mottagnaFragorEjBesvarade = 0;
        int mottagnaFragorHanterade = 0;

        for (FragaSvarStatus fsStatus : fsStatuses) {
            if (isFromWebcert(fsStatus)) {
                skickadeFragorTotalt++;
                if (fsStatus.isClosed()) {
                    skickadeFragorHanterade++;
                } else if (fsStatus.hasAnswerSet()) {
                    skickadeFragorBesvarade++;
                } else {
                    skickadeFragorEjBesvarade++;
                }
            } else if (isFromFK(fsStatus)) {
                mottagnaFragorTotalt++;
                if (fsStatus.isClosed()) {
                    mottagnaFragorHanterade++;
                } else if (fsStatus.hasAnswerSet()) {
                    mottagnaFragorBesvarade++;
                } else {
                    mottagnaFragorEjBesvarade++;
                }
            }
        }

        return Pair.of(new ArendeCount(skickadeFragorTotalt, skickadeFragorEjBesvarade, skickadeFragorBesvarade, skickadeFragorHanterade),
                new ArendeCount(mottagnaFragorTotalt, mottagnaFragorEjBesvarade, mottagnaFragorBesvarade, mottagnaFragorHanterade));
    }

    private Pair<ArendeCount, ArendeCount> performArendeCount(List<Arende> arenden) {
        int skickadeFragorTotalt = 0;
        int skickadeFragorBesvarade = 0;
        int skickadeFragorEjBesvarade = 0;
        int skickadeFragorHanterade = 0;
        int mottagnaFragorTotalt = 0;
        int mottagnaFragorBesvarade = 0;
        int mottagnaFragorEjBesvarade = 0;
        int mottagnaFragorHanterade = 0;
        Set<String> isAnswered = new HashSet<>();

        for (Arende arende : arenden) {
            if (!Strings.nullToEmpty(arende.getSvarPaId()).trim().isEmpty()) {
                isAnswered.add(arende.getSvarPaId());
            }
        }

        for (Arende arende : arenden) {
            if (ArendeAmne.PAMINN == arende.getAmne() || !Strings.nullToEmpty(arende.getSvarPaId()).trim().isEmpty()) {
                // skip answers and reminders
                continue;
            }
            if (FRAGESTALLARE_WEBCERT.equalsIgnoreCase(arende.getSkickatAv())) {
                skickadeFragorTotalt++;
                if (Status.CLOSED.equals(arende.getStatus())) {
                    skickadeFragorHanterade++;
                } else if (isAnswered.contains(arende.getMeddelandeId())) {
                    skickadeFragorBesvarade++;
                } else {
                    skickadeFragorEjBesvarade++;
                }
            } else if (FRAGESTALLARE_FK.equalsIgnoreCase(arende.getSkickatAv())) {
                mottagnaFragorTotalt++;
                if (Status.CLOSED.equals(arende.getStatus())) {
                    mottagnaFragorHanterade++;
                } else if (isAnswered.contains(arende.getMeddelandeId())) {
                    mottagnaFragorBesvarade++;
                } else {
                    mottagnaFragorEjBesvarade++;
                }
            }
        }
        return Pair.of(new ArendeCount(skickadeFragorTotalt, skickadeFragorEjBesvarade, skickadeFragorBesvarade, skickadeFragorHanterade),
                new ArendeCount(mottagnaFragorTotalt, mottagnaFragorEjBesvarade, mottagnaFragorBesvarade, mottagnaFragorHanterade));
    }

    private FragorOchSvar performCount(List<FragaSvarStatus> fsStatuses) {

        int antalFragor = 0;
        int antalSvar = 0;
        int antalHanteradeFragor = 0;
        int antalHanteradeSvar = 0;

        for (FragaSvarStatus fsStatus : fsStatuses) {

            // WEBCERT-2001: Vi vill endast öka antalet hanteradeSvar på
            // faktiska svar från FK.
            if (isFromWebcert(fsStatus)) {
                if (fsStatus.hasAnswerSet()) {
                    antalSvar++;
                    if (fsStatus.isClosed()) {
                        antalHanteradeSvar++;
                    }
                }
            } else if (isFromFK(fsStatus)) {
                antalFragor++;
                if (fsStatus.isClosed()) {
                    antalHanteradeFragor++;
                }
            }
        }

        return new FragorOchSvar(antalFragor, antalSvar, antalHanteradeFragor,
                antalHanteradeSvar);
    }

    public boolean isFromFK(FragaSvarStatus fsStatus) {
        return fsStatus.getFrageStallare().equalsIgnoreCase(FRAGESTALLARE_FK);
    }

    public boolean isFromWebcert(FragaSvarStatus fsStatus) {
        return fsStatus.getFrageStallare().equalsIgnoreCase(
                FRAGESTALLARE_WEBCERT);
    }

}
