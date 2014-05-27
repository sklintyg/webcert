package se.inera.certificate.mc2wc;

import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.mc2wc.batch.MigrationJobExecutor;

public class Main {
			
	private static final String CONTEXT_LOCATION = "/application-context.xml";
	private static final String CONFIG_PARAM = "configFile";
	private static final String LOGGER_PARAM = "logFile";
	private static final String VALID_MODES = "ei";
	private static final String MODE_IMPORT_PARAM = "i";
	private static final String MODE_EXPORT_PARAM = "e";
		
	private static Logger log = LoggerFactory.getLogger(ApplicationConsoleLogger.NAME);
	
	@Autowired
	private MigrationJobExecutor migrationJobExecutor;
	
	public static void main(String[] args) throws Exception {
		
		Properties systemProps = System.getProperties();
		checkThatRequiredProperatiesAreSet(systemProps);
		ApplicationMode appMode = parseAndValidateArguments(args);
		
		Main main = new Main();
		main.configure(appMode);
		main.start(appMode);
	}

	public void configure(ApplicationMode appMode) throws Exception {
		log.info("Configuring...");
						
		ApplicationContextLoader appCtxLoader = new ApplicationContextLoader();
		appCtxLoader.load(this, appMode, CONTEXT_LOCATION);
	}
	
	private static void exitApplication() {
		log.info("Exiting application");
		System.exit(-1);
	}

	private static void checkThatRequiredProperatiesAreSet(Properties props) {
		if (!props.containsKey(LOGGER_PARAM)) {
			log.error("Param '{}' is not set", LOGGER_PARAM); 
			exitApplication();
		}
		
		if (!props.containsKey(CONFIG_PARAM)) {
			log.error("Param '{}' is not set", CONFIG_PARAM); 
			exitApplication();
		}
		
		log.info("Starting application...");
	}
	
	
	private static ApplicationMode parseAndValidateArguments(String[] args) {
		
		OptionParser parser = new OptionParser(VALID_MODES);
		OptionSet options = parser.parse(args);
		
		if(options.has(MODE_EXPORT_PARAM)) {
			return ApplicationMode.EXPORT;
		} else if (options.has(MODE_IMPORT_PARAM)) {
			return ApplicationMode.IMPORT;
		} else {
			log.error("Invalid mode in argument, valid modes are [-{}]...", VALID_MODES);
			exitApplication();
		}
		
		return null;
	}
	
	private void start(ApplicationMode appMode) throws Exception {
		log.info("Starting {} job!", appMode.mode());
		Long jobExecId = migrationJobExecutor.startJob(appMode);
		
		int status = migrationJobExecutor.checkJob(jobExecId);
		
		while (status > 0) {
			Thread.sleep(500);
			status = migrationJobExecutor.checkJob(jobExecId);
		}
		
	}
	
}
