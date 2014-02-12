package se.inera.webcert.modules.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class IntygModulesRegistryImpl implements IntygModuleRegistry {

    private Map<String, IntygModule> modulesMap = new HashMap<String, IntygModule>();
    
    public IntygModulesRegistryImpl() {
        
    }

    @PostConstruct
    public void mockRegistry() {
        
        IntygModule im = new IntygModule("fk7263", "Läkarintyg FK 7263", "fk7263", 1);
        modulesMap.put(im.getId(), im);
        
        im = new IntygModule("ts-bas", "Transportstyrelsens läkarintyg, bas", "ts-bas", 2);
        modulesMap.put(im.getId(), im);
        
        im = new IntygModule("ts-diabetes", "Transportstyrelsens läkarintyg, diabetes", "ts-diabetes", 3);
        modulesMap.put(im.getId(), im);
        
        im = new IntygModule("ivar", "Intyg vid avbeställd resa", "rli", 4);
        modulesMap.put(im.getId(), im);
        
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.module.IntygModuleRegistry#getModule(java.lang.String)
     */
    @Override
    public IntygModule getModule(String name) {
        return modulesMap.get(name);
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.module.IntygModuleRegistry#listAllModules()
     */
    @Override
    public List<IntygModule> listAllModules() {
        List<IntygModule> moduleList = new ArrayList<IntygModule>();
        moduleList.addAll(modulesMap.values());
        Collections.sort(moduleList);
        return moduleList;
    }
    
}
