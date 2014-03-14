package se.inera.webcert.modules.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;

@Component
public class IntygModuleRegistryImpl implements IntygModuleRegistry {

    private static Logger LOG = LoggerFactory.getLogger(IntygModuleRegistryImpl.class);

    @Autowired
    private List<ModuleEntryPoint> moduleEntryPoints;

    private Map<String, ModuleApi> moduleApiMap = new HashMap<String, ModuleApi>();

    private List<IntygModule> moduleList = new ArrayList<IntygModule>();

    @PostConstruct
    public void init() {
        initModuleApiMap(moduleEntryPoints);
        initModulesList(moduleEntryPoints);
        LOG.info("Module registry loaded with {} modules", moduleEntryPoints.size());
    }
        
    private void initModuleApiMap(List<ModuleEntryPoint> moduleEntryPoints) {
        for (ModuleEntryPoint entryPoint : moduleEntryPoints) {
            moduleApiMap.put(entryPoint.getModuleName(), entryPoint.getModuleApi());
        }
    }

    private void initModulesList(List<ModuleEntryPoint> moduleEntryPoints) {

        IntygModule module;

        for (ModuleEntryPoint entryPoint : moduleEntryPoints) {
            module = new IntygModule(entryPoint.getModuleName(), entryPoint.getModuleName());
            moduleList.add(module);
        }

        Collections.sort(moduleList);
    }

    @Override
    public List<IntygModule> listAllModules() {
        return moduleList;
    }

    @Override
    public ModuleApi getModule(String name) {
        return moduleApiMap.get(name);
    }

}
