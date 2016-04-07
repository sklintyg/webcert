package se.inera.intyg.webcert.web.service.utkast;

import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;

@Component
public class CopyCompletionUtkastBuilder extends AbstractUtkastBuilder<CreateCompletionCopyRequest> {

    @Override
    public Relation createRelation(CreateCompletionCopyRequest copyRequest) {
        return createRelation(copyRequest, RelationKod.KOMPLT);
    }

    private Relation createRelation(CreateCompletionCopyRequest request, RelationKod relationKod) {
        Relation relation = new Relation();
        relation.setRelationIntygsId(request.getOriginalIntygId());
        relation.setRelationKod(relationKod);
        relation.setMeddelandeId(request.getMeddelandeId());
        return relation;
    }
}
