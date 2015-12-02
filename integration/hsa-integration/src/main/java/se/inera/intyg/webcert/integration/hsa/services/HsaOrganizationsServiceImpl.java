package se.inera.webcert.hsa.services;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.util.logging.LogMarkers;
import se.inera.ifv.hsawsresponder.v3.AddressType;
import se.inera.ifv.hsawsresponder.v3.AttributeListType;
import se.inera.ifv.hsawsresponder.v3.AttributeValueListType;
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
import se.inera.webcert.hsa.model.AbstractVardenhet;
import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.stub.Medarbetaruppdrag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
@Service
public class HsaOrganizationsServiceImpl implements HsaOrganizationsService {

    private static final String HSA_SEARCH_BASE = "c=SE";
    private static final String HSA_ATR_HSA_HEALTH_CARE_UNIT_MEMBER = "hsaHealthCareUnitMember";
    private static final String HSA_ATR_UNIT_PRESCRIPTION_CODE = "unitPrescriptionCode";
    private static final String HSA_ATR_HSA_IDENTITY = "hsaIdentity";

    private static final String DEFAULT_ARBETSPLATSKOD = "0000000";
    private static final String DEFAULT_POSTNR = "XXXXX";

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

        List<MiuInformationType> miuInformation = response.getMiuInformation();
        LOG.debug("User '{}' has a total of {} medarbetaruppdrag", hosPersonHsaId, miuInformation.size());

        // filter by syfte. Only 'Vård och behandling' assignments are relevant for WebCert.
        Iterable<MiuInformationType> filteredMius = Iterables.filter(miuInformation,
                new Predicate<MiuInformationType>() {
                    @Override
                    public boolean apply(MiuInformationType miuInformationType) {
                        return Medarbetaruppdrag.VARD_OCH_BEHANDLING.equalsIgnoreCase(miuInformationType
                                .getMiuPurpose());
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

        LOG.debug("User '{}' has {} VoB medarbetaruppdrag for {} vårdgivare", hosPersonHsaId,
                vardgivareIdToMiuInformation.size(), vardgivareIdToMiuInformation.keySet().size());

        for (String vardgivareId : vardgivareIdToMiuInformation.keySet()) {
            Vardgivare vardgivare = createVardgivareFromMIU(vardgivareIdToMiuInformation.get(vardgivareId));
            if (vardgivare != null) {
                vardgivareList.add(vardgivare);
            }
        }

        Collections.sort(vardgivareList);

        return vardgivareList;
    }

    private List<Vardenhet> fetchAllEnheter(Vardgivare vardgivare, MiuInformationType miuInformationType) {

        List<Vardenhet> vardenheter = new ArrayList<>();

        String enhetHsaId = miuInformationType.getCareUnitHsaIdentity();
        String enhetNamn = miuInformationType.getCareUnitName();

        String enhetDn = fetchDistinguishedName(enhetHsaId, enhetNamn);

        if (enhetDn == null) {
            return vardenheter;
        }

        LOG.debug("DN for unit '{}' is '{}'", enhetHsaId, enhetDn);

        List<CareUnitType> careUnits = fetchSubEnheter(vardgivare, enhetDn);

        for (CareUnitType careUnit : careUnits) {
            Vardenhet vardenhet = new Vardenhet(careUnit.getHsaIdentity(), careUnit.getCareUnitName(),
                    careUnit.getCareUnitStartDate(), careUnit.getCareUnitEndDate());

            // only add enhet if it is currently active
            if (!isActive(vardenhet.getStart(), vardenhet.getEnd())) {
                LOG.debug("Vårdenhet '{}' is not active right now", vardenhet.getId());
                continue;
            }

            GetHsaUnitResponseType response = client.callGetHsaunit(vardenhet.getId());
            updateWithContactInformation(vardenhet, response);

            vardenhet.setArbetsplatskod(getWorkplaceCode(vardenhet.getId()));

            attachMottagningar(vardenhet);

            vardenheter.add(vardenhet);
        }

        return vardenheter;
    }

    private boolean isActive(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime now = new LocalDateTime();

        if (fromDate != null && now.isBefore(fromDate)) {
            return false;
        }
        return !(toDate != null && now.isAfter(toDate));
    }

    private List<CareUnitType> fetchSubEnheter(Vardgivare vardgivare, String enhetDn) {
        LookupHsaObjectType parameters = new LookupHsaObjectType();
        parameters.setHsaIdentity(vardgivare.getId());
        parameters.setSearchBase(enhetDn);
        GetCareUnitListResponseType response = client.callGetCareUnitList(parameters);
        LOG.debug("Enhet '{}' has {} enheter beneath", enhetDn, response.getCareUnits().getCareUnit().size());
        return response.getCareUnits().getCareUnit();
    }

    private String fetchDistinguishedName(String hsaId, String namn) {
        HsawsSimpleLookupType lookupType = new HsawsSimpleLookupType();
        ExactType exactType = new ExactType();
        exactType.setSearchAttribute(HSA_ATR_HSA_IDENTITY);
        exactType.setSearchOperator(SearchOperatorExact.EXACT);
        exactType.setValue(hsaId);

        AttributeListType attributeList = new AttributeListType();
        attributeList.getAttribute().add(HSA_ATR_HSA_IDENTITY);
        lookupType.setAttributes(attributeList);
        lookupType.setLookup(exactType);

        HsawsSimpleLookupResponseType lookupResponse = client.callHsawsSimpleLookup(lookupType);
        switch (lookupResponse.getResponseValues().size()) {
        case 0:
            LOG.error(LogMarkers.HSA, "Unit '{}' with hsaId '{}' is missing", namn, hsaId);
            return null;
        case 1:
            return lookupResponse.getResponseValues().get(0).getDN();
        default:
            LOG.warn(LogMarkers.HSA, "The hsaId '{}' is used for more than one unit. This is probably due to a misconfiguration in HSA", hsaId);
            return lookupResponse.getResponseValues().get(0).getDN();
        }
    }

    private void attachMottagningar(Vardenhet vardenhet) {
        List<String> mottagningsIds = fetchMottagningsHsaId(vardenhet.getId());
        for (String mottagningsId : mottagningsIds) {
            Mottagning mottagning = fetchMottagning(mottagningsId);

            if (mottagning == null) {
                LOG.error(LogMarkers.HSA, "Could not attach mottagning '{}' to vardenhet '{}'", mottagningsId, vardenhet.getId());
                continue;
            }

            // only add mottagning if it is active
            if (!isActive(mottagning.getStart(), mottagning.getEnd())) {
                LOG.debug("Mottagning '{}' is not active right now", mottagning.getId());
                continue;
            }

            mottagning.setArbetsplatskod(getWorkplaceCode(mottagning.getId()));

            vardenhet.getMottagningar().add(mottagning);
            LOG.debug("Attached mottagning '{}' to vardenhet '{}'", mottagning.getId(), vardenhet.getId());
        }

        Collections.sort(vardenhet.getMottagningar());
    }

    private String getWorkplaceCode(String careUnitHsaId) {
        LOG.debug("Fetching arbetsplatskod from HSA for enhet '{}'", careUnitHsaId);

        HsawsSimpleLookupType parameters = new HsawsSimpleLookupType();

        ExactType lookupValue = new ExactType();
        lookupValue.setSearchAttribute(HSA_ATR_HSA_IDENTITY);
        lookupValue.setSearchOperator(SearchOperatorExact.EXACT);
        lookupValue.setValue(careUnitHsaId);
        parameters.setLookup(lookupValue);

        AttributeListType attributes = new AttributeListType();
        attributes.getAttribute().add(HSA_ATR_UNIT_PRESCRIPTION_CODE);

        parameters.setAttributes(attributes);

        HsawsSimpleLookupResponseType response = client.callHsawsSimpleLookup(parameters);

        // Read response and pick values
        for (AttributeValueListType attValList : response.getResponseValues()) {
            for (AttributeValuePairType attValue : attValList.getResponse()) {
                if (attValue.getAttribute().equalsIgnoreCase(HSA_ATR_UNIT_PRESCRIPTION_CODE)) {
                    if (!attValue.getValue().isEmpty()) {
                        String value = attValue.getValue().get(0);
                        if (value != null && !value.isEmpty()) {
                            return value;
                        } else {
                            return DEFAULT_ARBETSPLATSKOD;
                        }
                    }
                }
            }
        }

        return DEFAULT_ARBETSPLATSKOD;
    }

    private Mottagning fetchMottagning(String mottagningsHsaId) {

        GetHsaUnitResponseType response = client.callGetHsaunit(mottagningsHsaId);

        LOG.debug("Fetching data for mottagning '{}'", mottagningsHsaId);

        if (response == null) {
            LOG.error(LogMarkers.HSA, "Mottagning '{}' was not found in HSA. Inconsistent data in HSA", mottagningsHsaId);
            return null;
        }

        Mottagning mottagning = new Mottagning(response.getHsaIdentity(), response.getName(), response.getStartDate(), response.getEndDate());
        updateWithContactInformation(mottagning, response);

        return mottagning;
    }

    private void updateWithContactInformation(AbstractVardenhet vardenhet, GetHsaUnitResponseType response) {
        vardenhet.setEpost(response.getEmail());
        if (response.getTelephoneNumbers() != null && !response.getTelephoneNumbers().getTelephoneNumber().isEmpty()) {
            vardenhet.setTelefonnummer(response.getTelephoneNumbers().getTelephoneNumber().get(0));
        }
        AddressType address = response.getPostalAddress();
        if (address == null) {
            return;
        }
        StringBuilder postaAddress = new StringBuilder();
        List<String> lines = address.getAddressLine();
        for (int i = 0; i < lines.size() - 1; i++) {
            postaAddress.append(lines.get(i).trim());
        }
        vardenhet.setPostadress(postaAddress.toString());
        String lastLine = lines.get(lines.size() - 1);
        final int shortestLengthToIncludeBothPnrAndPostort = 7;
        if (lastLine.length() > shortestLengthToIncludeBothPnrAndPostort && Character.isDigit(lastLine.charAt(0))) {
            final int startPostort = 6;
            vardenhet.setPostnummer(lastLine.substring(0, startPostort).trim());
            vardenhet.setPostort(lastLine.substring(startPostort).trim());
        } else {
            vardenhet.setPostnummer(DEFAULT_POSTNR);
            vardenhet.setPostort(lastLine.trim());
        }
    }

    private List<String> fetchMottagningsHsaId(String vardenhetsId) {
        List<String> mottagningIds = new ArrayList<>();

        HsawsSimpleLookupType lookupType = new HsawsSimpleLookupType();

        ExactType exactType = new ExactType();
        exactType.setSearchAttribute(HSA_ATR_HSA_IDENTITY);
        exactType.setSearchOperator(SearchOperatorExact.EXACT);
        exactType.setValue(vardenhetsId);

        AttributeListType attributeList = new AttributeListType();
        attributeList.getAttribute().add(HSA_ATR_HSA_HEALTH_CARE_UNIT_MEMBER);
        lookupType.setAttributes(attributeList);
        lookupType.setSearchBase(HSA_SEARCH_BASE);
        lookupType.setLookup(exactType);

        HsawsSimpleLookupResponseType lookupResponse = client.callHsawsSimpleLookup(lookupType);

        if (lookupResponse.getResponseValues().isEmpty()) {
            LOG.debug("Vardenhet '{}' has 0 mottagningar", vardenhetsId);
            return mottagningIds;
        }

        List<AttributeValuePairType> attributes = lookupResponse.getResponseValues().get(0).getResponse();

        for (AttributeValuePairType attribute : attributes) {
            if (HSA_ATR_HSA_HEALTH_CARE_UNIT_MEMBER.equals(attribute.getAttribute())) {
                mottagningIds.addAll(attribute.getValue());
            }
        }

        LOG.debug("Vardenhet '{}' has {} mottagningar", vardenhetsId, mottagningIds.size());

        return mottagningIds;
    }

    private Vardgivare createVardgivareFromMIU(List<MiuInformationType> miuInformationTypes) {

        Vardgivare vardgivare = new Vardgivare(miuInformationTypes.get(0).getCareGiver(), miuInformationTypes.get(0).getCareGiverName());

        for (MiuInformationType miuInformationType : miuInformationTypes) {
            vardgivare.getVardenheter().addAll(fetchAllEnheter(vardgivare, miuInformationType));
        }

        Collections.sort(vardgivare.getVardenheter());

        if (vardgivare.getVardenheter().isEmpty()) {
            return null;
        }

        LOG.debug("Created Vardgivare {}", vardgivare);

        return vardgivare;
    }

    @Override
    public Vardenhet getVardenhet(String careUnitHsaId) {

        LOG.debug("Getting info on vardenhet '{}'", careUnitHsaId);

        // get the basic details
        GetHsaUnitResponseType response = client.callGetHsaunit(careUnitHsaId);

        Vardenhet vardenhet = new Vardenhet(careUnitHsaId, response.getName(),
                response.getStartDate(), response.getEndDate());

        // update with unit contact details
        updateWithContactInformation(vardenhet, response);

        // update with the workplace code for this unit
        String unitWorkplaceCode = getWorkplaceCode(vardenhet.getId());
        vardenhet.setArbetsplatskod(unitWorkplaceCode);

        return vardenhet;
    }
}
