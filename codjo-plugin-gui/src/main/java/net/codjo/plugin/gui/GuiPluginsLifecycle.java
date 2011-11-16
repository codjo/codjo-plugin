package net.codjo.plugin.gui;
import net.codjo.plugin.common.ApplicationPlugin;
import net.codjo.plugin.common.DefaultPluginsLifecycle;
import java.util.List;
import org.apache.log4j.Logger;
/**
 *
 */
public class GuiPluginsLifecycle extends DefaultPluginsLifecycle {
    private GuiConfiguration guiConfiguration;


    public GuiPluginsLifecycle(Logger logger) {
        super(logger);
    }


    public void setGuiConfiguration(GuiConfiguration guiConfiguration) {
        this.guiConfiguration = guiConfiguration;
    }


    @Override
    protected void startExtensionPoint(PluginsDispatcher pluginsDispatcher,
                                       CoreWrapper coreWrapper,
                                       ListenersDispatcher listeners) throws Exception {
        logger().info("**** Initialisation Gui");
        listeners.beforeInitGui();

        if (guiConfiguration == null) {
            logger().info("Activation annulee (aucun guiConfiguration disponible)");
            return;
        }

        dispatchInitGuiCall(pluginsDispatcher);

        listeners.afterInitGui();
    }


    void dispatchInitGuiCall(PluginsDispatcher pluginsDispatcher) throws Exception {
        List<? extends ApplicationPlugin> plugins = pluginsDispatcher.getPlugins();
        for (ApplicationPlugin plugin : plugins) {
            if (plugin instanceof GuiPlugin) {
                GuiPlugin guiPlugin = (GuiPlugin)plugin;
                logger().info("Plugin.initGui " + plugin);
                // HACK : Pour generaliser les GuiPlugins
                //noinspection unchecked
                guiPlugin.initGui(guiConfiguration);
            }
        }
    }
}