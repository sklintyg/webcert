package se.inera.certificate.mc2wc;

public enum ApplicationMode {
	IMPORT("import","importJob"),
	EXPORT("export","exportJob");
	
	private String mode;
	
	private String jobName;
	
	private ApplicationMode(String mode, String jobName) {
		this.mode = mode;
		this.jobName = jobName;
	}
	
	public String mode() {
		return mode;
	}
	
	public String jobName() {
		return jobName;
	}
}
