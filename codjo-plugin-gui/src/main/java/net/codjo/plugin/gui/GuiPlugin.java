package net.codjo.plugin.gui;
import net.codjo.plugin.common.ApplicationPlugin;
/**
 *
 */
public interface GuiPlugin<T extends GuiConfiguration> extends ApplicationPlugin {
    void initGui(T configuration) throws Exception;
}
