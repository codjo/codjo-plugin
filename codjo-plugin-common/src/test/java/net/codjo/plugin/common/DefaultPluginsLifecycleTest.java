package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.AgentContainerMock;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.plugin.common.PluginsLifecycle.CoreWrapper;
import net.codjo.test.common.LogString;
import static java.util.Arrays.asList;
import org.apache.log4j.Logger;
import org.junit.Test;
/**
 *
 */
public class DefaultPluginsLifecycleTest {
    private DefaultPluginsLifecycle lifecycle = new DefaultPluginsLifecycle(Logger.getRootLogger());
    private LogString log = new LogString();


    @Test
    public void test_startPhase() throws Exception {
        lifecycle.start(asList(new ApplicationPluginMock(new LogString("plugin", log))),
                        new CoreWrapperMock(),
                        asList(new LifecycleListenerMock(log)));

        log.assertContent("beforeInitContainer()"
                          + ", plugin.initContainer(containerConfiguration(null))"
                          + ", beforeStart()"
                          + ", plugin.start(agentContainer(n/a))");
    }


    @Test
    public void test_startPhase_multiple() throws Exception {
        lifecycle.start(asList(new ApplicationPluginMock(new LogString("plugin1", log)),
                               new ApplicationPluginMock(new LogString("plugin2", log))),
                        new CoreWrapperMock(),
                        asList(new LifecycleListenerMock(new LogString("listener1", log)),
                               new LifecycleListenerMock(new LogString("listener2", log))));

        log.assertContent("listener1.beforeInitContainer()"
                          + ", listener2.beforeInitContainer()"
                          + ", plugin1.initContainer(containerConfiguration(null))"
                          + ", plugin2.initContainer(containerConfiguration(null))"
                          + ", listener1.beforeStart()"
                          + ", listener2.beforeStart()"
                          + ", plugin1.start(agentContainer(n/a))"
                          + ", plugin2.start(agentContainer(n/a))");
    }


    @Test
    public void test_stopPhase() throws Exception {
        lifecycle.stop(asList(new ApplicationPluginMock(new LogString("plugin", log))),
                       asList(new LifecycleListenerMock(log)));

        log.assertContent("beforeStop()"
                          + ", plugin.stop()"
                          + ", afterStop()");
    }


    @Test
    public void test_stopPhase_multiple() throws Exception {
        lifecycle.stop(asList(new ApplicationPluginMock(new LogString("plugin1", log)),
                              new ApplicationPluginMock(new LogString("plugin2", log))),
                       asList(new LifecycleListenerMock(new LogString("listener1", log)),
                              new LifecycleListenerMock(new LogString("listener2", log))));

        log.assertContent("listener1.beforeStop()"
                          + ", listener2.beforeStop()"
                          + ", plugin2.stop()"
                          + ", plugin1.stop()"
                          + ", listener1.afterStop()"
                          + ", listener2.afterStop()");
    }


    public static class CoreWrapperMock implements CoreWrapper {
        public CommandLineArguments getCommandLineArguments() {
            return null;
        }


        public ContainerConfiguration getContainerConfiguration() {
            return new ContainerConfiguration();
        }


        public AgentContainer getAgentContainer() {
            return new AgentContainerMock(new LogString());
        }
    }
}
