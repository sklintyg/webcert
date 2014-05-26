package se.inera.certificate.mc2wc;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.JOptCommandLinePropertySource;
import org.springframework.core.env.PropertySource;

import se.inera.certificate.mc2wc.batch.MigrationJobExecutor;

import java.io.File;

public class Main {
			
	private static final String CONTEXT_LOCATION = "/application-context.xml";
	private static final String CONFIG_PARAM = "configFile";
	private static final String LOGGER_PARAM = "logFile";
	private static final String LOG_FILE_DIR_PROP = "LOG_FILE_DIR";
	
	private static Logger log = LoggerFactory.getLogger(ApplicationConsoleLogger.NAME);
	
	@Autowired
	private MigrationJobExecutor migrationJobExecutor;
	
	public static void main(String[] args) throws Exception {
		log.info("Starting application...");
		Main main = new Main();
		main.configure(args);
		main.start();
	}

	public void configure(String[] args) throws Exception {
		log.info("Configuring...");
		
		OptionSet options = parseArguments(args);
		
		if (options == null) {
			System.exit(-1);
		}
        String logFolder = new File((String) options.valueOf(LOGGER_PARAM)).getCanonicalPath();
        String configFile = new File((String) options.valueOf(CONFIG_PARAM)).getCanonicalPath();
        log.info("Using log folder: " + logFolder);
        log.info("Using config file: {}", configFile);
		System.getProperties().setProperty(LOG_FILE_DIR_PROP, configFile);
		
		PropertySource<OptionSet> ps = new JOptCommandLinePropertySource(options);
		
		ApplicationContextLoader appCxtLoader = new ApplicationContextLoader();
		appCxtLoader.load(this, ps, CONTEXT_LOCATION);
	}
	
	private OptionSet parseArguments(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts(CONFIG_PARAM).withRequiredArg().ofType(String.class);
		parser.accepts(LOGGER_PARAM).withRequiredArg().ofType(String.class);
				
		OptionSet options = parser.parse(args);
		
		if (!options.has(CONFIG_PARAM) || !options.has(LOGGER_PARAM)) {
			log.error("Can not start, missing required arguments...");
			return null;
		}
		
		return options;
	}
	
	private void start() throws Exception {
		log.info("Starting migration!");
		Long jobExecId = migrationJobExecutor.startMigration();
		
		int status = migrationJobExecutor.checkMigrationJob(jobExecId);
		
		while (status > 0) {
			Thread.sleep(500);
			status = migrationJobExecutor.checkMigrationJob(jobExecId);
		}
		
	}

}
