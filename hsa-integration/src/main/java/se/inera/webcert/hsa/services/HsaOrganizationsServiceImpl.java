package se.inera.webcert.hsa.services;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.ifv.hsawsresponder.v3.AttributeListType;
import se.inera.ifv.hsawsresponder.v3.AttributeValuePairType;
import se.inera.ifv.hsawsresponder.v3.CareUnitType;
import se.inera.ifv.hsawsresponder.v3.ExactType;
import se.inera.ifv.hsawsresponder.v3.GetCareUnitListResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaUnitResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonType;
import se.inera.ifv.hsawsresponder.v3.HsawsSimpleLookupResponseType;
import se.inera.ifv.hsawsresponder.v3.HsawsSimpleLookupType;
import se.inera.ifv.hsawsresponder.v3.LookupHsaObjectType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.ifv.hsawsresponder.v3.SearchOperatorExact;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;

/**
 * @author andreaskaltenbach
 */
@Service
public class HsaOrganizationsServiceImpl implements HsaOrganizationsService {

    private static final Logger LOG = LoggerFactory.getLogger(HsaOrganizationsServiceImpl.class);
    public static final String VARD_OCH_BEHANDLING = "Vård och behandling";

    @Autowired
    private HSAWebServiceCalls client;

    @Override
    public List<Vardgivare> getAuthorizedEnheterForHosPerson(String hosPersonHsaId) {
        List<Vardgivare> vardgivareList = new ArrayList<>();

        // Set hos person hsa ID
        GetMiuForPersonType parameters = new GetMiuForPersonType();
        parameters.setHsaIdentity(hosPersonHsaId);

        GetMiuForPersonResponseType response = client.callMiuRights(parameters);

        // filter by syfte. Only 'Vård och behandling' assignments are relevant for WebCert.
        Iterable<MiuInformationType> filteredMius = Iterables.filter(response.getMiuInformation(),
                new Predicate<MiuInformationType>() {
                    @Override
                    public boolean apply(MiuInformationType miuInformationType) {
                        return VARD_OCH_BEHANDLING.equalsIgnoreCase(miuInformationType.getMiuPurpose());
                    }
                });

        // group medarbetaruppdrag by vardgivare ID
        ImmutableListMultimap<String, MiuInformationType> vardgivareIdToMiuInformation = Multimaps.index(filteredMius,
                new Function<MiuInformationType, String>() {
                    @Override
                    public String apply(MiuInformationType miuInformation) {
                        return miuInformation.getCareGiver();
                    }
                });

        for (String vardgivareId : vardgivareIdToMiuInformation.keySet()) {
            vardgivareList.add(convert(vardgivareIdToMiuInformation.get(vardgivareId)));
        }
        return vardgivareList;
    }

    private List<Vardenhet> fetchAllEnheter(Vardgivare vardgivare, MiuInformationType miuInformationType) {

        List<Vardenhet> vardenheter = new ArrayList<>();

        String enhetDn = fetchDistinguishedName(miuInformationType);

        List<CareUnitType> careUnits = fetchSubEnheter(vardgivare, enhetDn);

        // TODO - check care unit start and end date!!!

        for (CareUnitType careUnit : careUnits) {
            Vardenhet vardenhet = new Vardenhet(careUnit.getHsaIdentity(), careUnit.getCareUnitName());
            attachMottagningar(vardenhet);
            vardenheter.add(vardenhet);
        }

        return vardenheter;
    }

    private List<CareUnitType> fetchSubEnheter(Vardgivare vardgivare, String enhetDn) {
        LookupHsaObjectType parameters = new LookupHsaObjectType();
        parameters.setHsaIdentity(vardgivare.getId());
        parameters.setSearchBase(enhetDn);
        GetCareUnitListResponseType response = client.callGetCareUnitList(parameters);
        return response.getCareUnits().getCareUnit();
    }

    private String fetchDistinguishedName(MiuInformationType miuInformationType) {
        HsawsSimpleLookupType lookupType = new HsawsSimpleLookupType();
        ExactType exactType = new ExactType();
        exactType.setSearchAttribute("hsaIdentity");
        exactType.setSearchOperator(SearchOperatorExact.EXACT);
        exactType.setValue(miuInformationType.getCareUnitHsaIdentity());

        AttributeListType attributeList = new AttributeListType();
        attributeList.getAttribute().add("hsaIdentity");
        lookupType.setAttributes(attributeList);
        lookupType.setLookup(exactType);

        HsawsSimpleLookupResponseType lookupResponse = client.callHsawsSimpleLookup(lookupType);

        return lookupResponse.getResponseValues().get(0).getDN();
    }

    private void attachMottagningar(Vardenhet vardenhet) {
        List<String> mottagningsIds = fetchMottagningsHsaId(vardenhet);
        for (String mottagningsId : mottagningsIds) {
            Mottagning mottagning = fetchMottagning(mottagningsId);
            vardenhet.getMottagningar().add(mottagning);
        }
    }

    private Mottagning fetchMottagning(String mottagningsHsaId) {
        GetHsaUnitResponseType response = client.callGetHsaunit(mottagningsHsaId);
        return new Mottagning(response.getHsaIdentity(), response.getName());
    }


    private List<String> fetchMottagningsHsaId(Vardenhet vardenhet) {
        List<String> ids = new ArrayList<>();

        HsawsSimpleLookupType lookupType = new HsawsSimpleLookupType();

        ExactType exactType = new ExactType();
        exactType.setSearchAttribute("hsaIdentity");
        exactType.setSearchOperator(SearchOperatorExact.EXACT);
        exactType.setValue(vardenhet.getId());

        AttributeListType attributeList = new AttributeListType();
        attributeList.getAttribute().add("hsaHealthCareUnitMember");
        lookupType.setAttributes(attributeList);

        // TODO - improve - search base can be narrowed down
        // lookupType.setSearchBase("c=SE");
        lookupType.setLookup(exactType);

        HsawsSimpleLookupResponseType lookupResponse = client.callHsawsSimpleLookup(lookupType);

        if (lookupResponse.getResponseValues().isEmpty()) {
            return ids;
        }

        List<AttributeValuePairType> attributes = lookupResponse.getResponseValues().get(0).getResponse();
        for (AttributeValuePairType attribute : attributes) {
            if ("hsaHealthCareUnitMember".equals(attribute.getAttribute())) {
                ids.addAll(attribute.getValue());
            }
        }
        
        return ids;
    }

    private Vardgivare convert(List<MiuInformationType> miuInformationTypes) {

        Vardgivare vardgivare = new Vardgivare(miuInformationTypes.get(0).getCareGiver(), miuInformationTypes.get(0)
                .getCareGiverName());

        for (MiuInformationType miuInformationType : miuInformationTypes) {

            List<Vardenhet> enheter = fetchAllEnheter(vardgivare, miuInformationType);
            vardgivare.getVardenheter().addAll(enheter);
        }
        return vardgivare;
    }
}
