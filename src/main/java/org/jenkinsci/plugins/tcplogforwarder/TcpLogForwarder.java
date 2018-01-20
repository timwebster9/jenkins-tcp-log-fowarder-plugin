package org.jenkinsci.plugins.tcplogforwarder;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is only annotated as an Extension to support non-pipeline builds.  It is
 * not used as an extension in pipelines.
 */
@Extension
public class TcpLogForwarder extends ConsoleLogFilter implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(TcpLogForwarder.class.getName());

    private String jobDescription;

    /**
     * Included for backwards compatibility (required for @Extension classes)
     */
    public TcpLogForwarder() {
    }

    /**
     * Called from Pipeline step
     *
     * @param jobDescription the job display name/number
     */
    public TcpLogForwarder(final String jobDescription) {
        this.jobDescription = jobDescription;
    }

    @Override
    public OutputStream decorateLogger(final Run build, final OutputStream logger) throws IOException {

        final TcpLogForwarderConfiguration config = getConfig();
        final boolean isEnabled = config.isEnabled();

        LOG.log(Level.INFO, "TCP Log Forwarder status: " + isEnabled);

        if (!isEnabled) {
            return logger;
        }

        return new ForwarderFilterOutputStream(config.getSocket(), logger, this.getJobDescription(build));
    }

    TcpLogForwarderConfiguration getConfig() {
        return TcpLogForwarderConfiguration.get();
    }

    // Horrible, but can support non-pipeline builds this way
    private String getJobDescription(Run build) {
        if (this.jobDescription == null) {
            if (build != null) {
                return build.getFullDisplayName();
            }
        }
        return this.jobDescription;
    }

}
