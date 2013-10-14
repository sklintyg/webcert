package se.inera.webcert.hsa.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetMiuForPersonType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
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
        Iterable<MiuInformationType> filteredMius = Iterables.filter(response.getMiuInformation(), new Predicate<MiuInformationType>() {
            @Override
            public boolean apply(MiuInformationType miuInformationType) {
                return VARD_OCH_BEHANDLING.equalsIgnoreCase(miuInformationType.getMiuPurpose());
            }
        });

        // group medarbetaruppdrag by vardgivare ID
        ImmutableListMultimap<String, MiuInformationType> vardgivareIdToMiuInformation =
                Multimaps.index(filteredMius, new Function<MiuInformationType, String>() {
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







        /*
        for (MiuInformationType miu : response.getMiuInformation()) {
            // Check that MiuPurpose is "Vård och behandling"
            if (miu.getMiuPurpose().equalsIgnoreCase("Vård och behandling")) {

                // Add data as a new CareUnit and CareGiver

                String vardgivareId = miu.getCareGiver();

                Vardgivare vardgivare = new Vardgivare(vardgivareId, miu.getCareGiverName());

                int index = vardgivareList.indexOf(vardgivare);
                if (index != -1) {
                    vardgivare = vardgivareList.get()

                }
                if (vardgivareList.contains(new Vardgivare(vardgivareId)))

                Vardgivare vardgivare = new Vardgivare(miu.getCareGiver(), miu.getCareGiverName());


                if (vardgivareMap.containsKey(vardgivare)) {
                    vardgivare.
                }

                Vardenhet enhet = new Vardenhet();
                enhet.setId(miu.getCareUnitHsaIdentity());
                enhet.setNamn(miu.getCareUnitName());
                vardgivare.getVardenheter().add(enhet);

                vardgivareMap.put(vardgivareId, vardgivare);
            }
        }
        //return vardgivareMap.values();

         */


    private Vardgivare convert(List<MiuInformationType> miuInformationTypes) {

        Vardgivare vardgivare = new Vardgivare(miuInformationTypes.get(0).getCareGiver(), miuInformationTypes.get(0).getCareGiverName());

        for (MiuInformationType miuInformationType : miuInformationTypes) {
            vardgivare.getVardenheter().add(new Vardenhet(miuInformationType.getCareUnitHsaIdentity(), miuInformationType.getCareUnitName()));
        }
        return vardgivare;
    }
}
