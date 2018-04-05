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
package se.inera.intyg.webcert.integration.fmb.stub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.fmb.model.Kod;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.Attributes;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxData;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;
import se.inera.intyg.webcert.integration.fmb.model.typfall.TypfallData;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

// Fullständig URL till endpoint, ex: http://localhost:9088/services/fmbstubs/fmbstub/typfall
@Service("fmbStub")
@Path("/fmbstub")
public class FmbStub {

    private static final Logger LOG = LoggerFactory.getLogger(FmbStub.class);

    private ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("typfall")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response getTypfall() throws IOException {
        final URL typfallJson = getClass().getResource("/TypfallStubResponse.json");
        final Typfall typfall = mapper.readValue(typfallJson, Typfall.class);
        addHardcodedInfo(typfall);
        return Response.ok(mapper.writeValueAsString(typfall)).build();

    }

    @GET
    @Path("forsakringsmedicinskdiagnosinformation")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response getForsakringsmedicinskDiagnosinformation() throws IOException {
        final URL typfallJson = getClass().getResource("/FmdxInfoStubResponse.json");
        final FmdxInformation fmdxInformation = mapper.readValue(typfallJson, FmdxInformation.class);
        addHardcodedInfo(fmdxInformation);
        return Response.ok(mapper.writeValueAsString(fmdxInformation)).build();
    }

    private <T> T copy(T model, Class<T> tClass) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final byte[] bytes = objectMapper.writeValueAsBytes(model);
        return objectMapper.readValue(bytes, tClass);
    }

    private void addHardcodedInfo(Typfall typfall) {
        final List<TypfallData> data = typfall.getData();
        final TypfallData typfallData = data.get(0);
        try {
            final TypfallData copy = copy(typfallData, TypfallData.class);
            final se.inera.intyg.webcert.integration.fmb.model.typfall.Attributes attributes = copy.getAttributes();
            attributes.setTypfallsmening("Akut bronkit nedsätter normalt inte arbetsförmågan. Om patienten har långvarig svår hosta "
                    + "kan det möjligen påverka allmäntillståndet genom att patienten blir trött. Sjukskrivning enbart i undantagsfall "
                    + "vid tydligt nedsatt allmäntillstånd i upp till 2 veckor. Röstkrävande yrken kan behöva längre sjukskrivning.");
            attributes.getFmbtillstand().setDiagnoskod(Arrays.asList(getDiagnoskod("J22"), getDiagnoskod("J20")));
            data.add(copy);
        } catch (IOException e) {
            LOG.error("Failed to insert hard coded FMB typfall info", e);
        }
    }

    private void addHardcodedInfo(FmdxInformation fmdxInformation) {
        final List<FmdxData> data = fmdxInformation.getData();
        final FmdxData fmdxData = data.get(0);
        try {
            final FmdxData copy = copy(fmdxData, FmdxData.class);
            final Attributes attributes = copy.getAttributes();
            attributes.setDiagnoskod(Arrays.asList(getDiagnoskod("J22"), getDiagnoskod("J20")));
            attributes.getAktivitetsbegransning().setAktivitetsbegransningsbeskrivning("Akut bronkit påverkar inte "
                    + "funktionstillståndet, bortsett från irriterande hosta i normalfallet. "
                    + "Bakteriell sekundärinfektion kan påverka allmäntillståndet genom att patienten blir trött.");
            attributes.getFunktionsnedsattning().setFunktionsnedsattningsbeskrivning("Tillståndet är vanligtvis "
                    + "kortvarigt och varar några dagar till några veckor. "
                    + "Ibland får patienten hosta under flera månader vilket är ett uttryck för en sekundärinfektion.");
            attributes.getForsakringsmedicinskinformation().setMarkup("Återkommande akuta bronkiter hos rökare bör "
                    + "medföra rökstopp. Bihåleinflammationer efter viroser kan ligga bakom "
                    + "återkommande akuta bronkiter. Långvarig bronkit kan bero på twar eller infektion med "
                    + "mykoplasma pneumoni. "
                    + "\n\nHos patienter med samtidig annan luftvägs- eller lungsjukdom som exempelvis astma eller "
                    + "kol kan symtomen "
                    + "vid akut bronkit bli mer uttalade och funktionsnedsättningen bli mer långdragen.");
            attributes.getSymtomprognosbehandling().setMarkup("Akut bronkit orsakas vanligen av luftvägsinflammation "
                    + "och epitelskada (skador på hud och slemhinnor i kroppen) "
                    + "efter vanlig virusförkylning. Akut bronkit kan ge hosta under flera månader och är ofta "
                    + "tecken på inflammation "
                    + "i bronkerna. Symtom är akut påkommande torr eller slemmig hosta.");
            data.add(copy);
        } catch (IOException e) {
            LOG.error("Failed to insert hard coded FMB Dx info", e);
        }
    }

    private Kod getDiagnoskod(String kod) {
        final Kod diagnoskod = new Kod();
        diagnoskod.setKod(kod);
        return diagnoskod;
    }

}
