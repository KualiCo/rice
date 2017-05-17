# Kuali Rice

Kuali Rice is an open source application development framework and middleware suite developed for and by the higher
education community.

Kuali Rice is currently in maintenance mode, but does continue to be maintained from the perspective of security,
bug fixes, and minor enhancements while its replacements are being developed. This repository only contains the latest
version of the Kuali Rice source code. Source code for versions 1.0 through 2.5 of Kuali Rice can be found at
https://github.com/kuali/rice

## Running Rice for Development

### Project and Database Setup

* Install the latest version of MySQL Community Edition
  * Be sure to remember your root password!
* Clone this repository
* Run a `mvn install` from the command line after cloning (this will probably take awhile)
* Change directory to `db/impex/master` in directory where you cloned the repository
* Run the following to ensure that you can connect to your mysql instance
  * ```mvn validate -Pdb,mysql -Dimpex.dba.password=[root password]```
  * if you have no root passwords use `NONE` for the password
  * if this does not work, make sure your MySQL database is up and running and try again
  * if you want to tweak the schema name, username, or password, see [](https://wiki.kuali.org/display/KULRICE/Load+Impex+Data+via+Maven)
* Execute the following from the root of your project:
```mvn clean install -Pdb,mysql -Dimpex.dba.password=[root password]```
* You will now have a mysql database created with the name, username, and password of "RICE"

### Configuration

* Copy the `rice.keystore` file from `rice-middleware/security/rice.keystore` to your `/usr/local/rice` directory (create this directory if it does not exist)
* For Kuali Rice standalone you will need a file at `/usr/local/rice/rice-config.xml` in order to configure the database
* Create the file at that location using the following template:
```
<config>
    <param name="appserver.url">http://localhost:8080</param>
    <param name="app.context.name">rice-standalone</param>
    
    <param name="keystore.file">/usr/local/rice/rice.keystore</param>
    <param name="keystore.alias">rice</param>
    <param name="keystore.password">r1c3pw</param>

    <param name="datasource.url">jdbc:mysql://localhost:3306/RICE</param>
    <param name="datasource.username">RICE</param>
    <param name="datasource.password">RICE</param>
    <param name="datasource.driver.name">${datasource.driver.name.MySQL}</param>
    <param name="datasource.pool.minSize">3</param>
    <param name="datasource.pool.maxOpenPreparedStatements">500</param>
    <param name="datasource.platform">${datasource.platform.MySQL}</param>
    <param name="datasource.ojb.platform">MySQL</param>
    
    <param name="filter.login.class">org.kuali.rice.krad.web.filter.DummyLoginFilter</param>
    <param name="filtermapping.login.1">/*</param>

</config>
```

### Running with S3 Attachments

If you want to use S3 for attachments for KEW and KRAD you will need to do the following:

1. Pass a `-Dspring.profiles.active=s3` parameter to the startup of the JVM for Tomcat
2. Add configuration to your rice-config.xml file for S3 as follows:

```
<param name="cloud.aws.credentials.accessKey">...</param>
<param name="cloud.aws.credentials.secretKey">...</param>
<param name="rice.kew.attachments.s3.bucketName">rice-bucket-name</param>
<param name="rice.kew.attachments.s3.folderName">dev/kew</param>
<param name="rice.krad.attachments.s3.bucketName">rice-bucket-name</param>
<param name="rice.krad.attachments.s3.folderName">dev/krad</param>
```

Note that the access and secret keys will need to allow read/write access to the configured bucket. Additionally, KEW will store it's attachments under the specified folder (`dev/kew` in the above example). Be sure to set this appropriately, especially if reusing buckets across environments.

### Development in Eclipse

* Download and install the latest version of Eclipse for J2EE developers
* Increase the max memory used when launching eclipse from 1024 (the default) to at least 2048.
* Install the Groovy eclipse and m2e connector plugins from the following url in the Eclipse Marketplace: http://dist.springsource.org/snapshot/GRECLIPSE/e4.5/
* Disable all validators globally in Eclipse
* Add an Ant property pointing to your Eclipse workspace
  * Eclipse -> Preferences -> Ant -> Runtime -> Properties -> Add Property
  * Set name as "eclipse.workspace" and value as "${workspace_loc}"
* File -> Import.. -> Existing Maven Projects
* Then browse the folder where you cloned the repo and ensure that all of the various pom.xml modules are selected and click "Finish"
* After approximately one year of waiting, the build should complete and hopefully there will be no errors

### Setting up Dev Environment for Running Integration Tests

* Change directory to `db/impex/master` in directory where you cloned the repository
* Run the following to ensure that you can connect to your mysql instance
  * ```mvn validate -Pdb,mysql -Dimpex.dba.password=[root password]```
  * if you have no root passwords use `NONE` for the password
  * if this does not work, make sure your MySQL database is up and running and try again
* Execute the following from the root of your project:
```mvn clean install -Pdb,mysql,integration-test -Dimpex.dba.password=[root password] -Dimpex.username=RICECI```
* You will now have a mysql database for purposes of continuous integration created with the name, username, and password of "RICECI"
* Next, run "prepare-unit-test-environment" from the Ant build.xml file in the root of the project
  * Note that if you already have a unit test config file in place this will fail
* If you want the S3-related tests to pass, then you will need to add configuration to your common-test-config.xml file for S3 attachment configuration as specified earlier. Be sure to use an appropriate folder name for your integration tests so that it does not conflict with your development or other testing environments. There is no need to manually pass `-Dspring.profiles.active=s3` when running your integration tests as the appropriate integration test modules will set this profile when forking the JVM for the test run.
* To run the integration tests from the command line, execute the following:
```mvn -Pitests verify```

## Running the Kuali Rice Standalone Server

To get the WAR file that contains the Kuali Rice server, simply run the following from the root of the project:

```mvn package```

The Kuali Rice Standalone WAR will then be available under the `rice-middleware/standalone/target` directory.
Alternatively, you can simply grab the [already built WAR from Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.kuali.rice%22%20AND%20a%3A%22rice-standalone%22).



