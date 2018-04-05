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

import se.inera.intyg.webcert.web.converter.util.ConvertToFKTypes;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;

/**
 * Created by pehr on 10/2/13.
 */
public final class FKQuestionConverter {

    private FKQuestionConverter() {

    }

    public static QuestionToFkType convert(FragaSvar fs) {
        QuestionToFkType fkQuestion = new QuestionToFkType();

        fkQuestion.setAmne(ConvertToFKTypes.toAmneTyp(fs.getAmne()));
        fkQuestion.setAdressVard(ConvertToFKTypes.toVardAdresseringsType(fs.getVardperson()));

        fkQuestion.setAvsantTidpunkt(fs.getFrageSkickadDatum());
        fkQuestion.setFraga(ConvertToFKTypes.toInnehallType(fs.getFrageText(), fs.getFrageSigneringsDatum()));

        fkQuestion.setLakarutlatande(ConvertToFKTypes.toLakarUtlatande(fs.getIntygsReferens()));
        fkQuestion.setVardReferensId(fs.getInternReferens().toString());

        return fkQuestion;
    }
}
