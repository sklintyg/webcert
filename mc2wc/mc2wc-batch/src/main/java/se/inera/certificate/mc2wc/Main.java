package se.inera.certificate.mc2wc;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.JOptCommandLinePropertySource;
import org.springframework.core.env.PropertySource;

import se.inera.certificate.mc2wc.batch.MigrationJobExecutor;

public class Main {
	
	public static final String CONSOLE_LOGGER = "mc2wc.console";
		
	private static final String CONTEXT_LOCATION = "/application-context.xml";
	private static final String CONFIG_PARAM = "configFile";
	private static final String LOGGER_PARAM = "logFile";
	
	private static Logger log = LoggerFactory.getLogger(CONSOLE_LOGGER);
	
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
		OptionParser parser = new OptionParser();
		parser.accepts(CONFIG_PARAM).withRequiredArg();
		OptionSet options = parser.parse(args);
		PropertySource ps = new JOptCommandLinePropertySource(options);
		
		ApplicationContextLoader appCxtLoader = new ApplicationContextLoader();
		appCxtLoader.load(this, ps, CONTEXT_LOCATION);
	}
	
	private void start() throws Exception {
		log.info("Starting migration!");
		int status = migrationJobExecutor.startMigration();
		
		while (status > 0) {
			status = migrationJobExecutor.checkMigrationJob();
			Thread.sleep(500);
		}
		
	}

}
