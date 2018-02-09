package se.inera.intyg.webcert.web.service.signatur.nias.xmldsig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringWriter;

@Service
public class UtkastModelToXmlConverterServiceImpl {

    @Autowired
    private IntygModuleRegistry intygModuleRegistry;

    public String utkastToXml(Utkast utkast) {
        String intygsTyp = utkast.getIntygsTyp();
        String json = utkast.getModel();
        try {
            ModuleApi moduleApi = intygModuleRegistry.getModuleApi(intygsTyp);
            Utlatande utlatandeFromJson = moduleApi.getUtlatandeFromJson(json);
            Intyg intyg = moduleApi.getIntygFromUtlatande(utlatandeFromJson);
            StringWriter sw = new StringWriter();
            JAXB.marshal(intyg, sw);
            return sw.toString();
        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        } catch (ModuleException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
