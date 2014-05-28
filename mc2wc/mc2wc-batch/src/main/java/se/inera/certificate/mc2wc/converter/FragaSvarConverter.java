package se.inera.certificate.mc2wc.converter;

import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

public interface FragaSvarConverter {

    FragaSvar toFragaSvar(QuestionType qa);

}
