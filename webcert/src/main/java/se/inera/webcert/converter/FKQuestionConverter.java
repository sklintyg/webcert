package se.inera.webcert.converter;

import se.inera.webcert.converter.util.ConvertToFKTypes;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;

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
