/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.server;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.plugin.common.ApplicationCoreTestCase;
import net.codjo.test.common.fixture.CompositeFixture;
import net.codjo.test.common.fixture.SystemExitFixture;
import java.util.List;
/**
 * Classe de test de {@link ServerCore}.
 */
public class ServerCoreTest extends ApplicationCoreTestCase<ServerCore> {
    private SystemExitFixture exitFixture = new SystemExitFixture();
    private AgentContainerFixture agentFixture = new AgentContainerFixture();
    private CompositeFixture fixture = new CompositeFixture(agentFixture, exitFixture);


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


    public void test_defaultPlugins() throws Exception {
        ServerCore serverCore = new ServerCore();
        List plugins = getPlugins(serverCore);

        assertEquals(1, plugins.size());

        assertTrue(plugins.get(0) instanceof AdministrationServerPlugin);
    }


    public void test_startAndExitIfFailure() throws Exception {
        agentFixture.startContainer();

        ServerCore serverCore = new ServerCore();
        try {
            serverCore.startAndExitIfFailure(createArguments());
            fail();
        }
        catch (SecurityException exception) {
            assertEquals(SystemExitFixture.BLOCK_MESSAGE, exception.getMessage());
        }
        exitFixture.getLog().assertContent("System.exit(-1)");
    }


    @Override
    protected ServerCore createApplicationCore() {
        return new ServerCore();
    }
}
