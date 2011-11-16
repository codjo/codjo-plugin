package net.codjo.plugin.server;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.test.common.LogString;
/**
 *
 */
public class ServerCoreMock extends ServerCore {
    private final LogString log;
    private final AgentContainerFixture fixture;


    public ServerCoreMock() {
        this.log = new LogString();
        this.fixture = null;
    }


    public ServerCoreMock(LogString log) {
        this.log = log;
        this.fixture = null;
    }


    public ServerCoreMock(LogString log, AgentContainerFixture fixture) {
        this.log = log;
        this.fixture = fixture;
    }


    @Override
    public void stop() throws Exception {
        log.call("stop");
    }


    @Override
    protected void startContainer() throws ContainerFailureException {
        log.call("startContainer");
    }


    @Override
    protected AgentContainer createAgentContainer(ContainerConfiguration containerConfiguration) {
        return fixture.getContainer();
    }


    @Override
    public void addGlobalComponent(Object object) {
        super.addGlobalComponent(object);
        if (log == null) {
            return;
        }
        log.call("addGlobalComponent", toString(object));
    }


    @Override
    public <T, U extends T> void addGlobalComponent(Class<T> classKey, U object) {
        super.addGlobalComponent(classKey, object);
        if (log == null) {
            return;
        }
        log.call("addGlobalComponent", classKey.getSimpleName(), toString(object));
    }


    @Override
    public void removeGlobalComponent(Class aClass) {
        super.removeGlobalComponent(aClass);
        log.call("removeGlobalComponent", aClass.getSimpleName());
    }


    private String toString(Object object) {
        return (object != null ? object.getClass().getSimpleName() : "null");
    }
}

