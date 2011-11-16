package net.codjo.plugin.common;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.plugin.common.PluginsLifecycle.LifecycleListener;
import net.codjo.test.common.LogString;
/**
 *
 */
public class LifecycleListenerMock extends LifecycleListener {
    private LogString log;


    public LifecycleListenerMock(LogString log) {
        this.log = log;
    }


    @Override
    public void beforeInitContainer(CommandLineArguments arguments) throws Exception {
        log.call("beforeInitContainer");
    }


    @Override
    public void beforeStart(ContainerConfiguration configuration) throws Exception {
        log.call("beforeStart");
    }


    @Override
    public void beforeStop() throws Exception {
        log.call("beforeStop");
    }


    @Override
    public void afterStop() throws Exception {
        log.call("afterStop");
    }


    @Override
    public void beforeInitGui() throws Exception {
        log.call("beforeInitGui");
    }


    @Override
    public void afterInitGui() throws Exception {
        log.call("afterInitGui");
    }
}
