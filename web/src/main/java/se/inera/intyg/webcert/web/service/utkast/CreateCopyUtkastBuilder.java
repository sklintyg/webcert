package se.inera.intyg.webcert.web.service.utkast;

import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCopyRequest;

@Component
public class CreateCopyUtkastBuilder extends AbstractUtkastBuilder<CreateCopyRequest>{

    @Override
    public Relation createRelation(CreateCopyRequest copyRequest) {
        return null;
    }

}
