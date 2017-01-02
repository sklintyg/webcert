/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import java.util.*;
import java.util.stream.Stream;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.*;

import io.swagger.annotations.*;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.persistence.fmb.model.Fmb;
import se.inera.intyg.webcert.persistence.fmb.model.FmbType;
import se.inera.intyg.webcert.persistence.fmb.repository.FmbRepository;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.*;

@Path("/fmb")
@Api(value = "fmb", description = "REST API för Försäkringsmedicinskt beslutsstöd", produces = MediaType.APPLICATION_JSON)
public class FmbApiController extends AbstractApiController {

    private static final int MIN_ICD10_POSITION = 3;

    private static final Logger LOG = LoggerFactory.getLogger(FmbApiController.class);

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;

    @Autowired
    private FmbRepository fmbRepository;

    @Autowired
    private DiagnosService diagnosService;

    @GET
    @Path("/{icd10}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get FMB data for ICD10 codes", httpMethod = "GET", notes = "Fetch the admin user details", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "Given FMB data for icd10 code found", response = FmbResponse.class),
            @ApiResponse(code = BAD_REQUEST, message = "Bad request due to missing icd10 code")
    })
    public Response getFmbForIcd10(@ApiParam(value = "ICD10 code", required = true) @PathParam("icd10") String icd10) {
        if (icd10 == null || icd10.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing icd10 code").build();
        }
        final FmbResponse result = getFmbResponse(icd10.toUpperCase(Locale.ENGLISH));
        return Response.ok(result).build();
    }

    /**
     * Create response structure, mapping fmb specific names to external generic naming to be used for many intygstypes.
     *
     * @param icd10
     * @return
     */
    private FmbResponse getFmbResponse(String icd10) {
        final List<FmbForm> forms = new ArrayList<>(FmbFormName.values().length);

        String icd10WithFmb = checkIcd10ForFmbInfo(icd10);

        String icd10Description = getDiagnoseDescriptionForIcd10Code(icd10WithFmb);

        forms.add(getFmbForm(icd10WithFmb, FmbFormName.DIAGNOS, FmbType.SYMPTOM_PROGNOS_BEHANDLING, FmbType.GENERELL_INFO));
        forms.add(getFmbForm(icd10WithFmb, FmbFormName.FUNKTIONSNEDSATTNING, FmbType.FUNKTIONSNEDSATTNING));
        forms.add(getFmbForm(icd10WithFmb, FmbFormName.AKTIVITETSBEGRANSNING, FmbType.AKTIVITETSBEGRANSNING));
        forms.add(getFmbForm(icd10WithFmb, FmbFormName.ARBETSFORMAGA, FmbType.BESLUTSUNDERLAG_TEXTUELLT));
        return new FmbResponse(icd10WithFmb, icd10Description, Lists.newArrayList(Iterables.filter(forms, Predicates.notNull())));
    }

    private String getDiagnoseDescriptionForIcd10Code(String icd10WithFmb) {
        DiagnosResponse response = diagnosService.getDiagnosisByCode(icd10WithFmb, Diagnoskodverk.ICD_10_SE);
        if (!response.getResultat().equals(DiagnosResponseType.OK)) {
            LOG.info("Failed to get diagnose description for {} with result {}", icd10WithFmb, response.getResultat().name());
            return null;
        } else {
            return response.getDiagnoser().get(0).getBeskrivning();
        }
    }

    private String checkIcd10ForFmbInfo(String icd10) {
        String icd10WithFmbInfo = icd10;
        while (icd10WithFmbInfo.length() >= MIN_ICD10_POSITION) {
            if (fmbContentExists(icd10WithFmbInfo, FmbType.values())) {
                return icd10WithFmbInfo;
            }
            // Make the icd10-code one position shorter, and thus more general.
            icd10WithFmbInfo = StringUtils.chop(icd10WithFmbInfo);
        }
        return icd10;
    }

    private boolean fmbContentExists(String icd10Code, FmbType... types) {
        return Stream.of(types)
                .anyMatch(t -> getFmbContent(icd10Code, t) != null);
    }

    private FmbForm getFmbForm(String icd10, FmbFormName name, FmbType... fmbTypes) {
        final List<FmbContent> contents = new ArrayList<>(fmbTypes.length);
        for (FmbType fmbType : fmbTypes) {
            FmbContent fmbContent = getFmbContent(icd10, fmbType);
            if (fmbContent != null) {
                contents.add(fmbContent);
            }
        }
        if (contents.isEmpty()) {
            return null;
        }
        return new FmbForm(name, contents);
    }

    private FmbContent getFmbContent(String icd10, FmbType fmbType) {
        final List<Fmb> fmbs = fmbRepository.findByIcd10AndTyp(icd10, fmbType);

        if (fmbs == null || fmbs.isEmpty()) {
            LOG.info("No FMB information for ICD10 '{}' and type '{}'", icd10, fmbType);
            return null;
        }

        final List<String> texts = Lists.transform(fmbs, new Function<Fmb, String>() {
            @Override
            public String apply(Fmb fmb) {
                if (fmb == null) {
                    return "";
                }
                return fmb.getText();
            }
        });
        final List<String> textsWithoutDuplicates = Lists.newArrayList(Sets.newHashSet(texts));

        if (textsWithoutDuplicates.size() == 1) {
            return new FmbContent(fmbType, textsWithoutDuplicates.get(0));
        }

        return new FmbContent(fmbType, textsWithoutDuplicates);
    }

}
