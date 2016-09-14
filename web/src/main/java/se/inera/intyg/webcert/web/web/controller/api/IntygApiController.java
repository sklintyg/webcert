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

package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import se.inera.intyg.common.security.authorities.AuthoritiesHelper;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.*;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.NotifiedState;

import javax.persistence.OptimisticLockException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Controller for the API that serves WebCert.
 *
 * @author nikpet
 *
 */
@Path("/intyg")
@Api(value = "intyg", description = "REST API för intygshantering", produces = MediaType.APPLICATION_JSON)
public class IntygApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(IntygApiController.class);

    private static final List<UtkastStatus> ALL_DRAFTS = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.DRAFT_INCOMPLETE);

    private static final String OFFLINE_MODE = "offline_mode";

    @Autowired
    private IntygService intygService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    public IntygApiController() {
    }

    /**
     * Compiles a list of Intyg from two data sources. Signed Intyg are
     * retrieved from Intygstjänst, drafts are retrieved from Webcerts db. Both
     * types of Intyg are converted and merged into one sorted list.
     *
     * @param personNummerIn
     *            personnummer
     * @return a Response carrying a list containing all Intyg for a person.
     */
    @GET
    @Path("/person/{personNummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response listDraftsAndIntygForPerson(@PathParam("personNummer") String personNummerIn) {
        Personnummer personNummer = new Personnummer(personNummerIn);
        LOG.debug("Retrieving intyg for person {}", personNummer.getPnrHash());

        List<String> enhetsIds = getEnhetIdsForCurrentUser();

        if (enhetsIds.isEmpty()) {
            LOG.error("Current user has no assignments");
            return Response.status(Status.BAD_REQUEST).build();
        }

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(enhetsIds, personNummer);
        LOG.debug("Got #{} intyg", intygItemListResponse.getLeft().size());

        List<Utkast> utkastList;

        if (authoritiesValidator.given(getWebCertUserService().getUser()).features(WebcertFeature.HANTERA_INTYGSUTKAST).isVerified()) {
            Set<String> intygstyper = authoritiesHelper.getIntygstyperForPrivilege(getWebCertUserService().getUser(),
                    AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

            utkastList = utkastRepository.findDraftsByPatientAndEnhetAndStatus(
                    DaoUtil.formatPnrForPersistence(personNummer),
                    enhetsIds,
                    ALL_DRAFTS,
                    intygstyper);

            LOG.debug("Got #{} utkast", utkastList.size());
        } else {
            utkastList = Collections.emptyList();
        }

        List<ListIntygEntry> allIntyg = IntygDraftsConverter.merge(intygItemListResponse.getLeft(), utkastList);

        Response.ResponseBuilder responseBuilder = Response.ok(allIntyg);
        if (intygItemListResponse.getRight()) {
            responseBuilder = responseBuilder.header(OFFLINE_MODE, Boolean.TRUE.toString());
        }

        return responseBuilder.build();
    }

    /**
     * Sets the notified flag on an Intyg.
     *
     * @param intygsId
     *            Id of the Intyg
     * @param notifiedState
     *            True or False
     * @return
     *         Response
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}/vidarebefordra")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNotifiedOnIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version, NotifiedState notifiedState) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_UTKAST)
                .orThrow();

        Utkast updatedIntyg;
        try {
            updatedIntyg = utkastService.setNotifiedOnDraft(intygsId, version, notifiedState.isNotified());
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        LOG.debug("Set forward to {} on intyg {} with id '{}'",
                new Object[] { updatedIntyg.getVidarebefordrad(), intygsTyp, updatedIntyg.getIntygsId() });

        ListIntygEntry intygEntry = IntygDraftsConverter.convertUtkastToListIntygEntry(updatedIntyg);

        return Response.ok(intygEntry).build();
    }
}
