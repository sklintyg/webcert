package se.inera.webcert.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static se.inera.certificate.modules.support.ApplicationOrigin.WEBCERT;

@Component
public class IntygModuleRegistryImpl implements IntygModuleRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleRegistryImpl.class);

    @Autowired
    private List<ModuleEntryPoint> moduleEntryPoints;

    private Map<String, ModuleApi> moduleApiMap = new HashMap<String, ModuleApi>();
    
    private Map<String, IntygModule> intygModuleMap = new HashMap<String, IntygModule>();

    @PostConstruct
    private void initModulesList() {

        for (ModuleEntryPoint entryPoint : moduleEntryPoints) {
            moduleApiMap.put(entryPoint.getModuleId(), entryPoint.getModuleApi());
            IntygModule module = new IntygModule(entryPoint.getModuleId(), entryPoint.getModuleName(),
                    entryPoint.getModuleDescription(),
                    entryPoint.getModuleCssPath(WEBCERT), entryPoint.getModuleScriptPath(WEBCERT),
                    entryPoint.getModuleDependencyDefinitionPath(WEBCERT));
            intygModuleMap.put(module.getId(), module);
        }

        LOG.info("Module registry loaded with {} modules", moduleApiMap.size());
    }

    @Override
    public List<IntygModule> listAllModules() {
        List<IntygModule> moduleList = new ArrayList<IntygModule>(intygModuleMap.values());
        Collections.sort(moduleList);
        return moduleList;
    }

    @Override
    public ModuleApi getModuleApi(String id) {
        return moduleApiMap.get(id);
    }

    @Override
    public IntygModule getIntygModule(String id) {
        return intygModuleMap.get(id);
    }
    
    public boolean moduleExists(String moduleId) {
        return intygModuleMap.containsKey(moduleId);
    }
    
    public List<ModuleEntryPoint> getModuleEntryPoints() {
        return moduleEntryPoints;
    }

}
