package se.inera.webcert.modules.registry;

import java.util.List;

public interface IntygModuleRegistry {

    public abstract IntygModule getModule(String name);

    public abstract List<IntygModule> listAllModules();

}
