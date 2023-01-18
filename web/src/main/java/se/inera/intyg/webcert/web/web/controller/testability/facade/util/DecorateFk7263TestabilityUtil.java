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

package se.inera.intyg.webcert.web.web.controller.testability.facade.util;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.fk7263.model.internal.PrognosBedomning;
import se.inera.intyg.common.fk7263.model.internal.Rehabilitering;
import se.inera.intyg.common.support.model.InternalDate;
import se.inera.intyg.common.support.model.InternalLocalDateInterval;
import se.inera.intyg.common.support.model.LocalDateInterval;

@Component
public class DecorateFk7263TestabilityUtil {

    public void decorateWithMinimumValues(Fk7263Utlatande utlatande) {
        utlatande.setAvstangningSmittskydd(false);
        utlatande.setDiagnosKod("diagnoskod");
        utlatande.setDiagnosBeskrivning("beskrivning");
        utlatande.setSjukdomsforlopp("aktuellt sjukdomsförlopp");
        utlatande.setFunktionsnedsattning("funktionsnedsättning");
        utlatande.setUndersokningAvPatienten(new InternalDate(LocalDate.now()));
        utlatande.setAtgardInomSjukvarden("åtgärd inom sjukvården");
        utlatande.setRehabilitering(Rehabilitering.rehabiliteringGarInteAttBedoma);
        utlatande.setArbetsformagaPrognos("arbetsförmåga prognos");
        utlatande.setPrognosBedomning(PrognosBedomning.arbetsformagaPrognosNej);
        utlatande.setArbetsloshet(true);
        utlatande.setNedsattMed25(
            new InternalLocalDateInterval(LocalDate.now().toString(), LocalDate.now().toString()));
    }

    public void decorateWithMaximumValues(Fk7263Utlatande utlatande) {
        utlatande.setAvstangningSmittskydd(true);
        utlatande.setDiagnosKod("diagnoskod");
        utlatande.setDiagnosBeskrivning("beskrivning");
        utlatande.setDiagnosBeskrivning1("beskrivning1");
        utlatande.setDiagnosBeskrivning2("beskrivning2");
        utlatande.setDiagnosBeskrivning3("beskrivning3");
        utlatande.setDiagnosKod2("diagnoskod2");
        utlatande.setDiagnosKod3("diagnoskod3");
        utlatande.setDiagnosKodsystem1("diagnoskodsystem1");
        utlatande.setDiagnosKodsystem2("diagnoskodsystem2");
        utlatande.setDiagnosKodsystem3("diagnoskodsystem3");
        utlatande.setSjukdomsforlopp("aktuellt sjukdomsförlopp");
        utlatande.setFunktionsnedsattning("funktionsnedsättning");
        utlatande.setUndersokningAvPatienten(new InternalDate(LocalDate.now()));
        utlatande.setTelefonkontaktMedPatienten(new InternalDate(LocalDate.now()));
        utlatande.setJournaluppgifter(new InternalDate(LocalDate.now()));
        utlatande.setAnnanReferens(new InternalDate(LocalDate.now()));
        utlatande.setAktivitetsbegransning("aktivitetsbegränsningar");
        utlatande.setRekommendationKontaktArbetsformedlingen(true);
        utlatande.setRekommendationKontaktForetagshalsovarden(true);
        utlatande.setRekommendationOvrigt("Övrigt");
        utlatande.setRekommendationOvrigtCheck(true);
        utlatande.setAtgardInomSjukvarden("åtgärd inom sjukvården");
        utlatande.setAnnanAtgard("annanÅtgärd");
        utlatande.setRehabilitering(Rehabilitering.rehabiliteringAktuell);
        utlatande.setArbetsformagaPrognos("arbetsförmåga prognos");
        utlatande.setPrognosBedomning(PrognosBedomning.arbetsformagaPrognosNej);
        utlatande.setArbetsformagaPrognosGarInteAttBedomaBeskrivning("Går inte att bedöma beskrivning");
        utlatande.setArbetsloshet(true);
        utlatande.setForaldrarledighet(true);
        utlatande.setNuvarandeArbete(true);
        utlatande.setNuvarandeArbetsuppgifter("nuvarande arbetsuppgifter");
        utlatande.setNedsattMed25(
            new InternalLocalDateInterval(LocalDate.now().toString(), LocalDate.now().toString()));
        utlatande.setNedsattMed50(
            new InternalLocalDateInterval(LocalDate.now().toString(), LocalDate.now().toString()));
        utlatande.setNedsattMed75(
            new InternalLocalDateInterval(LocalDate.now().toString(), LocalDate.now().toString()));
        utlatande.setNedsattMed100(
            new InternalLocalDateInterval(LocalDate.now().toString(), LocalDate.now().toString()));

        utlatande.setGiltighet(new LocalDateInterval(LocalDate.now(), LocalDate.now().plusDays(1)));
        utlatande.setSamsjuklighet(true);
        utlatande.setRessattTillArbeteAktuellt(true);
        utlatande.setRessattTillArbeteEjAktuellt(true);
        utlatande.setKontaktMedFk(true);
        utlatande.setTjanstgoringstid("tjänstgörningstid");
        utlatande.setAnnanReferensBeskrivning("annan referens");
        utlatande.setKommentar("kommentar");
    }
}
