package net.codjo.plugin.server;
import net.codjo.agent.ContainerConfigurationMock;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.test.common.LogString;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 */
public class AdministrationServerPluginTest extends TestCase {
    private AdministrationServerPlugin plugin;
    private LogString log = new LogString();
    private AgentContainerFixture fixture = new AgentContainerFixture();


    public void test_other() throws Exception {
        plugin.initContainer(new ContainerConfigurationMock(log));
        plugin.stop();
        log.assertContent("");
    }


    public void test_start() throws Exception {
        plugin.start(fixture.getContainer());

        fixture.assertUntilOk(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                fixture.assertContainsAgent(AdministrationServerPlugin.AGENT_NAME);
            }
        });
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
        ServerCore serverMockCore = new ServerCoreMock(log, fixture);

        plugin = new AdministrationServerPlugin(serverMockCore);
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }
}
