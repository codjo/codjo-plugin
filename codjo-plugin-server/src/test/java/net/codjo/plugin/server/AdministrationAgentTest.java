/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.server;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Aid;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.protocol.RequestProtocol;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.TesterAgent;
import net.codjo.test.common.LogString;
import java.security.Permission;
import junit.framework.TestCase;
/**
 *
 */
public class AdministrationAgentTest extends TestCase {
    private static final String ADMINISTRATOR_AID = "phenix";
    private AgentContainerFixture fixture = new AgentContainerFixture();
    private LogString log = new LogString();


    public void test_shutdownServer() throws Exception {
        ServerCore serverMockCore = new ServerCoreMock(new LogString("agentServer", log), fixture);

        fixture.startNewAgent(ADMINISTRATOR_AID, new AdministrationAgent(serverMockCore, 0));

        TesterAgent tester = new TesterAgent();
        tester.record()
              .sendMessage(createShutdownRequest())
              .then()
              .receiveMessage()
              .assertReceivedMessage(MessageTemplate.matchProtocol(RequestProtocol.REQUEST))
              .assertReceivedMessage(MessageTemplate.matchPerformative(AclMessage.Performative.INFORM))
              .die();

        fixture.startNewAgent("tester", tester);

        fixture.waitForAgentDeath("tester");
        tester.assertNoError();

        fixture.assertUntilOk(AgentAssert.log(log, "agentServer.stop(), System.exit(0)"));
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
        blockExitWithSecurityManager(log);
    }


    @Override
    protected void tearDown() throws Exception {
        rollbackSecurityManager();
        fixture.doTearDown();
    }


    private AclMessage createShutdownRequest() {
        AclMessage requestMessage = new AclMessage(AclMessage.Performative.REQUEST);
        requestMessage.setProtocol(RequestProtocol.REQUEST);
        requestMessage.addReceiver(new Aid(ADMINISTRATOR_AID));

        requestMessage.setContentObject(AdministrationAgent.SHUTDOWN_REQUEST);
        return requestMessage;
    }


    static void blockExitWithSecurityManager(final LogString log) {
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


    static void rollbackSecurityManager() {
        System.setSecurityManager(null);
    }
}
