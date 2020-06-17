/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.event;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.common.enumerations.EventKod;
import se.inera.intyg.webcert.persistence.event.model.UtkastEvent;
import se.inera.intyg.webcert.persistence.event.repository.UtkastEventRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;


@Service
@Transactional
public class UtkastEventServiceImpl implements UtkastEventService {

    @Autowired
    UtkastEventRepository utkastEventRepository;

    @Override
    public List<UtkastEvent> getUtkastEvents(String intygsId) {
        return utkastEventRepository.findByIntygsId(intygsId);
    }

    public void createUtkastEventFromCopyUtkast(Utkast utkast, String user, EventKod eventKod, String originalIntygsId) {
        String meddelande = getMeddelandeForCopyUtkastEvent(utkast, eventKod, originalIntygsId);
        save(utkast.getIntygsId(), user, eventKod, meddelande);
    }

    @Override
    public void createUtkastEvent(String intygsId, String user, EventKod eventKod) {
        createUtkastEvent(intygsId, user, eventKod, eventKod.getKlartext());
    }

    @Override
    public void createUtkastEvent(String intygsId, String user, EventKod eventKod, String meddelande) {
        if (meddelande.isEmpty()) {
            save(intygsId, user, eventKod, eventKod.getKlartext());
        } else {
            save(intygsId, user, eventKod, meddelande);
        }
    }

    private void save(String intygsId, String user, EventKod eventKod, String meddelande) {
        UtkastEvent event = new UtkastEvent();
        event.setIntygsId(intygsId);
        event.setAnvandare(user);
        event.setEventKod(eventKod);
        event.setTimestamp(LocalDateTime.now());
        event.setMeddelande(meddelande);

        utkastEventRepository.save(event);
    }

    private String getMeddelandeForCopyUtkastEvent(Utkast utkast, EventKod event, String originalId) {
        switch (event) {
            case ODEFINIERAT:
                return "testing, testing " + originalId;
            case ERSATTER:
                return "Ersätter " + originalId;
            case KOMPLETTERAR:
                return "Kompletterar " + originalId;
            case FORLANGER:
                return "Förlänger " + originalId;
            case SKAPATFRAN:
                return "Skapar " + utkast.getIntygsTyp() + " från " + originalId;
        }
        return event.getKlartext();
    }

}
