package net.codjo.plugin.server;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.TesterAgent;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.plugin.server.AdministrationShutdowner}.
 */
public class AdministrationShutdownerTest extends TestCase {
    private LogString log = new LogString();
    private AgentContainerFixture fixture = new AgentContainerFixture();


    public void test_shutdown() throws Exception {
        fixture.startContainer();
        checkShutdown(AclMessage.Performative.INFORM, "System.exit(0)");
    }


    public void test_shutdownFailures() throws Exception {
        fixture.startContainer();
        checkShutdown(AclMessage.Performative.FAILURE, "System.exit(-1)");
    }


    public void test_mainContainerIsMissing() throws Exception {
        try {
            AdministrationShutdowner.main(new String[]{"localhost", "" + AgentContainer.CONTAINER_PORT});
            fail();
        }
        catch (SecurityException e) {
            log.assertContent("System.exit(0)");
        }
    }


    public void test_arguments() throws Exception {
        assertBadArguments(null);
        assertBadArguments(new String[]{"localhost"});
        assertBadArguments(new String[]{"localhost", "I am not a number"});
    }


    @Override
    protected void setUp() throws Exception {
        AdministrationAgentTest.blockExitWithSecurityManager(log);
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        AdministrationAgentTest.rollbackSecurityManager();
        fixture.doTearDown();
    }


    private void checkShutdown(AclMessage.Performative serverResponse, final String reaction)
          throws ContainerFailureException {
        TesterAgent testerAgent = new TesterAgent();
        testerAgent.record()
              .receiveMessage()
              .assertReceivedMessage(AdministrationAgent.matchShutdownRequest())
              .replyWithContent(serverResponse, null)
              .die();

        fixture.startNewAgent(AdministrationServerPlugin.AGENT_NAME, testerAgent);
        fixture.assertContainsAgent(AdministrationServerPlugin.AGENT_NAME);

        AdministrationShutdowner.main(new String[]{"localhost", "" + AgentContainer.CONTAINER_PORT});

        fixture.waitForAgentDeath(AdministrationServerPlugin.AGENT_NAME);
        testerAgent.assertNoError();
        fixture.assertUntilOk(AgentAssert.log(log, reaction));
    }


    private void assertBadArguments(String[] args) {
        try {
            AdministrationShutdowner.main(args);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(AdministrationShutdowner.USAGE_MESSAGE, ex.getMessage());
        }
    }
}
