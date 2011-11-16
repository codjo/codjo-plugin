package net.codjo.plugin.common;
import net.codjo.plugin.common.ApplicationCore.MainBehaviour;
import net.codjo.test.common.LogString;
/**
 *
 */
public class MainBehaviourMock implements MainBehaviour {
    private final LogString logString;


    public MainBehaviourMock(LogString logString) {
        this.logString = logString;
    }


    public void execute(String... args) {
        logString.call("execute", args);
    }
}
