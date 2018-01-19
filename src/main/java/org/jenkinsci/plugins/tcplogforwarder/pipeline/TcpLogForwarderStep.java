package org.jenkinsci.plugins.tcplogforwarder.pipeline;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;
import jenkins.YesNoMaybe;
import org.jenkinsci.plugins.tcplogforwarder.TcpLogForwarder;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.BodyInvoker;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TcpLogForwarderStep extends Step {

    @DataBoundConstructor
    public TcpLogForwarderStep() {}

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        System.out.println("Starting...");
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
        public void stop(@Nonnull Throwable cause) throws Exception {
            getContext().onFailure(cause);
        }

        private ConsoleLogFilter createConsoleLogFilter(StepContext context)
                throws IOException, InterruptedException {
            ConsoleLogFilter original = context.get(ConsoleLogFilter.class);
            Run build = context.get(Run.class);
            ConsoleLogFilter subsequent = new TcpLogForwarder(build.getFullDisplayName());
            return BodyInvoker.mergeConsoleLogFilters(original, subsequent);
        }


    }

    @Extension(dynamicLoadable = YesNoMaybe.YES, optional = true)
    public static class TcpLogForwarderStepDescriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            HashSet requiredContext = new HashSet<>();
            requiredContext.add(Run.class);
            return requiredContext;
        }

        @Override
        public String getFunctionName() {
            return "tcpForwardLog";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Forward console log to a remote TCP endpoint";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }
    }

}
