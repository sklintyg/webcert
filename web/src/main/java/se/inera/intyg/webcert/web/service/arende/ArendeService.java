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
package se.inera.intyg.webcert.web.service.arende;

import java.util.List;
import java.util.Map;
import java.util.Set;

import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;

public interface ArendeService {

    /**
     * Validates and decorates incoming arende with additional information.
     */
    Arende processIncomingMessage(Arende arende);

    ArendeConversationView createMessage(String intygId, ArendeAmne amne, String rubrik, String meddelande);

    ArendeConversationView answer(String svarPaMeddelandeId, String meddelande);

    List<ArendeConversationView> answerKomplettering(String intygsId, String meddelande);

    List<ArendeConversationView> setForwarded(String intygsId);

    ArendeConversationView openArendeAsUnhandled(String meddelandeId);

    /**
     * List names of signing doctors for units where the webcert user is logged in.
     */
    List<Lakare> listSignedByForUnits(String enhetsId);

    List<ArendeConversationView> getArenden(String intygsId);

    QueryFragaSvarResponse filterArende(QueryFragaSvarParameter filterParameters);

    /**
     * Close Arende and set status 'CLOSED'.
     */
    ArendeConversationView closeArendeAsHandled(String meddelandeId, String intygTyp);

    /**
     * Close all Kompletteringsbegäran for the given certificate.
     */
    void closeCompletionsAsHandled(String intygId, String intygTyp);

    /**
     * Close all messages related to a certificate.
     *
     * @param intygsId the certificates unique identifier
     */
    void closeAllNonClosed(String intygsId);

    Arende getArende(String meddelandeId);

    Map<String, Long> getNbrOfUnhandledArendenForCareUnits(List<String> allUnitIds, Set<String> intygsTyper);

    /**
     * Return the ID of the most recent message for the care unit currently logged in to.
     * @param intygsId
     * @return
     */
    String getLatestMeddelandeIdForCurrentCareUnit(String intygsId);
}
