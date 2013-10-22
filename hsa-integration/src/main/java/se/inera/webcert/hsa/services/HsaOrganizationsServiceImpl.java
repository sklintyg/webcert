package se.inera.webcert.hsa.services;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import se.inera.webcert.hsa.stub.Medarbetaruppdrag;

/**
 * @author andreaskaltenbach
 */
@Service
public class HsaOrganizationsServiceImpl implements HsaOrganizationsService {

    private static final Logger LOG = LoggerFactory.getLogger(HsaOrganizationsServiceImpl.class);

    @Autowired
    private HSAWebServiceCalls client;

    @Override
    public List<Vardgivare> getAuthorizedEnheterForHosPerson(String hosPersonHsaId) {
        List<Vardgivare> vardgivareList = new ArrayList<>();

        // Set hos person hsa ID
        GetMiuForPersonType parameters = new GetMiuForPersonType();
        parameters.setHsaIdentity(hosPersonHsaId);
        GetMiuForPersonResponseType response = client.callMiuRights(parameters);
        LOG.debug("User with HSA-Id " + hosPersonHsaId + " has " + response.getMiuInformation().size()
                + " medarbetaruppdrag");

        // filter by syfte. Only 'Vård och behandling' assignments are relevant for WebCert.
        Iterable<MiuInformationType> filteredMius = Iterables.filter(response.getMiuInformation(),
                new Predicate<MiuInformationType>() {
                    @Override
                    public boolean apply(MiuInformationType miuInformationType) {
                        return Medarbetaruppdrag.VARD_OCH_BEHANDLING.equalsIgnoreCase(miuInformationType.getMiuPurpose());
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
        LOG.debug("User with HSA-Id has active 'Vård och behandling' for " + vardgivareIdToMiuInformation.size()
                + " vårdgivare");

        for (String vardgivareId : vardgivareIdToMiuInformation.keySet()) {
            vardgivareList.add(convert(vardgivareIdToMiuInformation.get(vardgivareId)));
        }
        return vardgivareList;
    }

    private List<Vardenhet> fetchAllEnheter(Vardgivare vardgivare, MiuInformationType miuInformationType) {

        List<Vardenhet> vardenheter = new ArrayList<>();

        String enhetHsaId = miuInformationType.getCareUnitHsaIdentity();

        String enhetDn = fetchDistinguishedName(enhetHsaId);
        LOG.debug("DN for enhet " + enhetHsaId + " is " + enhetDn);

        List<CareUnitType> careUnits = fetchSubEnheter(vardgivare, enhetDn);

        for (CareUnitType careUnit : careUnits) {
            Vardenhet vardenhet = new Vardenhet(careUnit.getHsaIdentity(), careUnit.getCareUnitName(),
                    careUnit.getCareUnitStartDate(), careUnit.getCareUnitEndDate());

            // only add enhet if it is currently active
            if (isActive(vardenhet.getStart(), vardenhet.getEnd())) {
                attachMottagningar(vardenhet);
                vardenheter.add(vardenhet);
            } else {
                LOG.debug("Enhet " + enhetHsaId + " is not active right now");
            }
        }

        return vardenheter;
    }

    private boolean isActive(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime now = new LocalDateTime();

        if (fromDate != null && now.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && now.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private List<CareUnitType> fetchSubEnheter(Vardgivare vardgivare, String enhetDn) {
        LookupHsaObjectType parameters = new LookupHsaObjectType();
        parameters.setHsaIdentity(vardgivare.getId());
        parameters.setSearchBase(enhetDn);
        GetCareUnitListResponseType response = client.callGetCareUnitList(parameters);
        LOG.debug("Enhet " + enhetDn + " has " + response.getCareUnits().getCareUnit().size() + " enheter beneath");
        return response.getCareUnits().getCareUnit();
    }

    private String fetchDistinguishedName(String hsaId) {
        HsawsSimpleLookupType lookupType = new HsawsSimpleLookupType();
        ExactType exactType = new ExactType();
        exactType.setSearchAttribute("hsaIdentity");
        exactType.setSearchOperator(SearchOperatorExact.EXACT);
        exactType.setValue(hsaId);

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
            if (isActive(mottagning.getStart(), mottagning.getEnd())) {
                vardenhet.getMottagningar().add(mottagning);
            }
            else {
                LOG.debug("Mottagning " + mottagningsId + " is not active right now");
            }
        }
    }

    private Mottagning fetchMottagning(String mottagningsHsaId) {

        GetHsaUnitResponseType response = client.callGetHsaunit(mottagningsHsaId);
        LOG.debug("Fetching details for mottagning " + mottagningsHsaId);

        // TODO - filter by date

        return new Mottagning(response.getHsaIdentity(), response.getName(), response.getStartDate(), response.getEndDate());
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

        LOG.debug("Enhet " + vardenhet.getId() + " has " + attributes.size() + " mottagningar");

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
            vardgivare.getVardenheter().addAll(fetchAllEnheter(vardgivare, miuInformationType));
        }
        return vardgivare;
    }
}
