/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.fmb.sjukfall.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Befattning;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Arbetsformaga;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Befattningar;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Enhet;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Formaga;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Patient;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Vardgivare;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import se.inera.intyg.infra.sjukfall.dto.IntygData;

public class IntygstjanstConverterTest {

    private static final LocalDate START_DATUM = LocalDate.of(2018, 12, 12);
    private static final LocalDate SLUT_DATUM = LocalDate.of(2028, 12, 12);
    private static final LocalDateTime SIGNERING_TIDPUNKT = LocalDateTime.of(2018, 11, 11, 11, 11);
    private static final String INTYG_ID = "intyg-id";
    private static final String ROOT = "root";
    private static final String HSA_ID = "hsa-id";
    private static final String LAKARE_KOD = "203090";
    private static final String LAKARE_SYSTEM = "system";
    private static final String LAKARE_SYSTEM_NAME = "system-name";
    private static final String LAKARE_SYSTEM_VERSION = "system-version";
    private static final String LAKARE_DISPLAY_NAME = "LAKARE";
    private static final String LAKARE_ORIGINAL_TEXT = "original-text";
    private static final String PERSON_NUMMER = "191212121212";
    private static final int NEDSATTNING = 100;
    private static final String VARDGIVAR_NAMN = "Vard Givar Namn";
    private static final String ENHETS_NAMN = "En Hets Namn";
    private static final String FULLSTANDIGT_NAMN = "Full Standigt Namn";
    private static final String DIAGNOS_KOD = "diagnos-kod";
    private static final boolean ENKELT_INTYG = false;


    @Test
    public void toSjukfallFormat() {

        final IntygsData from = createIntygsData();
        final List<IntygData> intygDataList = IntygstjanstConverter.toSjukfallFormat(Collections.singletonList(from));

        assertThat(intygDataList).hasSize(1);

        final IntygData to = intygDataList.get(0);
        assertThat(to.getIntygId()).isEqualTo(from.getIntygsId());

        assertThat(to.getPatientId()).isEqualTo(from.getPatient().getPersonId().getExtension());
        assertThat(to.getPatientNamn()).isEqualTo(from.getPatient().getFullstandigtNamn());

        assertThat(to.getLakareId()).isEqualTo(from.getSkapadAv().getPersonalId().getExtension());
        assertThat(to.getLakareNamn()).isEqualTo(from.getSkapadAv().getFullstandigtNamn());

        assertThat(to.getDiagnosKod().getOriginalCode()).isEqualTo(from.getDiagnoskod());

        assertThat(to.getFormagor()).hasSize(1);
        assertThat(to.getFormagor().get(0).getStartdatum()).isEqualTo(from.getArbetsformaga().getFormaga().get(0).getStartdatum());
        assertThat(to.getFormagor().get(0).getSlutdatum()).isEqualTo(from.getArbetsformaga().getFormaga().get(0).getSlutdatum());
        assertThat(to.getFormagor().get(0).getNedsattning()).isEqualTo(from.getArbetsformaga().getFormaga().get(0).getNedsattning());

        assertThat(to.isEnkeltIntyg()).isEqualTo(from.isEnkeltIntyg());
        assertThat(to.getSigneringsTidpunkt()).isEqualTo(from.getSigneringsTidpunkt());
    }

    private IntygsData createIntygsData() {
        PersonId personId1 = new PersonId();
        personId1.setRoot(ROOT);
        personId1.setExtension(PERSON_NUMMER);

        Patient patient1 = new Patient();
        patient1.setPersonId(personId1);
        patient1.setFullstandigtNamn(FULLSTANDIGT_NAMN);

        HsaId hsaId1 = new HsaId();
        hsaId1.setRoot(ROOT);
        hsaId1.setExtension(HSA_ID);

        Befattning befattning1 = new Befattning();
        befattning1.setCode(LAKARE_KOD);
        befattning1.setCodeSystem(LAKARE_SYSTEM);
        befattning1.setCodeSystemName(LAKARE_SYSTEM_NAME);
        befattning1.setCodeSystemVersion(LAKARE_SYSTEM_VERSION);
        befattning1.setDisplayName(LAKARE_DISPLAY_NAME);
        befattning1.setOriginalText(LAKARE_ORIGINAL_TEXT);

        Befattningar befattningar1 = new Befattningar();
        befattningar1.getBefattning().add(befattning1);

        Vardgivare vardgivare1 = new Vardgivare();
        vardgivare1.setVardgivarId(hsaId1);
        vardgivare1.setVardgivarnamn(VARDGIVAR_NAMN);

        Enhet enhet1 = new Enhet();
        enhet1.setEnhetsId(hsaId1);
        enhet1.setEnhetsnamn(ENHETS_NAMN);
        enhet1.setVardgivare(vardgivare1);

        final HosPersonal hosPersonal1 = new HosPersonal();
        hosPersonal1.setPersonalId(hsaId1);
        hosPersonal1.setFullstandigtNamn(FULLSTANDIGT_NAMN);
        hosPersonal1.setBefattningar(befattningar1);
        hosPersonal1.setEnhet(enhet1);

        Formaga formaga1 = new Formaga();
        formaga1.setStartdatum(START_DATUM);
        formaga1.setSlutdatum(SLUT_DATUM);
        formaga1.setNedsattning(NEDSATTNING);

        Arbetsformaga arbetsformaga1 = new Arbetsformaga();
        arbetsformaga1.getFormaga().add(formaga1);

        IntygsData intygsData1 = new IntygsData();
        intygsData1.setIntygsId(INTYG_ID);
        intygsData1.setPatient(patient1);
        intygsData1.setSkapadAv(hosPersonal1);
        intygsData1.setDiagnoskod(DIAGNOS_KOD);
        intygsData1.setArbetsformaga(arbetsformaga1);
        intygsData1.setEnkeltIntyg(ENKELT_INTYG);
        intygsData1.setSigneringsTidpunkt(SIGNERING_TIDPUNKT);
        
        return intygsData1;
    }
}
