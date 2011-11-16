package net.codjo.plugin.server;
import junit.framework.TestCase;
/**
 *
 */
public class AbstractServerPluginTest extends TestCase {
    public void test_abstractDoNothing() throws Exception {
        AbstractServerPlugin plugin = new MyAbstractServerPlugin();
        plugin.initContainer(null);
        plugin.start(null);
        plugin.stop();
    }


    private static class MyAbstractServerPlugin extends AbstractServerPlugin {
    }
}
