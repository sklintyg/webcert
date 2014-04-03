package se.inera.webcert.modules;

import java.util.List;

import se.inera.certificate.modules.support.api.ModuleApi;

public interface IntygModuleRegistry {

    abstract ModuleApi getModuleApi(String id);

    abstract IntygModule getIntygModule(String id);

    abstract List<IntygModule> listAllModules();

}
