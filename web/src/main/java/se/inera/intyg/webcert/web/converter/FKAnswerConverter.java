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
package se.inera.intyg.webcert.web.converter;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.KompletteringType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.web.converter.util.ConvertToFKTypes;


/**
 * Created by pehr on 10/2/13.
 */
public final class FKAnswerConverter {

    private FKAnswerConverter() {
    }

    public static AnswerToFkType convert(FragaSvar fs) {
        AnswerToFkType fkAnswer = new AnswerToFkType();

        fkAnswer.setAmne(ConvertToFKTypes.toAmneTyp(fs.getAmne()));
        fkAnswer.setAdressVard(ConvertToFKTypes.toVardAdresseringsType(fs.getVardperson()));

        fkAnswer.setAvsantTidpunkt(fs.getFrageSkickadDatum());
        if (fs.getMeddelandeRubrik() != null) {
            fkAnswer.setFkMeddelanderubrik(fs.getMeddelandeRubrik());
        }

        fkAnswer.setFkReferensId(fs.getExternReferens());
        fkAnswer.setVardReferensId(fs.getInternReferens().toString());

        if (fs.getSistaDatumForSvar() != null) {
            fkAnswer.setFkSistaDatumForSvar(fs.getSistaDatumForSvar());
        }

        fkAnswer.setFraga(ConvertToFKTypes.toInnehallType(fs.getFrageText(), fs.getFrageSigneringsDatum()));
        fkAnswer.setSvar(ConvertToFKTypes.toInnehallType(fs.getSvarsText(), fs.getSvarSigneringsDatum()));

        fkAnswer.setLakarutlatande(ConvertToFKTypes.toLakarUtlatande(fs.getIntygsReferens()));

        if (fs.getKompletteringar() != null) {
            for (Komplettering komplettering : fs.getKompletteringar()) {
                KompletteringType kt = new KompletteringType();
                kt.setFalt(komplettering.getFalt());
                kt.setText(komplettering.getText());
                fkAnswer.getFkKomplettering().add(kt);
            }
        }

        return fkAnswer;
    }

}
