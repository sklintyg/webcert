package se.inera.webcert.web.service;

import org.springframework.stereotype.Service;
import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;

/**
 * @author johannesc
 */
@Service
public class HsaServiceImpl implements HsaService {

    @Override
    public Vardenheter getVardenheterMedMedarbetaruppdrag(String userHsaId) {
        Vardenheter enheter = new Vardenheter();
        Vardenhet enhet1 = new Vardenhet("HSA_ENHET_1", "Vårdenhet 1");
        enhet1.getMottagningar().add(new Mottagning("HSA_MOTTAGNING_1", "Mottagning 1"));
        enhet1.getMottagningar().add(new Mottagning("HSA_MOTTAGNING_2", "Mottagning 2"));
        Vardenhet enhet2 = new Vardenhet("HSA_ENHET_2", "Vårdenhet 2");
        enhet2.getMottagningar().add(new Mottagning("HSA_MOTTAGNING_3", "Mottagning 3"));
        enhet2.getMottagningar().add(new Mottagning("HSA_MOTTAGNING_4", "Mottagning 4"));
        enhet2.getMottagningar().add(new Mottagning("HSA_MOTTAGNING_5", "Mottagning 5"));

        enheter.getVardenheter().add(enhet1);
        enheter.getVardenheter().add(enhet2);
        return enheter;
    }
}
