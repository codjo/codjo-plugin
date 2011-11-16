package net.codjo.plugin.gui;
import net.codjo.plugin.common.ApplicationPluginMock;
import net.codjo.plugin.common.DefaultPluginsLifecycleTest.CoreWrapperMock;
import net.codjo.plugin.common.LifecycleListenerMock;
import net.codjo.test.common.LogString;
import static java.util.Arrays.asList;
import org.apache.log4j.Logger;
import org.junit.Test;
/**
 *
 */
public class GuiPluginsLifecycleTest {
    private LogString log = new LogString();


    @Test
    public void test_guiInitialisation() throws Exception {
        GuiPluginsLifecycle lifecycle = new GuiPluginsLifecycle(Logger.getRootLogger());

        lifecycle.setGuiConfiguration(new GuiConfigurationMock());

        lifecycle.start(asList(new ApplicationPluginMock(new LogString("plugin", log)),
                               new GuiPluginMock(new LogString("guiPlugin", log))),
                        new CoreWrapperMock(),
                        asList(new LifecycleListenerMock(log)));

        log.assertContent("beforeInitContainer()"
                          + ", plugin.initContainer(containerConfiguration(null))"
                          + ", guiPlugin.initContainer(containerConfiguration(null))"
                          + ", beforeStart()"
                          + ", plugin.start(agentContainer(n/a))"
                          + ", guiPlugin.start(agentContainer(n/a))"
                          + ", beforeInitGui()"
                          + ", guiPlugin.initGui(GuiConfigurationMock)"
                          + ", afterInitGui()");
    }


    @Test
    public void test_noGuiBehviour_noGuiConfiguration() throws Exception {
        GuiPluginsLifecycle lifecycle = new GuiPluginsLifecycle(Logger.getRootLogger());

        lifecycle.start(asList(new GuiPluginMock(new LogString("guiPlugin", log))),
                        new CoreWrapperMock(),
                        asList(new LifecycleListenerMock(log)));

        log.assertContent("beforeInitContainer()"
                          + ", guiPlugin.initContainer(containerConfiguration(null))"
                          + ", beforeStart()"
                          + ", guiPlugin.start(agentContainer(n/a))"
                          + ", beforeInitGui()");
    }


    public static class GuiPluginMock extends ApplicationPluginMock implements GuiPlugin {
        GuiPluginMock(LogString log) {
            super(log);
        }


        public void initGui(GuiConfiguration configuration) throws Exception {
            log.call("initGui", configuration.getClass().getSimpleName());
        }
    }
}
