package org.jenkinsci.plugins.tcplogforwarder;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

@Extension
public class TcpLogForwarder extends ConsoleLogFilter {

    @Override
    public OutputStream decorateLogger(Run build, OutputStream logger) throws IOException {

        final TcpLogForwarderConfiguration config = TcpLogForwarderConfiguration.get();

        if (!config.isEnabled()) {
            return logger;
        }

        final String host = config.getHost();
        final int port = Integer.parseInt(config.getPort());

        final Socket socket = new Socket(host, port);
        return new ForwarderFilterOutputStream(socket, logger, build.getFullDisplayName());
    }
}
