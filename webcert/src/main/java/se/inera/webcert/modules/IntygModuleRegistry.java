package se.inera.webcert.modules;

import java.util.List;

import se.inera.certificate.modules.support.api.ModuleApi;

public interface IntygModuleRegistry {

    public abstract ModuleApi getModuleApi(String id);

    public abstract IntygModule getIntygModule(String id);

    public abstract List<IntygModule> listAllModules();

}
