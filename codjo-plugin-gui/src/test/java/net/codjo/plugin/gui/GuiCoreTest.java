package net.codjo.plugin.gui;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.plugin.common.MainBehaviourMock;
import net.codjo.test.common.LogString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
/**
 *
 */
public class GuiCoreTest {
    private GuiCore guiCore = new GuiCore();
    private LogString log = new LogString();


    @Test
    public void test_executeMainBehaviour() throws Exception {
        guiCore.setMainBehaviour(new MainBehaviourMock(log));
        guiCore.executeMainBehaviour("arg1", "arg2");
        log.assertContent("execute(arg1, arg2)");
    }


    @Test
    public void test_show_noGuiBehaviour() throws Exception {
        guiCore.setMainBehaviour(new MainBehaviourMock(log));

        guiCore.show(new String[]{"arg1", "arg2"});

        log.assertContent("execute(arg1, arg2)");
    }


    @Test
    public void test_executeMainBehaviour_mustFail() throws Exception {
        try {
            guiCore.executeMainBehaviour();
            fail();
        }
        catch (Exception e) {
            assertEquals("MainBehaviour is not set !!!", e.getLocalizedMessage());
        }
    }


    @Test
    public void test_createAgentContainer() throws Exception {
        assertNotNull(guiCore.createAgentContainer(new ContainerConfiguration()));
    }
}
