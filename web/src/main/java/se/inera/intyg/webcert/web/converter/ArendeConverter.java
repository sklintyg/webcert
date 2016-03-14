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

package se.inera.intyg.webcert.web.converter;

import java.util.stream.Collectors;

import se.inera.intyg.webcert.persistence.arende.model.*;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.v2.MeddelandeReferens;

public final class ArendeConverter {

    private ArendeConverter() {
    }

    public static Arende convert(SendMessageToCareType request) {
        Arende res = new Arende();
        res.setAmne(ArendeAmne.valueOf(request.getAmne().getCode()));
        res.setIntygsId(request.getIntygsId().getExtension());
        res.getKontaktInfo().addAll(request.getSkickatAv().getKontaktInfo());
        res.getKomplettering().addAll(request.getKomplettering().stream().map(ArendeConverter::convert).collect(Collectors.toList()));
        res.setMeddelande(request.getMeddelande());
        res.setMeddelandeId(request.getMeddelandeId());
        res.setPaminnelseMeddelandeId(request.getPaminnelseMeddelandeId());
        res.setPatientPersonId(request.getPatientPersonId().getExtension());
        res.setReferensId(request.getReferensId());
        res.setRubrik(request.getRubrik());
        res.setSistaDatumForSvar(request.getSistaDatumForSvar());
        res.setSkickatAv(request.getSkickatAv().getPart().getCode());
        res.setSkickatTidpunkt(request.getSkickatTidpunkt());
        if (request.getSvarPa() != null) {
            res.setSvarPaId(request.getSvarPa().getMeddelandeId());
            res.setSvarPaReferens(extractReferensId(request.getSvarPa()));
        }
        return res;
    }

    // There are between 0 and 1 referensid in the MeddelandeReferens according to specification 2.0.RC3
    // Because of this we get the first item if there exists one
    private static String extractReferensId(MeddelandeReferens meddelandeReferens) {
        return meddelandeReferens.getReferensId() != null && !meddelandeReferens.getReferensId().isEmpty()
                ? meddelandeReferens.getReferensId().get(0)
                : null;
    }

    private static MedicinsktArende convert(Komplettering k) {
        MedicinsktArende res = new MedicinsktArende();
        res.setFrageId(k.getFrageId());
        res.setText(k.getText());
        res.setInstans(k.getInstans());
        return res;
    }
}
