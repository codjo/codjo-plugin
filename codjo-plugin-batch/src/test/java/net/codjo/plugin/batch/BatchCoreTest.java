package net.codjo.plugin.batch;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Permission;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.plugin.common.ApplicationCoreTestCase;
import net.codjo.plugin.common.ApplicationPlugin;
import net.codjo.plugin.common.ApplicationPluginMock;
import net.codjo.plugin.common.CommandLineArguments;
import net.codjo.test.common.LogString;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
/**
 *
 */
public class BatchCoreTest extends ApplicationCoreTestCase<BatchCore> {
    private AgentContainerFixture fixture = new AgentContainerFixture();
    private UserId userId = UserId.createId("me", "secret");
    private ByteArrayOutputStream loggerMock;


    @Override
    protected void preApplicationCoreTest() {
        fixture.startContainer();
    }


    @Override
    protected BatchCore createApplicationCore() throws Exception {
        return new BatchCore();
    }


    @Override
    public void test_start_configurationFileDoesNotExist() throws Exception {
        preApplicationCoreTest();
        blockExitWithSecurityManager();

        try {
            applicationCore.start(createArguments("/path/do/not/exist/config.properties"));
            fail();
        }
        catch (SecurityException ex) {
            assertTrue(loggerMock.toString()
                             .contains(
                                   "net.codjo.agent.StartFailureException: Fichier de configuration est introuvable : "
                                   + "/path/do/not/exist/config.properties"));
        }
    }


    @Override
    public void test_pluginInitFailure() throws Exception {
        preApplicationCoreTest();
        blockExitWithSecurityManager();

        ApplicationPluginMock dummyPlugin = new ApplicationPluginMock(new LogString("plugin1", log));
        dummyPlugin.mockInitFailure(new InstantiationException("failure-init"));
        applicationCore.addPlugin(dummyPlugin);
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin2", log)));

        try {
            applicationCore.start(createArguments());
            fail();
        }
        catch (SecurityException exception) {
            assertTrue(loggerMock.toString().contains("java.lang.InstantiationException: failure-init"));
        }

        log.assertContent(
              "plugin1.initContainer(containerConfiguration(" + getExpectedContainerName() + ")), "
              + "System.exit(50)");
    }


    @Override
    public void test_pluginStartFailure() throws Exception {
        preApplicationCoreTest();
        blockExitWithSecurityManager();

        ApplicationPluginMock dummyPlugin = new ApplicationPluginMock(new LogString("plugin1", log));
        dummyPlugin.mockStartFailure(new InstantiationException("failure-init"));
        applicationCore.addPlugin(dummyPlugin);
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin2", log)));

        startAndCheckFailure();

        assertTrue(loggerMock.toString().contains("Plugin.start en échec : Plugin " + dummyPlugin));
        assertTrue(loggerMock.toString().contains("Erreur dans ApplicationCore.start."));
    }


    @Override
    public void test_pluginStartStopFailure() throws Exception {
        preApplicationCoreTest();
        blockExitWithSecurityManager();

        ApplicationPluginMock dummyPlugin = new ApplicationPluginMock(new LogString("plugin1", log));
        dummyPlugin.mockStartFailure(new InstantiationException("failure-init"));
        dummyPlugin.mockStopFailure(new InstantiationException("failure-stop"));
        applicationCore.addPlugin(dummyPlugin);
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin2", log)));

        startAndCheckFailure();

        assertTrue(loggerMock.toString().contains("Plugin.start en échec : Plugin " + dummyPlugin));
        assertTrue(loggerMock.toString().contains("Erreur dans ApplicationCore.start."));
    }


    public void test_start_withoutMainContainer() throws Exception {
        blockExitWithSecurityManager();
        try {
            applicationCore.start(createArguments());
            fail();
        }
        catch (SecurityException e) {
            log.assertContent("System.exit(50)");
        }
    }


    public void test_execute() throws Exception {
        applicationCore.addPlugin(new ApplicationPluginMock(new LogString("plugin1", log)));
        applicationCore.addPlugin((ApplicationPlugin)new BatchPluginMock("export", log));
        applicationCore.addPlugin(new BatchPluginMock("import", new LogString("import", log)));

        applicationCore.execute(userId, createArguments("import", "importArgument"));

        log.assertContent("import.execute(userId(me/secret)"
                          + ", CommandLineArguments(importArgument))");
    }


    public void test_execute_withoutNeededPlugin() throws Exception {
        try {
            applicationCore.execute(userId, createArguments("import", "importArgument"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Le plugin de type 'import' est inconnu", ex.getMessage());
        }
    }


    public void test_executeAndExit() throws Exception {
        blockExitWithSecurityManager();

        applicationCore = new BatchCore(createArguments("import", "importArgument"));
        applicationCore.addGlobalComponent(userId);
        applicationCore.addPlugin(new BatchPluginMock("import", new LogString("plugin", log)));
        fixture.startContainer();
        applicationCore.start(createArguments());
        log.clear();

        try {
            applicationCore.executeAndExit();
        }
        catch (SecurityException e) {
        }

        log.assertContent("plugin.execute(userId(me/secret)"
                          + ", CommandLineArguments(importArgument))"
                          + ", plugin.stop()"
                          + ", System.exit(0)");
    }


    public void test_executeAndExit_withProvidedUserId() throws Exception {
        blockExitWithSecurityManager();

        applicationCore = new BatchCore(createArguments("import", "importArgument"));
        applicationCore.addPlugin(new BatchPluginMock("import", new LogString("plugin", log)));
        fixture.startContainer();
        applicationCore.start(createArguments());
        log.clear();

        try {
            applicationCore.executeAndExit(userId);
        }
        catch (SecurityException e) {
        }

        log.assertContent("plugin.execute(userId(me/secret)"
                          + ", CommandLineArguments(importArgument))"
                          + ", plugin.stop()"
                          + ", System.exit(0)");
    }


    public void test_executeAndExit_jobFailure() throws Exception {
        blockExitWithSecurityManager();
        BatchPluginMock plugin = new BatchPluginMock("import");
        applicationCore.addPlugin(plugin);

        plugin.mockExecuteFailure(new BatchException("error"));
        executeAndExit();

        log.assertContent("System.exit(" + BatchCore.EXIT_WITH_JOB_ERROR + ")");
    }


    public void test_executeAndExit_jobTimeout() throws Exception {
        blockExitWithSecurityManager();
        BatchPluginMock plugin = new BatchPluginMock("import");
        applicationCore.addPlugin(plugin);

        plugin.mockExecuteFailure(new TimeoutBatchException("", 123L));
        executeAndExit();

        log.assertContent("System.exit(" + BatchCore.EXIT_WITH_TIMEOUT + ")");
    }


    public void test_executeAndExit_internalError() throws Exception {
        blockExitWithSecurityManager();
        BatchPluginMock plugin = new BatchPluginMock("import");
        applicationCore.addPlugin(plugin);

        plugin.mockExecuteFailure(new ContainerFailureException());
        executeAndExit();

        log.assertContent("System.exit(" + BatchCore.EXIT_WITH_INTERNAL_ERROR + ")");
    }


    public void test_addPlugin_withSameType() throws Exception {
        applicationCore.addPlugin(new BatchPluginMock("export", new LogString("export", log)));
        try {
            applicationCore.addPlugin(new BatchPluginMock("export", new LogString("export", log)));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Le plugin de type 'export' est déjà défini", ex.getMessage());
        }
    }


    public void test_checkAndLoadArguments() throws Exception {
        assertCheckFailure(BatchCore.CONFIGURATION, new String[]{});

        assertCheckFailure(BatchCore.BATCH_TYPE,
                           new String[]{
                                 "-" + BatchCore.CONFIGURATION, getContainerConfigFile().getPath(),
                           });

        assertCheckFailure(BatchCore.BATCH_INITIATOR,
                           new String[]{
                                 "-" + BatchCore.CONFIGURATION, getContainerConfigFile().getPath(),
                                 "-" + BatchCore.BATCH_TYPE, "", "-" + BatchCore.BATCH_ARGUMENT, "",
                           });

        assertArgumentsAreValid(new CommandLineArguments(
              new String[]{
                    "-" + BatchCore.CONFIGURATION, getContainerConfigFile().getPath(),
                    "-" + BatchCore.BATCH_TYPE, "", "-" + BatchCore.BATCH_ARGUMENT, "",
                    "-" + BatchCore.BATCH_INITIATOR, "",
              }));

        assertArgumentsAreValid(new CommandLineArguments(
              new String[]{
                    "-" + BatchCore.CONFIGURATION, getContainerConfigFile().getPath(),
                    "-" + BatchCore.BATCH_TYPE, "",
                    "-" + BatchCore.BATCH_INITIATOR, "",
              }));
    }


    public void test_checkAndLoadArguments_compatibility() throws Exception {
        CommandLineArguments arguments =
              new CommandLineArguments(new String[]{
                    "-configFile", getContainerConfigFile().getPath(), "-batchType",
                    "type", "-arg", "argument", "-initiator", "initiator",
              });

        applicationCore.checkAndLoadArgumentsFromConfiguration(arguments);

        assertArgumentEquality(arguments, "configFile", BatchCore.CONFIGURATION);
        assertArgumentEquality(arguments, "batchType", BatchCore.BATCH_TYPE);
        assertArgumentEquality(arguments, "initiator", BatchCore.BATCH_INITIATOR);
        assertArgumentEquality(arguments, "arg", BatchCore.BATCH_ARGUMENT);

        arguments =
              new CommandLineArguments(new String[]{
                    "-configFile", getContainerConfigFile().getPath(), "-batchType",
                    "type", "-arg", "argument", "-initiator", "initiator", "-date",
                    "2006-01-01",
              });

        assertArgumentEquality(arguments, "date", BatchCore.BATCH_DATE);
    }


    private void assertArgumentEquality(CommandLineArguments arguments, String argA, String argB) {
        assertEquals(arguments.getArgument(argA), arguments.getArgument(argB));
    }


    private void assertArgumentsAreValid(CommandLineArguments validArguments) throws IOException {
        applicationCore.checkAndLoadArgumentsFromConfiguration(validArguments);
    }


    private void startAndCheckFailure() throws Exception {
        try {
            applicationCore.start(createArguments());
            fail();
        }
        catch (SecurityException exception) {
            assertTrue(loggerMock.toString().contains("java.lang.InstantiationException: failure-init"));
        }

        log.assertContent("plugin1.initContainer(containerConfiguration(" + getExpectedContainerName() + "))"
                          + ", plugin2.initContainer(containerConfiguration(" + getExpectedContainerName()
                          + "))"
                          + ", plugin1.start(agentContainer(" + getExpectedContainerName() + "))"
                          + ", plugin2.stop(), plugin1.stop(), "
                          + "System.exit(50)");

        log.clear();
        applicationCore.stop();
        log.assertContent("");
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fixture.doSetUp();

        Logger logger = Logger.getRootLogger();
        loggerMock = new ByteArrayOutputStream();
        logger.addAppender(new WriterAppender(new SimpleLayout(), loggerMock));
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setSecurityManager(null);
        try {
            applicationCore.stop();
        }
        catch (Exception e) {
            //
        }

        fixture.doTearDown();
    }


    private void assertCheckFailure(String key, String[] arguments) throws IOException {
        try {
            applicationCore.checkAndLoadArgumentsFromConfiguration(new CommandLineArguments(arguments));
            fail(key + " doit etre présentes");
        }
        catch (IllegalArgumentException ex) {
            assertEquals("L'argument obligatoire '" + key + "' est absent.", ex.getMessage());
        }
    }


    private void executeAndExit() throws Exception {
        try {
            applicationCore.executeAndExit(userId, createArguments("import", "n/a"));
            fail();
        }
        catch (SecurityException ex) {
            //
        }
    }


    private void blockExitWithSecurityManager() {
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkExit(int status) {
                log.call("System.exit", "" + status);
                throw new SecurityException();
            }


            @Override
            public void checkPermission(Permission perm) {
            }
        });
    }


    private CommandLineArguments createArguments(String type, String arg) {
        return new CommandLineArguments(new String[]{
              "-" + BatchCore.CONFIGURATION, getContainerConfigFile().getPath(),
              "-" + BatchCore.BATCH_INITIATOR, "user_test",
              "-" + BatchCore.BATCH_TYPE, type,
              "-" + BatchCore.BATCH_ARGUMENT, arg,
        });
    }
}