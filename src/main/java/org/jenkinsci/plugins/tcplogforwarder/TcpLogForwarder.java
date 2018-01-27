package org.jenkinsci.plugins.tcplogforwarder;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
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
    public OutputStream decorateLogger(final Run build, final OutputStream logger) {

        final TcpLogForwarderConfiguration config = getConfig();
        final boolean isEnabled = config.isEnabled();

        if (!isEnabled) {
            return logger;
        }

        final String host = config.getHost();
        final int port = Integer.parseInt(config.getPort());

        try {
            final Socket socket = getSocket(host, port);
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
            return new ForwarderFilterOutputStream(writer, logger, this.getJobDescription(build), config.getMaxMessageValue());
        }
        catch (final IOException e) {
            LOG.log(Level.SEVERE, "Error!: " + e.getMessage());
            LOG.log(Level.SEVERE, "Error establishing socket connection to [" + host + ":" + port + "].  Aborting TCP Log Forwarder.");
            return logger;
        }
    }

    @VisibleForTesting
    TcpLogForwarderConfiguration getConfig() {
        return TcpLogForwarderConfiguration.get();
    }

    @VisibleForTesting
    Socket getSocket(final String host, final int port) throws IOException {
        return new Socket(host, port);
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
