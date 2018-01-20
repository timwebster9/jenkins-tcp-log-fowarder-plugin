package org.jenkinsci.plugins.tcplogforwarder.pipeline;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;
import jenkins.YesNoMaybe;
import org.jenkinsci.plugins.tcplogforwarder.TcpLogForwarder;
import org.jenkinsci.plugins.tcplogforwarder.TcpLogForwarderConfiguration;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.BodyInvoker;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;

public class TcpLogForwarderStep extends Step {

    @DataBoundConstructor
    public TcpLogForwarderStep() {}

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new TcpLogForwarderStepExecution(stepContext);
    }

    private static class TcpLogForwarderStepExecution extends StepExecution {

        private static final long serialVersionUID = 1L;

        public TcpLogForwarderStepExecution(@Nonnull StepContext context) {
            super(context);
        }

        @Override
        public boolean start() throws Exception {
            StepContext context = getContext();
            context.newBodyInvoker()
                   .withContext(createConsoleLogFilter(context))
                   .withCallback(BodyExecutionCallback.wrap(context))
                   .start();
            return false;
        }

        @Override
        public void stop(@Nonnull Throwable cause) {
            TcpLogForwarderConfiguration.get().closeSocket();
            getContext().onFailure(cause);
        }

        private ConsoleLogFilter createConsoleLogFilter(StepContext context)
                throws IOException, InterruptedException {

            final ConsoleLogFilter original = context.get(ConsoleLogFilter.class);
            final Run build = context.get(Run.class);
            final ConsoleLogFilter subsequent = new TcpLogForwarder(build.getFullDisplayName());
            return BodyInvoker.mergeConsoleLogFilters(original, subsequent);
        }
    }

    @Extension(dynamicLoadable = YesNoMaybe.YES, optional = true)
    public static class TcpLogForwarderStepDescriptor extends StepDescriptor {

        static final String FUNCTION_NAME = "tcpForwardLog";
        static final String DISPLAY_NAME = "Forward console log to a remote TCP endpoint";

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(Run.class);
        }

        @Override
        public String getFunctionName() {
            return FUNCTION_NAME;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

}
