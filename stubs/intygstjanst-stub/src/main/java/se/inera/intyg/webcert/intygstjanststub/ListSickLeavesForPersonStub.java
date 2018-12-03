package se.inera.intyg.webcert.intygstjanststub;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IIType;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsLista;
import java.util.Optional;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ResultCodeEnum;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ResultType;

@Service
public class ListSickLeavesForPersonStub implements ListSickLeavesForPersonResponderInterface {

    @Override
    public ListSickLeavesForPersonResponseType listSickLeavesForPerson(String s, ListSickLeavesForPersonType parameters) {

        Optional<String> personnummer = Optional.ofNullable(parameters.getPersonId())
                .map(IIType::getExtension)
                .map(StringUtils::trim);

        Preconditions.checkArgument(personnummer.isPresent());
        Preconditions.checkArgument(StringUtils.isNotEmpty(personnummer.get()));

        final IntygsLista intygsLista = new IntygsLista();

        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeEnum.OK);

        ListSickLeavesForPersonResponseType resp = new ListSickLeavesForPersonResponseType();
        resp.setResult(resultType);
        resp.setIntygsLista(intygsLista);

        return resp;
    }
}

