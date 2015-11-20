package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import se.inera.webcert.persistence.fragasvar.model.Amne;

public class CreateQuestionParameter {
    private Amne amne;
    private String frageText;

    public Amne getAmne() {
        return amne;
    }

    public void setAmne(Amne amne) {
        this.amne = amne;
    }

    public String getFrageText() {
        return frageText;
    }

    public void setFrageText(String frageText) {
        this.frageText = frageText;
    }
}
