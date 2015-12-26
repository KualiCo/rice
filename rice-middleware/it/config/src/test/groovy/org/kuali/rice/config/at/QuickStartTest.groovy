/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.config.at
import groovy.sql.Sql
import org.junit.*
import org.kuali.rice.core.impl.config.property.JAXBConfigImpl
/**
 * Test for the quickstart archetype.  Executes maven commands.
 */
class QuickStartTest {
    private static String basedir

    private File targetDir
    private JAXBConfigImpl config

    /**
     * determines the basedir for generating projects
     */
    @BeforeClass
    public static void setupBaseDir() {
        basedir = System.getProperty("basedir")
        if (basedir == null) {
            final String userDir = System.getProperty("user.dir")
            basedir = userDir + ((userDir.endsWith(File.separator + "it" + File.separator + "config")) ? "" : File.separator + "it" + File.separator + "config")
        }
    }

    /**
     * creates the directory to generate the projects in
     */
    @Before
    public void createTargetDir() {
        removeTargetDir()
        targetDir = new File(basedir + "/target/projects")
        if (!targetDir.exists()) {
            targetDir.mkdir()
        } else {
            removeTargetDir()
            targetDir.mkdir()
        }
        //println targetDir
    }

    /**
     * parses the test config
     */
    @Before
    public void setConfig() {
        config = new JAXBConfigImpl("classpath:META-INF/config-test-config.xml")
        config.parseConfig()

        //override jetty port in config:
        config.putProperty("http.port", getJettyPort())

        //println config
    }

    /**
     * deletes the directory to generate the projects in
     */
    @After
    public void removeTargetDir() {
        if (targetDir == null || !targetDir.exists()) {
            return
        }

        def recursiveDel
        recursiveDel = {
            it.eachDir( recursiveDel )
            it.eachFile {
                it.delete()
            }
            it.delete()
        }

        if (targetDir != null) {
            recursiveDel( targetDir )
        }
    }

    def getDatasourceOjbPlatform() { config.getProperty("datasource.ojb.platform") }
    def getDatasourceUrl() { config.getProperty("datasource.url") }
    def getDatasourceUsername() { config.getProperty("datasource.username") }
    def getDatasourcePassword() { config.getProperty("datasource.password") }
    def getDatasourceDriver() { config.getProperty("datasource.driver.name") }
    def getJettyPort() { config.getProperty("unittest.jetty.server1.port") }
    def getArchetypeVersion() { config.getProperty("rice.version") }
    def getJavaAgent() { config.getProperty("spring.instrument.javaagent") }

    //this is a hack to fix the quartz tables...  once an embedded db is supported we should use that.
    def fixQuartzTriggerTable() {
        def sql = null;

        try {
            sql = Sql.newInstance( getDatasourceUrl(), getDatasourceUsername(), getDatasourcePassword(), getDatasourceDriver() )
            sql.execute("DELETE FROM KRSB_QRTZ_LOCKS")

            sql.execute("INSERT INTO KRSB_QRTZ_LOCKS (LOCK_NAME) VALUES ('CALENDAR_ACCESS')")
            sql.execute("INSERT INTO KRSB_QRTZ_LOCKS (LOCK_NAME) VALUES ('JOB_ACCESS')")
            sql.execute("INSERT INTO KRSB_QRTZ_LOCKS (LOCK_NAME) VALUES ('MISFIRE_ACCESS')")
            sql.execute("INSERT INTO KRSB_QRTZ_LOCKS (LOCK_NAME) VALUES ('STATE_ACCESS')")
            sql.execute("INSERT INTO KRSB_QRTZ_LOCKS (LOCK_NAME) VALUES ('TRIGGER_ACCESS')")
        } finally {
            if (sql != null) {
                sql.close();
            }
        }
    }

    private OutputAwareMvnContext createStandardContext() {
        return new OutputAwareMvnContextImpl(
            args: ["org.apache.maven.plugins:maven-archetype-plugin:generate"],
            workingDir: targetDir,
            basedir: targetDir,
            addMavenOpts: true,
            quiet: false,
            silent: false,
            failOnError: true,
            deleteTempPom: true,
            stdOutWriter: new OutputStreamWriter(System.out),
            stdErrWriter: new OutputStreamWriter(System.err),
            overrideMavenOpts: null)
    }

    private Properties createStandardProperties() {
        return new Properties(
            [
                "interactiveMode": "false",
                "archetypeGroupId":"org.kuali.rice",
                "archetypeArtifactId": "rice-archetype-quickstart",
                "archetypeVersion": getArchetypeVersion(),
                "archetypeCatalog": "local,remote",
                "groupId": "org.kuali.rice",
                "artifactId": "qstest",
                "version": "1.0-SNAPSHOT",
                "package": "org.kuali.rice.qstest",
            ]
        )
    }

    private executeMaven(context) {
        new OutputAwareMvnExecutor().execute(context)
    }

    /**
     * This test generates a new project in a temp directory using the maven archetype plugin.
     */
    @Test
    void test_quickstart_gen() {
        def context = createStandardContext()
        def properties = createStandardProperties()
        context.projectProperties = properties
        context.properties = properties.keySet() as List
        executeMaven(context)
    }

    /**
     * This test generates a new project in a temp directory using the maven archetype plugin. It then executes a clean install on the project.
     * This tests that the quickstart application successfully generates, it compiles, and the unit and integration tests pass.
     */
    @Test
    void test_quickstart_gen_clean_install() {
        def context = createStandardContext()
        def properties = createStandardProperties()
        properties["goals"] = "clean install -Dmaven.failsafe.skip=true"
        context.projectProperties = properties
        context.properties = properties.keySet() as List

        executeMaven(context)
    }


    /**
     * This test generates a new project in a temp directory using the maven archetype plugin. It then executes a clean install while also running the integration tests on the project.
     * This tests that the quickstart application successfully generates, it compiles, and the unit and integration tests pass.
     * The integration test in the project make sure the project successfully starts up in an app server.
     */
    @Test
    void test_quickstart_gen_clean_install_int_tests() {
        def context = createStandardContext()
        def properties = createStandardProperties()

        def javaAgent = getJavaAgent();
        def overrideMvnOpts =  System.env["MAVEN_OPTS"] + " " + javaAgent;

        //db args for archetype generation
        properties["jetty.port"] = getJettyPort()
        properties["datasource_ojb_platform"] = getDatasourceOjbPlatform()
        properties["datasource_url"] = getDatasourceUrl()
        properties["datasource_username"] = getDatasourceUsername()
        properties["datasource_password"] = getDatasourcePassword()

        //turn on integration tests, set jetty.port for integration test run
        properties["goals"] = "clean install -Dmaven.failsafe.skip=false -Djetty.port=" + getJettyPort()
        context.projectProperties = properties
        context.properties = properties.keySet() as List

        context.setOverrideMavenOpts(overrideMvnOpts);

        //fixme: remove when we support embedded db
        fixQuartzTriggerTable();

        executeMaven(context)
    }

}
