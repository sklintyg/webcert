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

package se.inera.intyg.webcert.web.service.notification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Status;

@Component
public class FragorOchSvarCreatorImpl implements FragorOchSvarCreator {

    private static final String FRAGESTALLARE_FK = "FK";
    private static final String FRAGESTALLARE_WEBCERT = "WC";

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
    public FragorOchSvar createFragorOchSvar(String intygsId, SchemaVersion version) {
        FragorOchSvar fs = null;

        if (version.equals(SchemaVersion.VERSION_1)) {
            fs = performCount(fragaSvarRepository.findFragaSvarStatusesForIntyg(intygsId));
        } else if (version.equals(SchemaVersion.VERSION_2)) {
            fs = performArendeCount(arendeRepository.findByIntygsId(intygsId));
        }

        LOG.debug("Created FragorOchSvar ({}) for intyg {}", fs.toString(), intygsId);

        return fs;
    }

    @VisibleForTesting
    FragorOchSvar performArendeCount(List<Arende> arenden) {
      int antalSvar = 0;
      int antalHanteradeSvar = 0;
      int antalFragor = 0;
      int antalHanteradeFragor = 0;

      for (Arende arende : arenden) {
          if (arende.getSkickatAv().equalsIgnoreCase(FRAGESTALLARE_WEBCERT)) {
              if (!Strings.isNullOrEmpty(arende.getMeddelande())) {
                  antalSvar++;
                  if (arende.getStatus().equals(Status.CLOSED)) {
                      antalHanteradeSvar++;
                  }
              }
          } else if (arende.getSkickatAv().equalsIgnoreCase(FRAGESTALLARE_FK)) {
              antalFragor++;
              if (arende.getStatus().equals(Status.CLOSED)) {
                  antalHanteradeFragor++;
              }
          }
      }
      return new FragorOchSvar(antalFragor, antalSvar, antalHanteradeFragor, antalHanteradeSvar);
    }

    public FragorOchSvar performCount(List<FragaSvarStatus> fsStatuses) {

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
