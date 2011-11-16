/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.StartFailureException;
import net.codjo.agent.UserId;
import net.codjo.logging.LoggingProperties;
import net.codjo.plugin.common.session.SessionListener;
import net.codjo.plugin.common.session.SessionManager;
import net.codjo.test.common.LogString;
import net.codjo.test.common.fixture.DirectoryFixture;
import net.codjo.util.file.FileUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.apache.log4j.Appender;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.spi.RootLogger;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.defaults.DefaultPicoContainer;
/**
 * Classe de test de {@link ApplicationCore}.
 */
public abstract class ApplicationCoreTestCase<T extends ApplicationCore> extends TestCase {
    protected LogString log = new LogString();
    protected T applicationCore;
    private DirectoryFixture directoryFixture = DirectoryFixture.newTemporaryDirectoryFixture();


    protected abstract T createApplicationCore() throws Exception;


    protected void preApplicationCoreTest() {
    }


    protected List getPlugins(T core) {
        return core.getPlugins();
    }


    public void test_start() throws Exception {
        preApplicationCoreTest();
        assertNull(applicationCore.getAgentContainer());

        applicationCore.start(createArguments());

        assertNotNull(applicationCore.getAgentContainer());
        assertTrue(applicationCore.getAgentContainer().isAlive());

        ContainerConfiguration containerConfiguration = applicationCore.getContainerConfig();
        assertEquals(getExpectedContainerName(), containerConfiguration.getContainerName());

        applicationCore.stop();
        assertNull(applicationCore.getAgentContainer());
    }


    public void test_start_configurationFileDoesNotExist() throws Exception {
        preApplicationCoreTest();

        try {
            applicationCore.start(createArguments("/path/do/not/exist/config.properties"));
            fail();
        }
        catch (StartFailureException ex) {
            assertEquals("Fichier de configuration est introuvable : /path/do/not/exist/config.properties",
                         ex.getLocalizedMessage());
        }
    }


    public void test_addPlugin() throws Exception {
        preApplicationCoreTest();

        ContainerConfiguration oldContainerConfiguration = applicationCore.getContainerConfig();
        assertNotNull(oldContainerConfiguration);

        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin1", log)));
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin2", log)));

        applicationCore.start(createArguments());

        log.assertContent("plugin1.initContainer(containerConfiguration(" + getExpectedContainerName() + "))"
                          + ", plugin2.initContainer(containerConfiguration(" + getExpectedContainerName()
                          + "))"
                          + ", plugin1.start(agentContainer(" + getExpectedContainerName() + "))"
                          + ", plugin2.start(agentContainer(" + getExpectedContainerName() + "))");
        log.clear();

        applicationCore.stop();
        log.assertContent("plugin2.stop(), plugin1.stop()");

        assertEquals(getDefaultPluginCount() + 2, applicationCore.getPlugins().size());
        assertNotSame(oldContainerConfiguration, applicationCore.getContainerConfig());
    }


    public void test_pluginInitFailure() throws Exception {
        preApplicationCoreTest();
        ApplicationPluginMock dummyPlugin = new ApplicationPluginMock(new LogString("plugin1", log));
        dummyPlugin.mockInitFailure(new InstantiationException("failure-init"));
        applicationCore.addPlugin(dummyPlugin);
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin2", log)));

        try {
            applicationCore.start(createArguments());
            fail();
        }
        catch (InstantiationException exception) {
            assertEquals("failure-init", exception.getLocalizedMessage());
        }

        log.assertContent(
              "plugin1.initContainer(containerConfiguration(" + getExpectedContainerName() + "))");
    }


    public void test_pluginStartFailure() throws Exception {
        String oldLogConfig = removeCurrentLoggerConfig();
        try {
            System.setProperty(LoggingProperties.LOG_DIR, directoryFixture.getAbsolutePath());
            System.setProperty(LoggingProperties.LOG_FILE_NAME, "applicationCore.log");
            applicationCore = createApplicationCore();

            preApplicationCoreTest();
            ApplicationPluginMock dummyPlugin = new ApplicationPluginMock(new LogString("plugin1", log));
            dummyPlugin.mockStartFailure(new InstantiationException("failure-init"));
            applicationCore.addPlugin(dummyPlugin);
            applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin2", log)));

            assertStartFailure();

            File logFile = new File(directoryFixture, "applicationCore.log");
            assertTrue(logFile.exists());
            String logFileContent = FileUtil.loadContent(logFile);
            assertTrue(logFileContent.contains("Plugin.start en échec : Plugin " + dummyPlugin));
            assertTrue(logFileContent.contains("Erreur dans ApplicationCore.start."));
        }
        finally {
            setLoggerConfig(oldLogConfig);
        }
    }


    public void test_pluginStartStopFailure() throws Exception {
        preApplicationCoreTest();
        ApplicationPluginMock dummyPlugin = new ApplicationPluginMock(new LogString("plugin1", log));
        dummyPlugin.mockStartFailure(new InstantiationException("failure-init"));
        dummyPlugin.mockStopFailure(new InstantiationException("failure-stop"));
        applicationCore.addPlugin(dummyPlugin);
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin2", log)));

        assertStartFailure();
    }


    public void test_pluginStopFailure() throws Exception {
        preApplicationCoreTest();
        ApplicationPluginMock dummy1 = new ApplicationPluginMock(new LogString("plugin1", log));
        dummy1.mockStopFailure(new InstantiationException("failure1"));
        applicationCore.addPlugin(dummy1);

        ApplicationPluginMock dummy2 = new ApplicationPluginMock(new LogString("plugin2", log));
        dummy2.mockStopFailure(new InstantiationException("failure2"));
        applicationCore.addPlugin(dummy2);

        applicationCore.start(createArguments());
        AgentContainer agentContainer = applicationCore.getAgentContainer();
        log.clear();

        try {
            applicationCore.stop();
            fail();
        }
        catch (InstantiationException exception) {
            assertEquals("failure1", exception.getLocalizedMessage());
        }

        log.assertContent("plugin2.stop(), plugin1.stop()");
        assertNull(applicationCore.getAgentContainer());
        assertFalse(agentContainer.isAlive());
    }


    public void test_stop_withoutStart() throws Exception {
        preApplicationCoreTest();
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin1", log)));
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin2", log)));

        applicationCore.stop();

        log.assertContent("");
    }


    public void test_startStop_removeContainerConfiguration() throws Exception {
        preApplicationCoreTest();
        applicationCore.start(createArguments());
        ContainerConfiguration containerConfiguration
              = applicationCore.getGlobalComponent(ContainerConfiguration.class);

        applicationCore.stop();
        assertNotSame(containerConfiguration,
                      applicationCore.getGlobalComponent(ContainerConfiguration.class));
    }


    public void test_defaultLoggerConfiguration() throws Exception {
        String oldLogConfig = removeCurrentLoggerConfig();
        try {
            System.setProperty(LoggingProperties.LOG_DIR, directoryFixture.getAbsolutePath());
            System.setProperty(LoggingProperties.LOG_FILE_NAME, "applicationCore.log");
            applicationCore = createApplicationCore();
            Logger.getRootLogger().info("1er log");

            File logFile = new File(directoryFixture, "applicationCore.log");
            assertTrue(logFile.exists());
            assertTrue(FileUtil.loadContent(logFile).contains("1er log"));
        }
        finally {
            setLoggerConfig(oldLogConfig);
        }
    }


    public void test_runtimeConfigOverridesDefaultConfig() throws Exception {
        String oldLogConfig = removeCurrentLoggerConfig();
        try {
            System.setProperty(ApplicationCore.LOG_CONFIGURATION,
                               getResource("ApplicationCoreTestCase_log.properties").toExternalForm());

            System.setProperty(LoggingProperties.LOG_DIR, directoryFixture.getAbsolutePath());
            System.setProperty(LoggingProperties.LOG_FILE_NAME, "applicationCore.log");

            applicationCore = createApplicationCore();
            Logger.getRootLogger().info("1er log");

            assertFalse(new File(directoryFixture, "applicationCore.log").exists());

            assertOneAppenderWithName("forTest");
        }
        finally {
            setLoggerConfig(oldLogConfig);
        }
    }


    public void test_missingDefaultLogProperties() throws Exception {
        String oldLogConfig = removeCurrentLoggerConfig();
        try {
            System.getProperties().remove(LoggingProperties.LOG_DIR);
            System.getProperties().remove(LoggingProperties.LOG_FILE_NAME);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream err = new PrintStream(bos);
            System.setErr(err);

            applicationCore = createApplicationCore();
            Logger.getRootLogger().info("1er log");

            assertTrue(bos.toString().contains("log4j:WARN Please initialize the log4j system properly."));
        }
        finally {
            setLoggerConfig(oldLogConfig);
        }
    }


    public void test_initializeLogger() throws Exception {
        String oldLogConfig = removeCurrentLoggerConfig();
        try {
            applicationCore.initializeLogger(getResource("ApplicationCoreTestCase_log.properties"));
            assertOneAppenderWithName("forTest");
        }
        finally {
            setLoggerConfig(oldLogConfig);
        }
    }


    public void test_initializeLogger_override() throws MalformedURLException {
        String oldLogConfig = removeCurrentLoggerConfig();
        setLoggerConfig(getResource("ApplicationCoreTestCase_log2.properties").toExternalForm());
        try {
            applicationCore.initializeLogger(getResource("ApplicationCoreTestCase_log.properties"));
            assertOneAppenderWithName("forTest2");
        }
        finally {
            setLoggerConfig(oldLogConfig);
        }
    }


    public void test_pico_defaultComponentInstance() throws Exception {
        assertNotNull(applicationCore.getGlobalComponent(ApplicationCore.class));
        assertNotNull(applicationCore.getGlobalComponent(DefaultPicoContainer.class));
        assertNotNull(applicationCore.getGlobalComponent(ContainerConfiguration.class));
    }


    public void test_pico_pluginCreationUsingPico() throws Exception {
        applicationCore.addGlobalComponent(log);
        applicationCore.addPlugin(ApplicationPluginMock.class);

        ApplicationPluginMock mock = getPluginMock(applicationCore);
        mock.initContainer(new ContainerConfiguration());

        log.assertContent("initContainer(containerConfiguration(null))");
    }


    public void test_pico_pluginsRelationship() throws Exception {
        applicationCore.addPlugin(new ApplicationPluginMock());
        applicationCore.addPlugin(PluginThatDependsOnPluginMock.class);

        int firstPluginIndex = getDefaultPluginCount();
        int secondPluginIndex = firstPluginIndex + 1;

        List plugins = applicationCore.getPlugins();
        assertTrue(plugins.get(secondPluginIndex) instanceof PluginThatDependsOnPluginMock);

        PluginThatDependsOnPluginMock son = (PluginThatDependsOnPluginMock)plugins.get(secondPluginIndex);
        ApplicationPluginMock expectedFather = (ApplicationPluginMock)plugins.get(firstPluginIndex);

        assertSame(expectedFather, son.getFather());
    }


    public void test_pico_pluginThatDependsOnPluginManager() throws Exception {
        applicationCore.addPlugin(PluginThatDependsOnApplicationCore.class);
        int firstPluginIndex = getDefaultPluginCount();

        ApplicationPlugin plugin = applicationCore.getPlugins().get(firstPluginIndex);
        assertSame(applicationCore, ((PluginThatDependsOnApplicationCore)plugin).getPluginManager());
    }


    public void test_pico_addGlobalComponent() throws Exception {
        applicationCore.addGlobalComponent(5);
        assertEquals(5, applicationCore.getGlobalComponent(Integer.class).intValue());

        applicationCore.addGlobalComponent(List.class, new ArrayList());
        assertEquals(0, applicationCore.getGlobalComponent(List.class).size());
    }


    public void test_pico_addGlobalComponentImplementation() throws Exception {
        applicationCore.addGlobalComponent(log);

        applicationCore.addGlobalComponentImplementation(BeanUsingLog.class);
        log.assertContent("");

        applicationCore.getGlobalComponent(BeanUsingLog.class);
        log.assertContent("BeanUsingLog.<init>()");
    }


    public void test_pico_removeGlobalComponent() throws Exception {
        applicationCore.addGlobalComponent(List.class, new ArrayList());
        assertEquals(0, applicationCore.getGlobalComponent(List.class).size());

        applicationCore.removeGlobalComponent(List.class);

        assertNull(applicationCore.getGlobalComponent(List.class));
    }


    public void test_pico_servicesBootstrapper() throws Exception {
        preApplicationCoreTest();
        assertNull(JadeWrapper.getPicoContainer(applicationCore.getContainerConfig()));

        applicationCore.start(createArguments());

        MutablePicoContainer sonPico = JadeWrapper.getPicoContainer(applicationCore.getContainerConfig());
        assertIsSon(sonPico);

        applicationCore.stop();

        assertNull(JadeWrapper.getPicoContainer(applicationCore.getContainerConfig()));
        assertFalse(applicationCore.isStopping());
    }


    public void test_pico_createChildPicoContainer() throws Exception {
        MutablePicoContainer sonPico = applicationCore.createChildPicoContainer();
        assertIsSon(sonPico);
    }


    public void test_sessionManagement() throws Exception {
        applicationCore.addGlobalComponent(new LogString("plugin", log));
        applicationCore.addPlugin(PluginThatIsSessionListener.class);

        PluginThatIsSessionListener plugin = applicationCore.getPlugin(PluginThatIsSessionListener.class);

        plugin.getSessionManager().startSession(UserId.createId("jones", "p"));

        log.assertContent("plugin.handleSessionStart(jones)");
    }


    public void test_lifecycleListeners() throws Exception {
        preApplicationCoreTest();

        applicationCore.addLifecycleListener(new LifecycleListenerMock(new LogString(" listener ", log)));

        applicationCore.start(createArguments());

        log.assertAndClear(Pattern.compile(".* listener .*"));

        applicationCore.stop();

        log.assertAndClear(Pattern.compile(".* listener .*"));
    }


    public void test_picoStartAndStop() throws Exception {
        Startable startable = new Startable() {
            public void start() {
                log.call("start");
            }


            public void stop() {
                log.call("stop");
            }
        };

        applicationCore.addGlobalComponent(startable);

        preApplicationCoreTest();
        applicationCore.start(createArguments());

        log.assertContent("start()");
        log.clear();

        applicationCore.stop();

        log.assertContent("stop()");
        log.clear();
    }


    private void assertIsSon(MutablePicoContainer son) {
        MutablePicoContainer father = applicationCore.getPicoContainer();

        assertNotNull(son);
        assertNotSame(father, son);

        son.registerComponentInstance(5);
        assertNull(father.getComponentInstance(Integer.class));

        father.registerComponentInstance("test");
        assertEquals("test", son.getComponentInstance(String.class));
    }


    @Override
    protected void setUp() throws Exception {
        applicationCore = createApplicationCore();
        directoryFixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        try {
            applicationCore.stop();
        }
        catch (Exception e) {
            ; // Pas grave
        }
        directoryFixture.doTearDown();
    }


    private void assertStartFailure() throws Exception {
        try {
            applicationCore.start(createArguments());
            fail();
        }
        catch (InstantiationException exception) {
            assertEquals("failure-init", exception.getLocalizedMessage());
        }

        log.assertContent("plugin1.initContainer(containerConfiguration(" + getExpectedContainerName() + "))"
                          + ", plugin2.initContainer(containerConfiguration(" + getExpectedContainerName()
                          + "))"
                          + ", plugin1.start(agentContainer(" + getExpectedContainerName() + "))"
                          + ", plugin2.stop(), plugin1.stop()");

        log.clear();
        applicationCore.stop();
        log.assertContent("");
    }


    protected CommandLineArguments createArguments() {
        return createArguments(getContainerConfigFile().getPath());
    }


    protected CommandLineArguments createArguments(String configurationPath) {
        return new CommandLineArguments(new String[]{"-" + ApplicationCore.CONFIGURATION, configurationPath});
    }


    protected File getContainerConfigFile() {
        String fileName = getTestConfigFileName();
        URL resource = ApplicationCoreTestCase.class.getResource(fileName);
        if (resource == null) {
            resource = getClass().getResource(fileName);
        }
        if (resource == null) {
            throw new RuntimeException("La ressource '" + fileName + "' est introuvable.");
        }
        File output = new File(directoryFixture, fileName);
        try {
            String content = FileUtil.loadContent(new InputStreamReader(resource.openStream()));
            FileUtil.saveContent(output, content);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output;
    }


    protected String getExpectedContainerName() {
        return "applicationCore";
    }


    protected String getTestConfigFileName() {
        return "ApplicationCoreTestCase.properties";
    }


    private void assertOneAppenderWithName(String appenderName) {
        Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
        assertTrue(allAppenders.hasMoreElements());
        Appender appender = (Appender)allAppenders.nextElement();
        assertEquals(appenderName, appender.getName());
        assertFalse(allAppenders.hasMoreElements());
    }


    private void setLoggerConfig(String oldLogConfig) throws MalformedURLException {
        if (oldLogConfig == null) {
            return;
        }
        System.getProperties().setProperty(ApplicationCore.LOG_CONFIGURATION, oldLogConfig);
        PropertyConfigurator.configure(new URL(oldLogConfig));
    }


    private URL getResource(String name) {
        return ApplicationCoreTestCase.class.getResource(name);
    }


    private String removeCurrentLoggerConfig() {
        String oldLogConfig = System.getProperty(ApplicationCore.LOG_CONFIGURATION);
        System.getProperties().remove(ApplicationCore.LOG_CONFIGURATION);
        // Hack pour revenir aux conditions de départ du logger, sans influence des tests précédents.
        Hierarchy hierarchy = new Hierarchy(new RootLogger(Level.DEBUG));
        RepositorySelector repositorySelector = new DefaultRepositorySelector(hierarchy);
        LogManager.setRepositorySelector(repositorySelector, null);
        return oldLogConfig;
    }


    private ApplicationPluginMock getPluginMock(ApplicationCore manager) throws Exception {
        List plugins = manager.getPlugins();
        assertEquals(getDefaultPluginCount() + 1, plugins.size());
        assertTrue(plugins.get(getDefaultPluginCount()) instanceof ApplicationPluginMock);
        return (ApplicationPluginMock)plugins.get(getDefaultPluginCount());
    }


    private int getDefaultPluginCount() throws Exception {
        return createApplicationCore().getPlugins().size();
    }


    public static class BeanUsingLog {
        public BeanUsingLog(LogString log) {
            log.call("BeanUsingLog.<init>");
        }
    }
    public static class PluginThatDependsOnPluginMock extends ApplicationPluginMock {
        private final ApplicationPluginMock mock;


        public PluginThatDependsOnPluginMock(ApplicationPluginMock mock) {
            this.mock = mock;
        }


        public ApplicationPluginMock getFather() {
            return mock;
        }
    }
    public static class PluginThatDependsOnApplicationCore extends ApplicationPluginMock {
        private final ApplicationCore pluginManager;


        public PluginThatDependsOnApplicationCore(ApplicationCore server) {
            this.pluginManager = server;
        }


        public ApplicationCore getPluginManager() {
            return pluginManager;
        }
    }
    public static class PluginThatIsSessionListener extends ApplicationPluginMock implements SessionListener {
        private final SessionManager sessionManager;
        private final LogString logger;


        public PluginThatIsSessionListener(SessionManager sessionManager, LogString log) {
            this.sessionManager = sessionManager;
            this.logger = log;
        }


        public SessionManager getSessionManager() {
            return sessionManager;
        }


        public void handleSessionStart(UserId userId) {
            logger.call("handleSessionStart", userId.getLogin());
        }


        public void handleSessionStop(UserId userId) {
            logger.call("handleSessionStop", userId.getLogin());
        }
    }
}
