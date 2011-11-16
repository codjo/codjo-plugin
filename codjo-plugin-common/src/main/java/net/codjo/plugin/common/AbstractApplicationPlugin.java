package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
/**
 * @see ApplicationPlugin
 */
public abstract class AbstractApplicationPlugin implements ApplicationPlugin {
    public void initContainer(ContainerConfiguration containerConfiguration)
          throws Exception {
    }


    public void start(AgentContainer agentContainer)
          throws Exception {
    }


    public void stop() throws Exception {
    }
}
