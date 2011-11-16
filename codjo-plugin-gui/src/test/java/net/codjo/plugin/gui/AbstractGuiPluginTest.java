package net.codjo.plugin.gui;
import junit.framework.TestCase;
/**
 *
 */
public class AbstractGuiPluginTest extends TestCase {
    public void test_emptyMethods() throws Exception {
        AbstractGuiPlugin plugin = new AbstractGuiPlugin() {
        };
        plugin.initContainer(null);
        plugin.initGui(null);
        plugin.start(null);
        plugin.stop();
    }
}
