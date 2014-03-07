package se.inera.webcert.web.controller.api;

import org.springframework.stereotype.Component;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;

/**
 * TODO Ta bort denna klass när WC beror på minst en riktig modul
 */
@Component
public class ModuleEntryPointDummy implements ModuleEntryPoint {

    @Override
    public String getModuleName() {
        return "module1";
    }

    @Override
    public ModuleApi getModuleApi() {
        return null;
    }

    @Override
    public String getModuleScriptPath() {
        return "/js/module1";
    }
}
