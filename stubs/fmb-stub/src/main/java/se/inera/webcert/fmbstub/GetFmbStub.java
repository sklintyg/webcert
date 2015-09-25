package se.inera.webcert.fmbstub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getdiagnosinformationresponder.v1.GetDiagnosInformationResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbResponderInterface;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbResponseType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.getfmbresponder.v1.GetFmbType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.BeslutsunderlagType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.HuvuddiagnosType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.ICD10SEType;
import se.riv.processmanagement.decisionsupport.insurancemedicinedecisionsupport.v1.VersionType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class GetFmbStub implements GetFmbResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GetFmbStub.class);

    public GetFmbStub() {
        LOG.info("Starting stub: FMB GetFmbStub");
    }

    @Override
    public GetFmbResponseType getFmb(String s, GetFmbType getFmbType) {
        try {
            JAXBContext jbc = JAXBContext.newInstance(GetFmbResponseType.class);
            Unmarshaller u = jbc.createUnmarshaller();
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            Resource resource = r.getResource("GetFmbResponse.xml");
            GetFmbResponseType value = u.unmarshal(new StreamSource(resource.getInputStream()), GetFmbResponseType.class).getValue();
            addHardcodedInfo(value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        final GetFmbResponseType fmbResponse = new GetFmbResponseType();
        addHardcodedInfo(fmbResponse);
        return fmbResponse;
    }

    private void addHardcodedInfo(GetFmbResponseType fmbResponse) {
        final VersionType version = new VersionType();
        version.setSenateAndring(String.valueOf(System.currentTimeMillis()));
        fmbResponse.setVersion(version);
        final BeslutsunderlagType beslutsunderlag = new BeslutsunderlagType();
        beslutsunderlag.setTextuelltUnderlag("Akut bronkit nedsätter normalt inte arbetsförmågan. Om patienten har långvarig svår hosta kan det möjligen påverka allmäntillståndet genom att patienten blir trött. Sjukskrivning enbart i undantagsfall vid tydligt nedsatt allmäntillstånd i upp till 2 veckor. Röstkrävande yrken kan behöva längre sjukskrivning.");
        beslutsunderlag.getHuvuddiagnos().add(createHuvuddiagnos("J20"));
        beslutsunderlag.getHuvuddiagnos().add(createHuvuddiagnos("J22"));
        fmbResponse.getBeslutsunderlag().add(beslutsunderlag);
    }

    public static HuvuddiagnosType createHuvuddiagnos(String code) {
        final HuvuddiagnosType huvuddiagnos = new HuvuddiagnosType();
        final ICD10SEType kod = new ICD10SEType();
        kod.setCode(code);
        huvuddiagnos.setKod(kod);
        return huvuddiagnos;
    }

}
