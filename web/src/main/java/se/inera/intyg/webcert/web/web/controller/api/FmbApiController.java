package se.inera.intyg.webcert.web.web.controller.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.persistence.fmb.model.Fmb;
import se.inera.webcert.persistence.fmb.model.FmbType;
import se.inera.webcert.persistence.fmb.repository.FmbRepository;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbContent;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbForm;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbFormName;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/fmb")
@Api(value = "fmb", description = "REST API för Försäkringsmedicinskt beslutsstöd", produces = MediaType.APPLICATION_JSON)
public class FmbApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FmbApiController.class);

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;

    @Autowired
    private FmbRepository fmbRepository;

    @GET
    @Path("/{icd10}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get FMB data for ICD10 codes", httpMethod = "GET", notes = "Fetch the admin user details", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = OK, message = "Given FMB data for icd10 code found", response = FmbResponse.class),
            @ApiResponse(code = BAD_REQUEST, message = "Bad request due to missing icd10 code the data")
    })
    public Response getFmbForIcd10(@ApiParam(value = "ICD10 code", required = true) @PathParam("icd10") String icd10) {
        if (icd10 == null || icd10.isEmpty()) {
           return Response.status(Response.Status.BAD_REQUEST).entity("Missing icd10 code").build();
        }
        final FmbResponse result = getFmbResponse(icd10.toUpperCase(Locale.ENGLISH));
        return Response.ok(result).build();
    }

    private FmbResponse getFmbResponse(String icd10) {
        final List<FmbForm> forms = new ArrayList<>(FmbFormName.values().length);
        forms.add(getFmbForm(icd10, FmbFormName.FORM2, FmbType.FALT2_SPB, FmbType.FALT2_GENERAL));
        forms.add(getFmbForm(icd10, FmbFormName.FORM4, FmbType.FALT4));
        forms.add(getFmbForm(icd10, FmbFormName.FORM5, FmbType.FALT5));
        forms.add(getFmbForm(icd10, FmbFormName.FORM8B, FmbType.FALT8B));
        return new FmbResponse(icd10, Lists.newArrayList(Iterables.filter(forms, Predicates.notNull())));
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

        if ((fmbs == null) || fmbs.isEmpty()) {
            LOG.info("No FMB information for ICD10 '{}' and type '{}'", icd10, fmbType);
            return null;
        }

        if (fmbs.size() == 1) {
            return new FmbContent(fmbType, fmbs.get(0).getText());
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
        return new FmbContent(fmbType, texts);
    }

}
