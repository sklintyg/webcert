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
package se.inera.intyg.webcert.integration.tak.stub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.tak.model.ConnectionPoint;
import se.inera.intyg.webcert.integration.tak.model.ServiceContract;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

@Service("takStub")
@Path("/")
public class TakStub {
    private static final Logger LOG = LoggerFactory.getLogger(TakStub.class);

    private static final String CERT_STATUS_FOR_CARE_V3_NS =
            "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";
    private static final String RECEIVE_MEDICAL_CERT_QUESTION_NS =
            "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1";
    private static final String RECEIVE_MEDICAL_CERT_ANSWER_NS =
            "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1";
    private static final String SEND_MESSAGE_TO_CARE_NS =
            "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2";

    private ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("connectionPoints")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response getConnectionPoint(@QueryParam("platform") String platform, @QueryParam("environment") String env)
            throws IOException {
        LOG.debug("Stub got getConnectionPoint request");
        URL jsonUrl = getClass().getResource("/responses/connectionPointResponse.json");
        ConnectionPoint[] cP = mapper.readValue(jsonUrl, ConnectionPoint[].class);
        cP[0].setEnvironment(env);
        cP[0].setPlatform(platform);
        return Response.ok(mapper.writeValueAsString(cP)).build();
    }

    // CHECKSTYLE:OFF MagicNumber
    @GET
    @Path("serviceContracts")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response getServiceContracts(@QueryParam("namespace") String ns) throws IOException {
        LOG.debug("Stub got getServiceContracts request for {}", ns);
        URL jsonUrl = getClass().getResource("/responses/serviceContractResponse.json");
        ServiceContract[] sC = mapper.readValue(jsonUrl, ServiceContract[].class);
        int dummyId = 10;
        switch (ns) {
            case CERT_STATUS_FOR_CARE_V3_NS:
                dummyId = 11;
                break;
            case RECEIVE_MEDICAL_CERT_ANSWER_NS:
                dummyId = 12;
                break;
            case RECEIVE_MEDICAL_CERT_QUESTION_NS:
                dummyId = 13;
                break;
            case SEND_MESSAGE_TO_CARE_NS:
                dummyId = 14;
                break;
        }
        sC[0].setId(String.valueOf(dummyId));
        sC[0].setNamespace(ns);
        return Response.ok(mapper.writeValueAsString(sC)).build();
    }
    // CHECKSTYLE:ON MagicNumber

    // This is intentionally misspelled to mimic the actual API at NTJP...
    @GET
    @Path("logicalAddresss")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response getLogicalAddress(@QueryParam("logicalAdress") String hsaId,
                                      @QueryParam("connectionPointId") String connectionPointId,
                                      @QueryParam("serviceContractId") String serviceContractId) throws IOException {
        LOG.debug("Stub got getLogicalAddress request");
        URL jsonUrl = getClass().getResource("/responses/takningar.json");
        TakLogicalAddress[] takLogicalAddress = mapper.readValue(jsonUrl, TakLogicalAddress[].class);
        takLogicalAddress[0].setLogicalAddress(hsaId);
        takLogicalAddress[0].setDescription("Some description");
        return Response.ok(mapper.writeValueAsString(takLogicalAddress)).build();
    }
}
