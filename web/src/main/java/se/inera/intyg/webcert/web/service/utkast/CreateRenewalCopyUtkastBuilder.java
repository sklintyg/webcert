package se.inera.intyg.webcert.web.service.utkast;

import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelHolder;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;

@Component
public class CreateRenewalCopyUtkastBuilder extends AbstractUtkastBuilder<CreateRenewalCopyRequest> {

    @Override
    public Relation createRelation(CreateRenewalCopyRequest copyRequest) {
        return createRelation(copyRequest, RelationKod.FRLANG);
    }

    private Relation createRelation(CreateRenewalCopyRequest request, RelationKod relationKod) {
        Relation relation = new Relation();
        relation.setRelationIntygsId(request.getOriginalIntygId());
        relation.setRelationKod(relationKod);
        relation.setMeddelandeId(request.getMeddelandeId());
        return relation;
    }

    // @Override // TODO:
    // protected Utkast buildUtkastCopy(CreateRenewalCopyRequest copyRequest, String utkastId, String utkastTyp, boolean
    // addRelation, Relation relation,
    // String draftCopyJson, UtkastStatus utkastStatus) {
    // Utkast utkast = new Utkast();
    //
    // utkast.setIntygsId(utkastId);
    // utkast.setIntygsTyp(utkastTyp);
    // utkast.setStatus(utkastStatus);
    // utkast.setModel(draftCopyJson);
    //
    // if (addRelation) {
    // enrichWithRelation(utkast, relation);
    // }
    //
    // populateUtkastWithVardenhetAndHoSPerson(utkast, copyRequest);
    //
    // return utkast;
    //
    // return super.buildUtkastCopy(copyRequest, utkastId, utkastTyp, addRelation, relation, draftCopyJson,
    // utkastStatus);
    // }
    
    @Override
    protected InternalModelResponse getInternalModel(Utkast orgUtkast, ModuleApi moduleApi, CreateDraftCopyHolder draftCopyHolder)
            throws ModuleException {
        InternalModelResponse draftResponse = moduleApi.createRenewalFromTemplate(draftCopyHolder,
                new InternalModelHolder(orgUtkast.getModel()));
        return draftResponse;
    }

}
