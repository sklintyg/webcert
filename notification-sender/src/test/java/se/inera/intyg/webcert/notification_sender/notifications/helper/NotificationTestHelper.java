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
package se.inera.intyg.webcert.notification_sender.notifications.helper;

import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateTypeFormatEnum;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

import java.time.LocalDate;
import java.time.Year;

/**
 * Created by eriklupander on 2017-08-18.
 */
public class NotificationTestHelper {

    private static final String PERSNR = "191212121212";

    private NotificationTestHelper() {
    }

    public static Intyg createIntyg(String intygsTyp) {
        return createIntyg(intygsTyp, "1.0", "intyg123");
    }

    public static Intyg createIntyg(String intygsTyp, String intygTypeVersion, String intygsId) {
        Intyg intyg = new Intyg();
        IntygId intygId = new IntygId();
        intygId.setExtension(intygsId);
        intyg.setIntygsId(intygId);

        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsTyp);
        intyg.setTyp(typAvIntyg);
        intyg.setVersion(intygTypeVersion);

        intyg.setPatient(buildPatient());

        HosPersonal hosPersonal = new HosPersonal();
        Enhet enhet = new Enhet();
        enhet.setVardgivare(new Vardgivare());
        enhet.setArbetsplatskod(new ArbetsplatsKod());
        hosPersonal.setEnhet(enhet);
        intyg.setSkapadAv(hosPersonal);
        // DatePeriodType and PartialDateType must be allowed
        intyg.getSvar().add(InternalConverterUtil.aSvar("")
                .withDelsvar("", InternalConverterUtil.aDatePeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .withDelsvar("", InternalConverterUtil.aPartialDate(PartialDateTypeFormatEnum.YYYY, Year.of(1999)))
                .build());
        return intyg;
    }

    public static Person buildPerson(boolean sekretessmarkering) {
        return new Person(Personnummer.createPersonnummer(PERSNR).get(),
                sekretessmarkering, false, "Tolvan", "Mellis", "Tolvansson", "Tolvgatan 12", "12121", "Tolvhult");
    }

    public static Patient buildPatient() {
        PersonId personId = new PersonId();
        personId.setExtension(PERSNR);

        Patient patient = new Patient();
        patient.setPersonId(personId);
        patient.setFornamn("");
        patient.setMellannamn("");
        patient.setEfternamn("");
        patient.setPostadress("");
        patient.setPostnummer("");
        patient.setPostort("");

        return patient;
    }

}
