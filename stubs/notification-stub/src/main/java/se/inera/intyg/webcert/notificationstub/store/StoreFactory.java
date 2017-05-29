package se.inera.intyg.webcert.notificationstub.store;

import net.openhft.chronicle.map.ChronicleMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by eriklupander on 2017-05-29.
 */
public final class StoreFactory {

    private static final Logger LOG = LoggerFactory.getLogger(StoreFactory.class);

    private static final String WEBCERT_STUB_DATA_FOLDER = "webcert.stub.data.folder";
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private StoreFactory() {
    }

    public static ChronicleMap<String, String> getChronicleMap(String name, int minSize, int averageValueSize, String averageKey) {
        String notificationStubFile = getStubDataFile(name);

        LOG.info("Created disk-persistent ChronicleMap for notificationstub at {} with minsize {}.", notificationStubFile, minSize);

        try {
            ChronicleMap notificationsMap = ChronicleMap
                    .of(String.class, String.class)
                    .name(name)
                    .averageKey(averageKey)
                    .averageValueSize(averageValueSize)
                    .entries(minSize)
                    .createOrRecoverPersistedTo(new File(notificationStubFile));
            LOG.info("Successfully created disk-persistent ChronicleMap for notificationstub at {}", notificationStubFile);
            return notificationsMap;
        } catch (IOException e) {
            LOG.error("Could not create persistent notifications store: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private static String getStubDataFile(String name) {
        if (System.getProperty(WEBCERT_STUB_DATA_FOLDER) != null) {
            return System.getProperty(WEBCERT_STUB_DATA_FOLDER) + File.separator + name + ".data";
        } else if (System.getProperty(JAVA_IO_TMPDIR) != null) {
            return System.getProperty(JAVA_IO_TMPDIR) + File.separator + name + ".data";
        } else {
            throw new IllegalStateException("Error booting stub - cannot determine stub data folder from system properties.");
        }
    }
}
