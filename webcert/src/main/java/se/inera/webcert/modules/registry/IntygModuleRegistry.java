package se.inera.webcert.modules.registry;

import java.util.List;

import se.inera.certificate.modules.support.api.ModuleApi;

public interface IntygModuleRegistry {

    public abstract ModuleApi getModule(String name);

    public abstract List<IntygModule> listAllModules();

}
