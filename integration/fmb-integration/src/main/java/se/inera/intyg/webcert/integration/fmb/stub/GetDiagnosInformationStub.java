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

package se.inera.intyg.webcert.integration.fmb.stub;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.DiagnosInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.OvrigFmbInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.VersionType;

public class GetDiagnosInformationStub implements GetDiagnosInformationResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GetDiagnosInformationStub.class);

    public GetDiagnosInformationStub() {
        LOG.info("Starting stub: FMB GetDiagnosInformationStub");
    }

    @Override
    public GetDiagnosInformationResponseType getDiagnosInformation(String s, GetDiagnosInformationType getDiagnosInformationType) {
        try {
            JAXBContext jbc = JAXBContext.newInstance(GetDiagnosInformationResponseType.class);
            Unmarshaller u = jbc.createUnmarshaller();
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            Resource resource = r.getResource("GetDiagnosInformationResponse.xml");
            GetDiagnosInformationResponseType value = u
                    .unmarshal(new StreamSource(resource.getInputStream()), GetDiagnosInformationResponseType.class).getValue();
            addHardcodedInfo(value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        final GetDiagnosInformationResponseType diagnosInformationResponse = new GetDiagnosInformationResponseType();
        addHardcodedInfo(diagnosInformationResponse);
        return diagnosInformationResponse;
    }

    private void addHardcodedInfo(GetDiagnosInformationResponseType diagnosInformationResponse) {
        final VersionType version = new VersionType();
        version.setSenateAndring(String.valueOf(System.currentTimeMillis()));
        diagnosInformationResponse.setVersion(version);

        final DiagnosInformationType diagnosInformation1 = new DiagnosInformationType();
        diagnosInformation1.setAktivitetsbegransningBeskrivning(
                "Akut bronkit påverkar inte funktionstillståndet, bortsett från irriterande hosta i normalfallet. Bakteriell sekundärinfektion kan påverka allmäntillståndet genom att patienten blir trött.");
        diagnosInformation1.setFunktionsnedsattningBeskrivning(
                "Tillståndet är vanligtvis kortvarigt och varar några dagar till några veckor. Ibland får patienten hosta under flera månader vilket är ett uttryck för en sekundärinfektion.");

        final OvrigFmbInformationType ovrigFmbInformation1 = new OvrigFmbInformationType();
        ovrigFmbInformation1.setGenrellInformation(
                "Återkommande akuta bronkiter hos rökare bör medföra rökstopp. Bihåleinflammationer efter viroser kan ligga bakom återkommande akuta bronkiter. Långvarig bronkit kan bero på twar eller infektion med mykoplasma pneumoni. \n\nHos patienter med samtidig annan luftvägs- eller lungsjukdom som exempelvis astma eller kol kan symtomen vid akut bronkit bli mer uttalade och funktionsnedsättningen bli mer långdragen.");
        ovrigFmbInformation1.setSymtomPrognosBehandling(
                "Akut bronkit orsakas vanligen av luftvägsinflammation och epitelskada (skador på hud och slemhinnor i kroppen) efter vanlig virusförkylning. Akut bronkit kan ge hosta under flera månader och är ofta tecken på inflammation i bronkerna. Symtom är akut påkommande torr eller slemmig hosta.");

        diagnosInformation1.setOvrigFmbInformation(ovrigFmbInformation1);
        diagnosInformation1.getHuvuddiagnos().add(GetFmbStub.createHuvuddiagnos("J20"));
        diagnosInformation1.getHuvuddiagnos().add(GetFmbStub.createHuvuddiagnos("J22"));

        // Gör en 3-ställig diagnos
        final DiagnosInformationType diagnosInformation3 = new DiagnosInformationType();
        diagnosInformation3.setAktivitetsbegransningBeskrivning("Andra kristallartropatier");
        diagnosInformation3.setFunktionsnedsattningBeskrivning("Andra kristallartropatier");

        final OvrigFmbInformationType ovrigFmbInformation3 = new OvrigFmbInformationType();
        ovrigFmbInformation3.setGenrellInformation("Andra kristallartropatier");
        ovrigFmbInformation3.setSymtomPrognosBehandling("Andra kristallartropatier");

        diagnosInformation3.setOvrigFmbInformation(ovrigFmbInformation3);
        diagnosInformation3.getHuvuddiagnos().add(GetFmbStub.createHuvuddiagnos("M11"));

        // Gör en 4-ställig diagnos
        final DiagnosInformationType diagnosInformation4 = new DiagnosInformationType();
        diagnosInformation4.setAktivitetsbegransningBeskrivning("Andra specificerade kristallartropatier");
        diagnosInformation4.setFunktionsnedsattningBeskrivning("Andra specificerade kristallartropatier");

        final OvrigFmbInformationType ovrigFmbInformation4 = new OvrigFmbInformationType();
        ovrigFmbInformation4.setGenrellInformation("Andra specificerade kristallartropatier");
        ovrigFmbInformation4.setSymtomPrognosBehandling("Andra specificerade kristallartropatier");

        diagnosInformation4.setOvrigFmbInformation(ovrigFmbInformation4);
        diagnosInformation4.getHuvuddiagnos().add(GetFmbStub.createHuvuddiagnos("M118"));

        // Gör en 5-ställig diagnos
        final DiagnosInformationType diagnosInformation5 = new DiagnosInformationType();
        diagnosInformation5.setAktivitetsbegransningBeskrivning("Ett träben kan svårgöra fysiskt arbete, men arbete till sjöss påverkas inte.");
        diagnosInformation5.setFunktionsnedsattningBeskrivning("Normal funktionsnedsättning vid träben.");

        final OvrigFmbInformationType ovrigFmbInformation5 = new OvrigFmbInformationType();
        ovrigFmbInformation5.setGenrellInformation("Pyrofosfatartrit i knäled");
        ovrigFmbInformation5.setSymtomPrognosBehandling("Pyrofosfatartrit i knäled, förslagsvis erhåller patienten ett träben.");

        diagnosInformation5.setOvrigFmbInformation(ovrigFmbInformation5);
        diagnosInformation5.getHuvuddiagnos().add(GetFmbStub.createHuvuddiagnos("M118G"));

        diagnosInformationResponse.getDiagnosInformation().add(diagnosInformation1);
        diagnosInformationResponse.getDiagnosInformation().add(diagnosInformation3);
        diagnosInformationResponse.getDiagnosInformation().add(diagnosInformation4);
        // diagnosInformationResponse.getDiagnosInformation().add(diagnosInformation5);
    }

}
