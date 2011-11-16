package net.codjo.plugin.gui;
import net.codjo.plugin.common.AbstractApplicationPlugin;
/**
 *
 */
public abstract class AbstractGuiPlugin
      extends AbstractApplicationPlugin implements GuiPlugin<GuiConfiguration> {

    public void initGui(GuiConfiguration configuration) throws Exception {
    }
}
