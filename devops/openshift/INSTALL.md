
# OPENSHIFT INSTALLATION GUIDE -- WC 2019-2

Installation of Web application WebCert (WC) on OpenShift.

## 1 Updates since 2019-1 (release notes)

### 1.1 Database schema

Database schema doesn't need any updates.

### 1.2 New configuration and secret properties

**Note:** The new token OpenID Connect (OIDC) exchange feature is not yet activated but the settings have to be defined.

The following configuration properties have been added:

* `REFDATA_URL` -- Location of reference data, see below
* `IDP_OIDC_JWKS_URL` -- URL for OpenID Connect endpoint (IODC Token exchange)
* `IDP_OIDC_JWKS_SKEW` -- Allowed clock skew in seconds when validating age of token

The following configuration properties can be removed:

* `CERTIFICATE_SENDER_QUEUEMAME` -- typo has been corrected (since 2019-1)

### 1.3 Configuration of reference data

The main update is activation of the new reference data concept (master data for shared configurations). Refdata is provided as a JAR file and configured with the `REFDATA_URL` and `RESOURCES_FOLDER` parameters. Normally the default value of `RESOURCES_FOLDER` should be set to  `classpath:`. Three configuration updates is required in order to activate the new refdata:

1. Parameter `REFDATA_URL` shall be set to the actual location of the refdata JAR artefact.
2. Parameter `RESOURCES_FOLDER` or `-Dresources.folder=...` in `secret-env.sh` shall be set to `classpath:`. Though, it's recommended to remove this parameter from `secret-env.sh`. 
3. The old `resources.zip` must be removed in order to enable the `REFDATA_URL` setting. 

Latest builds of refdata can be downloaded from the Inera Nexus server. 

	https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/refdata/refdata/1.0.0.<build-num>/refdata-1.0.0.<build-num>.jar

### 1.4 Other recommendations

The following (well known) observation might require an action:

* Mutual TLS is not respected when terminating RIV-TA requests


## 2 Pre-Installation Requirements

The following prerequisites and requirements must be satisfied in order for the webcert to install successfully.

### 2.1 Backing Service Dependencies

The application has the following external services: 

On premise (execution environment):

* MySQL
* ActiveMQ
* Redis Sentinel
* Redis Server
* Inera Certificate Service (Intygstjänst, IT)
* Inera Private Practitioner Portal (PP)

Provided elsewhere:

* Inera Service Platform (NTjP)
* Inera Service Catalog (TAK)
* Inera SAML IdP
* CGI SAML IdP
* FMB API Service (SoS)
* NetID Access Service

For all backing services their actual addresses and user accounts have to be known prior to start the installation procedure.  

### 2.2 Integration / Firewall

WebCert communicates in/out with the Inera Service Platform and thus needs firewall rules for that access.

### 2.3 Certificates

WebCert needs certificates, keystores and truststores for Inera Service Platform, SAML IdP and CGI IdP. The operations provider is responsible for installing these certificates in the appropriate OpenShift "secret", see detailed instructions in the OpenShift section.

### 2.4 Message Queues

The queues listed below are required and depending on permissions those might be implicitly created by the application.

- `webcert.log.queue` -- sends PDL log records to Logsender
- `webcert.certificate.queue` -- sends Certificates to IT
- `webcert.notification.ws.queue` -- sends Notifications to remote EHR systems
- `webcert.notification.queue` -- sends and receives notifications
- `webcert.aggregated.notification.queue` -- sends and receives aggregation/batches of notifications
- `internal.notification.queue` -- receives Notifications from IT

### 2.5 Database

A database for the application must have been created.  It's recommended to use character set `utf8mb4` and case-sensitive collation. 

### 2.6 Access to Software Artifacts

Software artifacts are located at, and downloaded from:

* From Installing Client - [https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/webcert/webcert/maven-metadata.xml](https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/webcert/webcert/maven-metadata.xml)
* From OpenShift Cluster - docker.drift.inera.se/intyg/

### 2.7 Access to OpenShift Cluster

The OpenShift user account must haThe OpenShift user account must have the right permissions to process, create, delete and replace objects. A VPN account and connection is required in order to access the OpenShift Cluster.

### 2.8 Client Software Tools

The installation client must have **git** and **oc** (OpenShift Client) installed and if a database schema migration is required then **java** (Java 8) and **tar** is required in order to execute the migration tool (liquibase runner).

Must have:

* git
* oc
* VPN Client (such as Cisco Any Connect) 

To run database migration tool:

* java
* tar

# 3 Installation Procedure

### 3.1 Installation Checklist

1. All Pre-Installation Requirements are fulfilled, se above
2. Check if a database migration is required
3. Ensure that the secrets `webcert-env`, `webcert-certifikat` and `webcert-secret-envvar` are up to date
4. Ensure that the config maps `webcert-config` and `webcert-configmap-envvar` are up to date
5. Check that deployment works as expected 
6. Fine-tune memory settings for container and java process
7. Setup policies for number of replicas, auto-scaling and rolling upgrade strategy


### 3.2 Migrate Database Schema

Prior to any release that includes changes to the database schema, the operations provider must execute schema updates using the Liquibase runner tool provided in this section. 

_Please note: a complete database backup is recommended prior to run the database migration tool_

Replace `<version>` below with the actual application version.

Fetch the actual version of the tool, the example below runs `wget` to retrieve the package (tarball).

    > wget https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/webcert/webcert-liquibase-runner/<version>/webcert-liquibase-runner-<version>.tar


Download the tool to a computer with Java installed and access to the database in question.

    > tar xvf webcert-liquibase-runner-<version>.tar
    > cd webcert-liquibase-runner-<version>
    > bin/webcert-liquibase-runner --url=jdbc:mysql://<database-host>/<database-name> --username=<database_username> --password=<database_password> update


### 3.3 Get Source for Configuration


##### 3.3.1 Clone the repository

Clone repository and switch to the release branch specified in the release notes.
    
    > git clone https://github.com/sklintyg/webcert.git
    > git checkout release/2019-2
    > cd devops/openshift
    
Note that we strongly recommend using a git account that has read-only (e.g. public) access to the repo.
    
##### 3.3.2 Log-in into the cluster

Use **oc** to login and select the actual project, e.g:

    > oc login https://path.to.cluster
    username: ******
    password: ******
    > oc project <name>

#### 3.3.3 Ensure an up to date deployment configuration

A template for the deployment can be dowloaded from [deploytemplate-webapp.yaml](https://github.com/sklintyg/tools/blob/develop/devops/openshift/deploytemplate-webapp.yaml). This needs to be updated regarding assigned computing resources, i.e. the requested and limited amount of CPU needs to be increased as well as the Java memory heap settings, see `JAVA_OPTS`.

Syntax to create or replace the template: 

	> oc [ create | replace ] -f deploytemplate-webapp.yaml

### 3.4 Update configuration placeholders

For security reasons, no secret properties or configuration may be checked into git. Thus, a number of placeholders needs to be replaced prior to creating or updating secrets and/or config maps.

Open _&lt;env>/secret-vars.yaml_ and and assign correct values:

	ACTIVEMQ_BROKER_USERNAME: "<username>"
	ACTIVEMQ_BROKER_PASSWORD: "<password>"
	REDIS_PASSWORD: "<password>"
	DATABASE_USERNAME: "<username>"
	DATABASE_PASSWORD: "<password>"
	MAIL_PASSWORD: "<password>"
	CGI_GRP_WS_CERTIFICATE_PASSWORD: "<password>"
	CGI_GRP_WS_KEY_MANAGER_PASSWORD: "<password>"
	CGI_GRP_WS_TRUSTSTORE_PASSWORD: "<password>"
	HSA_WS_CERTIFICATE_PASSWORD: "<password>"
	HSA_WS_KEY_MANAGER_PASSWORD: "<password>"
	HSA_WS_TRUSTSTORE_PASSWORD: "<password>"
	NTJP_WS_CERTIFICATE_PASSWORD: "<password>"
	NTJP_WS_KEY_MANAGER_PASSWORD: "<password>"
	NTJP_WS_TRUSTSTORE_PASSWORD: "<password>"
	SAKERHETSTJANST_SAML_KEYSTORE_PASSWORD: "<password>"
	SAKERHETSTJANST_SAML_TRUSTSTORE_PASSWORD: "<password>"
	SAKERHETSTJANST_WS_CERTIFICATE_PASSWORD: "<password>"
	SAKERHETSTJANST_WS_KEY_MANAGER_PASSWORD: "<password>"
	SAKERHETSTJANST_WS_TRUSTSTORE_PASSWORD: "<password>"
  
Open _&lt;env>/configmap-vars.yaml_ and replace example `<value>` with expected values. You may also update the names of keystore/truststore files as well as their type (JKS or PKCS12). Also see working example from [webcert-test-configmap-envvar](https://raw.githubusercontent.com/sklintyg/webcert/develop/devops/openshift/test/configmap-vars.yaml). 

	JOB_UTKASTLOCK_CRON: "0 0 2 * * ?"
	JOB_UTKASTLOCK_LOCKED_AFTER_DAY: "14"
	SPRING_PROFILES_ACTIVE: "prod,caching-enabled,redis-sentinel"
	CONFIG_DIR: "${config.folder}"
	LOGBACK_FILE: "/opt/$APP_NAME/config/logback-ocp.xml"
	REDIS_HOST: "<hostname1[;hostname2;...]>"
	REDIS_PORT: "<port1[;port2;...]>"
	REDIS_SENTINEL_MASTER_NAME: "<name>"
	MAIL_HOST: "mailout.sth.basefarm.net"
	MAIL_FROM: "no-reply@webcert.intygstjanster.se"
	MAIL_PROTOCOL: [ "smtp" | "smtps" ]
	MAIL_USERNAME: "<value>"
	DATABASE_PORT: "3306"
	DATABASE_SERVER: "<value>"
	ACTIVEMQ_BROKER_URL: "<value>"
	NOTIFICATION_QUEUENAME: "webcert.notification.queue"
	NOTIFICATION_WS_QUEUENAME: "webcert.ws.notification.queue"
	NOTIFICATION_AGGREGATION_QUEUENAME: "webcert.aggregated.notification.queue"
	LOG_QUEUENAME: "webcert.log.queue"
	CERTIFICATE_SENDER_QUEUENAME: "webcert.certificate.queue"
	NTJP_WS_CERTIFICATE_FILE: "<file>"
	NTJP_WS_TRUSTSTORE_FILE: "<file>"
	NTJP_WS_CERTIFICATE_TYPE: [ "JKS" | "PKCS12" ]
	NTJP_WS_TRUSTSTORE_TYPE: [ "JKS" | "PKCS12" ]
	CGI_GRP_WS_CERTIFICATE_FILE: "<file>"
	CGI_GRP_WS_CERTIFICATE_TYPE: [ "JKS" | "PKCS12" ]
	CGI_GRP_WS_TRUSTSTORE_FILE: "<file>"
	CGI_GRP_WS_TRUSTSTORE_TYPE: [ "JKS" | "PKCS12" ]
	SAKERHETSTJANST_SAML_IDP_METADATA_URL: "<url>"
	SAKERHETSTJANST_SAML_KEYSTORE_FILE: "<file>"
	SAKERHETSTJANST_SAML_KEYSTORE_ALIAS: [ "JKS" | "PKCS12" ]
	WEBCERT_HOST_URL: "<url>"
	INTYGSTJANST_BASE_URL: "http://intygstjanst:8080/inera-certificate"
	FMB_ENDPOINT_URL: "https://api.socialstyrelsen.se/fmb"
	NETID_ACCESS_SERVER_URL: "https://showroom.lab.secmaker.com/nias/ServiceServer.asmx"
	NTJP_BASE_URL: "https://esb.ntjp.se/vp"
	SRS_BASE_URL: "deprecated"
	PRIVATEPRACTITIONER_BASE_URL: "<url>"
	PRIVATEPRACTITIONER_PORTAL_REGISTRATION_URL: "<url>"
	CGI_FUNKTIONSTJANSTER_SAML_IDP_METADATA_URL: "<url>"
	CGI_FUNKTIONSTJANSTER_GRP_URL: "<url>"
	CGI_GRP_SERVICEID: "<id>"
	CGI_GRP_DISPLAYNAME: "Inera Webcert"
	TAK_BASE_URL: "http://api.ntjp.se/coop/api/v1"
	TAK_ENVIRONMENT: [ "QA" | "PROD" ]
	INFRASTRUCTURE_DIRECTORY_LOGICALADDRESS: "SE165565594230-1000"
	INTYGSTJANST_LOGICALADDRESS: "5565594230"
	PRIVATEPRACTITIONER_LOGICALADDRESS: ""
	PUTJANST_LOGICALADDRESS: "SE165565594230-1000"
	SENDANSWERTOFK_LOGICALADDRESS: "2021005521"
	SENDQUESTIONTOFK_LOGICALADDRESS: "2021005521"
	SENDMESSAGETOFK_LOGICALADDRESS: "2021005521"
	IDP_OIDC_JWKS_URL: "https://idp.ineratest.org/oidc/jwks.json"
	IDP_OIDC_JWKS_SKEW: 30
   
Note 1: The `DATABASE_NAME` variable is assumed to be defined within the application deployment config.

Note 2: Parameters shall follow the Java naming convention when used as in the value field, e.g. the path to certificates indicated by the `CERTIFICATE_FOLDER` property and the truststore file might be defined like:
 
	NTJP_WS_TRUSTSTORE_FILE: "${certificate.folder}/truststore.jks"
    
        
The _&lt;env>/config/recipients.json_ file might require an update.
    
##### 3.4.1 Redis Sentinel Configuration

Redis sentinel requires at least three URL:s passed in order to work correctly. These are specified in the `REDIS_HOST` and `REDIS_PORT` parameters respectively:

    REDIS_HOST: "host1;host2;host3"
    REDIS_PORT: "26379;26379;26379"
    REDIS_SENTINEL_MASTER_NAME: "master"
    
### 3.5 Prepare Certificates

The `<env>` placeholder shall be substituted with the actual name of the environment such as `stage` or `prod`.

Staging and Prod certificates are **never** committed to git. However, you may temporarily copy them to _&lt;env>/certifikat_ in order to install/update them. Typically, certificates have probably been installed separately. The important thing is that the deployment template **requires** a secret named: `webcert-<env>-certifikat` to be available in the OpenShift project. It will be mounted to _/opt/webcert-<env>/certifikat_ in the container file system.


### 3.6 Creating Config and Secrets
If you've finished updating the files above, it's now time to use **oc** to install them into OpenShift.
All commands must be executed from the same folder as this markdown file, i.e. _/webcert/devops/openshift_ 

Note: To delete an existing ConfigMap or Secret use the following syntax:

	> oc delete [ configmap | secret ] <name>

##### 3.6.1 Create environment variables for Secret and ConfigMap
From YAML-files, their names are hard-coded into the respective file

    > oc create -f <env>/configmap-vars.yaml
    > oc create -f <env>/secret-vars.yaml
    
##### 3.6.2 Create Secret and ConfigMap
Creates config map and secret from the contents of the _&lt;env>/env_ and _&lt;env>/config_ folders:

    > oc create configmap webcert-<env>-config --from-file=<env>/config/
    > oc create secret generic webcert-<env>-env --from-file=<env>/env/ --type=Opaque
    
##### 3.6.3 Create Secret with Certificates
If this hasn't been done previously, you may **temporarily** copy keystores into the _&lt;env>/certifikat_ folder and then install them into OpenShift using this command:

    > oc create secret generic webcert-<env>-certifikat --from-file=<env>/certifikat/ --type=Opaque

### 3.7 Deploy
We're all set for deploying the application. As stated in the pre-reqs, the "deploytemplate-webapp" must be installed in the OpenShift project.

**Note 1** You need to reference the correct docker image from the Nexus!

**Note 2** Please specify the `DATABASE_NAME` actual MySQL database. Default is **webcert**.

Create a deployment:

    > oc process deploytemplate-webapp \
        -p APP_NAME=webcert \
        -p IMAGE=docker.drift.inera.se/intyg/webcert:<version> \
        -p STAGE=<env> 
        -p DATABASE_NAME=webcert \
        -o yaml | oc apply -f -
      
        
Alternatively, it's possible to use the deploytemplate-webapp file locally:

    > oc process -f deploytemplate-webapp.yaml -p APP_NAME=webcert ...

##### 3.7.1 Computing resources
WC manages hundreds to thousands of concurrent user sessions, and occasionally performs some CPU intensive operations.

Minimum production requirements are:

1. 2x CPU Cores
2. 2x GB RAM
3. 1.5x GB Java Heap Size (JAVA_OPTS=-Xmx1536M)
        
### 3.8 Verify
The pod(s) running webcert should become available within a few minutes use **oc** or Web Console to checkout the logs for progress:

	> oc logs dc/webcert[-<env>]

### 3.9 Routes
To publish WebCert a corresponding OCP route has to be created. The internal service listens on port 8080. The route should only accept `HTTPS` and is responsible of TLS termination.

WC Web Services should _only_ be accessible from inside of the OpenShift project using its _service_ name (e.g. http://intygstjanst:8080) and from Nationella tjänsteplattformen, i.e. take care when setting up an OpenShift routes so the WC Web Services can't be accessed from the Internet.
The security measures based on mutual TLS and PKI should nevertheless stop any attempts from unsolicited callers.
