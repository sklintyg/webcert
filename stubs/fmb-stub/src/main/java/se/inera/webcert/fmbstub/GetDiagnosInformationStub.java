package se.inera.webcert.fmbstub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.DiagnosInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.OvrigFmbInformationType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.VersionType;

public class GetDiagnosInformationStub implements GetDiagnosInformationResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GetDiagnosInformationStub.class);

    public GetDiagnosInformationStub() {
        LOG.info("Starting stub: FMB GetDiagnosInformationStub");
    }

    @Override
    public GetDiagnosInformationResponseType getDiagnosInformation(String s, GetDiagnosInformationType getDiagnosInformationType) {
        final GetDiagnosInformationResponseType diagnosInformationResponse = new GetDiagnosInformationResponseType();
        final VersionType version = new VersionType();
        version.setSenateAndring(String.valueOf(System.currentTimeMillis()));
        diagnosInformationResponse.setVersion(version);
        final DiagnosInformationType diagnosInformation = new DiagnosInformationType();
        diagnosInformation.setAktivitetsbegransningBeskrivning("Akut bronkit påverkar inte funktionstillståndet, bortsett från irriterande hosta i normalfallet. Bakteriell sekundärinfektion kan påverka allmäntillståndet genom att patienten blir trött.");
        diagnosInformation.setFunktionsnedsattningBeskrivning("Tillståndet är vanligtvis kortvarigt och varar några dagar till några veckor. Ibland får patienten hosta under flera månader vilket är ett uttryck för en sekundärinfektion.");
        final OvrigFmbInformationType ovrigFmbInformation = new OvrigFmbInformationType();
        ovrigFmbInformation.setGenrellInformation("Återkommande akuta bronkiter hos rökare bör medföra rökstopp. Bihåleinflammationer efter viroser kan ligga bakom återkommande akuta bronkiter. Långvarig bronkit kan bero på twar eller infektion med mykoplasma pneumoni. \n\nHos patienter med samtidig annan luftvägs- eller lungsjukdom som exempelvis astma eller kol kan symtomen vid akut bronkit bli mer uttalade och funktionsnedsättningen bli mer långdragen.");
        ovrigFmbInformation.setSymtomPrognosBehandling("Akut bronkit orsakas vanligen av luftvägsinflammation och epitelskada (skador på hud och slemhinnor i kroppen) efter vanlig virusförkylning. Akut bronkit kan ge hosta under flera månader och är ofta tecken på inflammation i bronkerna. Symtom är akut påkommande torr eller slemmig hosta.");
        diagnosInformation.setOvrigFmbInformation(ovrigFmbInformation);
        diagnosInformation.getHuvuddiagnos().add(GetFmbStub.createHuvuddiagnos("J20"));
        diagnosInformation.getHuvuddiagnos().add(GetFmbStub.createHuvuddiagnos("J22"));
        diagnosInformationResponse.getDiagnosInformation().add(diagnosInformation);
        return diagnosInformationResponse;
    }

}
