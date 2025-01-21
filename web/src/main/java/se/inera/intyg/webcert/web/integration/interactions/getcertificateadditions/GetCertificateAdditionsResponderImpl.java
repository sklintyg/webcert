/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.getcertificateadditions;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.IIType;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.inera.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.AdditionType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.AmneType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.IntygAdditionsType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.StatusType;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.util.StreamUtil;

/**
 * Created by eriklupander on 2017-05-11.
 */
@SchemaValidation
public class GetCertificateAdditionsResponderImpl implements GetCertificateAdditionsResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateAdditionsResponderImpl.class);

    @Autowired
    private ArendeService arendeService;

    @Override
    @PrometheusTimeMethod
    public GetCertificateAdditionsResponseType getCertificateAdditions(
        String logicalAddress,
        GetCertificateAdditionsType request) {

        if (isNullOrEmpty(request)) {
            throw new IllegalArgumentException("Request to GetCertificateType is missing required parameter 'intygs-id'");
        }

        LocalTime start = LocalTime.now();

        GetCertificateAdditionsResponseType response = new GetCertificateAdditionsResponseType();
        response.getAdditions().addAll(new ArrayList<>());

        try {
            List<IntygId> identifiers = request.getIntygsId().stream()
                .filter(StreamUtil.distinctByKeys(IIType::getExtension))
                .collect(Collectors.toList());

            List<String> extensions = identifiers.stream()
                .map(IIType::getExtension)
                .collect(Collectors.toList());

            List<Arende> arendeList = arendeService.getArendenExternal(extensions);
            identifiers.forEach(identity -> response.getAdditions().add(buildIntygAdditionsType(identity, arendeList)));

            LOG.debug("GetCertificateAdditionsResponderImpl: Successfully returned {} arenden in {} seconds",
                response.getAdditions().stream().map(IntygAdditionsType::getAddition).mapToLong(List::size).sum(),
                getExecutionTime(start));
            response.setResult(ResultCodeType.OK);

        } catch (Exception e) {
            LOG.error("GetCertificateAdditionsResponderImpl: Failed returning arenden", e);
            response.setResult(ResultCodeType.ERROR);
        }

        return response;
    }

    private boolean isNullOrEmpty(GetCertificateAdditionsType request) {
        return request == null || request.getIntygsId() == null || request.getIntygsId().size() == 0;
    }

    private String getExecutionTime(LocalTime start) {
        return LocalTime.now()
            .minus(start.toNanoOfDay(), ChronoUnit.NANOS)
            .format(DateTimeFormatter.ofPattern("ss.SSS"));
    }

    private IntygAdditionsType buildIntygAdditionsType(IntygId intygId,
        List<Arende> arendeList) {

        List<AdditionType> additions = arendeList.stream()
            .filter(arende -> arende.getIntygsId().equals(intygId.getExtension()))
            .filter(arende -> !arende.getAmne().equals(ArendeAmne.PAMINN))
            .map(this::mapArende)
            .collect(Collectors.toList());

        IntygAdditionsType intygAdditionsType = new IntygAdditionsType();
        intygAdditionsType.setIntygsId(intygId);
        intygAdditionsType.getAddition().addAll(additions);

        return intygAdditionsType;
    }

    private AdditionType mapArende(Arende arende) {
        AdditionType additionType = new AdditionType();
        additionType.setId(String.valueOf(arende.getId()));
        additionType.setSkapad(arende.getTimestamp());
        additionType.setStatus(mapStatus(arende.getStatus(), arende.getSvarPaId()));
        additionType.setAmne(mapAmne(arende.getAmne()));

        return additionType;
    }

    private AmneType mapAmne(ArendeAmne amne) {
        return AmneType.valueOf(amne.name());
    }

    private StatusType mapStatus(Status status, String svarPaId) {
        boolean isAnswerFromExternal = status == Status.ANSWERED && svarPaId != null && !svarPaId.isEmpty();
        if (isAnswerFromExternal) {
            return StatusType.BESVARAD;
        }
        switch (status) {
            case CLOSED:
            case PENDING_EXTERNAL_ACTION:
                return StatusType.BESVARAD;
            default:
                return StatusType.OBESVARAD;
        }
    }


}
