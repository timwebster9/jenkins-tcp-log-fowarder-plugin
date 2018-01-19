package org.jenkinsci.plugins.tcplogforwarder;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
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
    private String fullDisplayName;

    /**
     * Included for backwards compatibility (required for @Extension classes)
     */
    public TcpLogForwarder() {

    }

    public TcpLogForwarder(String fullDisplayName) {
        this.fullDisplayName = fullDisplayName;
    }

    @Override
    public OutputStream decorateLogger(Run build, OutputStream logger) throws IOException {

        final TcpLogForwarderConfiguration config = TcpLogForwarderConfiguration.get();
        final boolean isEnabled = config.isEnabled();

        LOG.log(Level.INFO, "TCP Log Forwarder status: " + isEnabled);

        if (!isEnabled) {
            return logger;
        }

        final String host = config.getHost();
        final int port = Integer.parseInt(config.getPort());

        final Socket socket = new Socket(host, port);
        return new ForwarderFilterOutputStream(socket, logger, this.getDisplayName(build));
    }

    // Horrible, but can support non-pipeline builds this way
    private String getDisplayName(Run build) {
        if (this.fullDisplayName == null) {
            if (build != null) {
                return build.getFullDisplayName();
            }
        }
        return this.fullDisplayName;
    }
}
