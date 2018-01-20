package org.jenkinsci.plugins.tcplogforwarder.pipeline;

import hudson.model.Run;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TcpLogForwarderStepDescriptorTest {

    private TcpLogForwarderStep.TcpLogForwarderStepDescriptor testSubject;

    @Before
    public void setup() {
        this.testSubject = new TcpLogForwarderStep.TcpLogForwarderStepDescriptor();
    }

    @Test
    public void getRequiredContext() throws Exception {
        assertThat(this.testSubject.getRequiredContext())
            .hasSize(1)
            .containsExactly(Run.class);
    }

    @Test
    public void getFunctionName() throws Exception {
        assertThat(this.testSubject.getFunctionName())
            .isEqualTo(TcpLogForwarderStep.TcpLogForwarderStepDescriptor.FUNCTION_NAME);
    }

    @Test
    public void getDisplayName() throws Exception {
        assertThat(this.testSubject.getDisplayName())
            .isEqualTo(TcpLogForwarderStep.TcpLogForwarderStepDescriptor.DISPLAY_NAME);
    }

    @Test
    public void takesImplicitBlockArgument() throws Exception {
        assertThat(this.testSubject.takesImplicitBlockArgument()).isEqualTo(true);
    }

}