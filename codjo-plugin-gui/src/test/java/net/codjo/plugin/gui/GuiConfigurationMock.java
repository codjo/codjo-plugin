package net.codjo.plugin.gui;
import net.codjo.plugin.common.ApplicationPlugin;
import net.codjo.test.common.LogString;
import javax.swing.Action;
/**
 *
 */
public class GuiConfigurationMock implements GuiConfiguration {
    private LogString log = new LogString();


    public GuiConfigurationMock() {
    }


    public GuiConfigurationMock(LogString log) {
        this.log = log;
    }


    public void registerAction(ApplicationPlugin plugin, String actionId, Class<? extends Action> action) {
        log.call("registerAction", plugin.getClass().getSimpleName(), actionId, action.getSimpleName());
    }


    public void registerAction(ApplicationPlugin plugin, String actionId, Action action) {
        log.call("registerAction",
                 plugin.getClass().getSimpleName(),
                 actionId,
                 action.getClass().getSimpleName());
    }
}
