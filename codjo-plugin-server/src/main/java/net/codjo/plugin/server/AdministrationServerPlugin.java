package net.codjo.plugin.server;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
/**
 *
 */
public final class AdministrationServerPlugin implements ServerPlugin {
    private final ServerCore serverCore;
    public static final String AGENT_NAME = "administration-job-agent";
    private static final int DELAY_BEFORE_SHUTDOWN = 3000;


    public AdministrationServerPlugin(ServerCore serverCore) {
        this.serverCore = serverCore;
    }


    public void initContainer(ContainerConfiguration configuration) throws Exception {
    }


    public void start(AgentContainer agentContainer) throws Exception {
        AdministrationAgent agent = new AdministrationAgent(serverCore, DELAY_BEFORE_SHUTDOWN);
        agentContainer.acceptNewAgent(AGENT_NAME, agent).start();
    }


    public void stop() throws Exception {
    }
}
