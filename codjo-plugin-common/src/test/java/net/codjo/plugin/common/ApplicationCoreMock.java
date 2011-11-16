package net.codjo.plugin.common;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.test.common.LogString;
/**
 *
 */
public class ApplicationCoreMock extends ApplicationCore {
    private final LogString log;
    private final AgentContainerFixture fixture;
    private Exception startException;
    private boolean mockStart;


    public ApplicationCoreMock() {
        this.log = new LogString();
        this.fixture = null;
    }


    public ApplicationCoreMock(LogString log) {
        this.log = log;
        this.fixture = null;
    }


    public ApplicationCoreMock(LogString log, AgentContainerFixture fixture) {
        this.log = log;
        this.fixture = fixture;
    }


    @Override
    public void setMainBehaviour(MainBehaviour mainBehaviour) {
        log.call("setMainBehaviour", toString(mainBehaviour));
    }


    @Override
    public void start(CommandLineArguments arguments) throws Exception {
        log.call("start");
        if (startException != null) {
            throw startException;
        }
        if (!mockStart) {
            super.start(arguments);
        }
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
    protected AgentContainer createAgentContainer(ContainerConfiguration configuration) {
        if (fixture != null) {
            return fixture.getContainer();
        }
        return AgentContainer.createMainContainer(configuration);
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


    public void mockStartException(Exception e) {
        startException = e;
    }


    public void mockStart() {
        mockStart = true;
    }


    private String toString(Object object) {
        return (object != null ? object.getClass().getSimpleName() : "null");
    }
}
