package net.codjo.plugin.server;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.plugin.common.ApplicationCoreTestCase;
/**
 *
 */
public class NodeCoreTest extends ApplicationCoreTestCase<NodeCore> {
    private AgentContainerFixture fixture = new AgentContainerFixture();


    @Override
    protected void preApplicationCoreTest() {
        fixture.startContainer();
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fixture.doTearDown();
    }


    @Override
    protected NodeCore createApplicationCore() throws Exception {
        return new NodeCore();
    }
}
