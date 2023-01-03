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
package se.inera.intyg.webcert.intygstjanststub;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ResultCodeEnum;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IIType;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsLista;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.Vardgivare;

@Service
public class ListActiveSickLeavesForCareUnitStub implements ListActiveSickLeavesForCareUnitResponderInterface {

    @Override
    public ListActiveSickLeavesForCareUnitResponseType listActiveSickLeavesForCareUnit(
        final String s, final ListActiveSickLeavesForCareUnitType parameters) {

        Optional<String> personnummer = Optional.ofNullable(parameters.getPersonId())
            .map(IIType::getExtension)
            .map(StringUtils::trim);

        Preconditions.checkArgument(personnummer.isPresent());
        Preconditions.checkArgument(StringUtils.isNotEmpty(personnummer.get()));

        final IntygsLista intygsLista = new IntygsLista();

        final HsaId hsaId = new HsaId();
        hsaId.setExtension("hsaid");
        hsaId.setRoot("root");

        final Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarId(hsaId);
        vardgivare.setVardgivarnamn("Vård Givare");

        ListActiveSickLeavesForCareUnitResponseType resp = new ListActiveSickLeavesForCareUnitResponseType();
        resp.setComment("");
        resp.setResultCode(ResultCodeEnum.OK);
        resp.setVardgivare(vardgivare);
        resp.setIntygsLista(intygsLista);

        return resp;
    }
}

