package net.codjo.plugin.gui;
import net.codjo.plugin.common.ApplicationPlugin;
import javax.swing.Action;
/**
 *
 */
public interface GuiConfiguration {
    void registerAction(ApplicationPlugin plugin, String actionId, Class<? extends Action> action);


    void registerAction(ApplicationPlugin plugin, String actionId, Action action);
}
