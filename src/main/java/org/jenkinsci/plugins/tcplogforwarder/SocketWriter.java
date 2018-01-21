package org.jenkinsci.plugins.tcplogforwarder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketWriter {

    private static final Logger LOG = Logger.getLogger(SocketWriter.class.getName());
    private static BufferedWriter WRITER;

    public static void write(final String msg) {
        if (isNotReady()) {
            init();
        }
        try {
            writeToSocket(msg);
        } catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage());

            // reinitialise the connection and retry
            init();

            try {
                LOG.log(Level.INFO, "Retrying message: " + msg);
                writeToSocket(msg);
            } catch (IOException e1) {
                // give up
                LOG.log(Level.SEVERE, "Aborting message: " + msg);
                throw new TcpLogforwarderException(e1);
            }
        }
    }

    private static void writeToSocket(final String msg) throws IOException {
        WRITER.write(msg);
        WRITER.flush();
        LOG.log(Level.INFO, "Wrote: " + msg);
    }

    public static void init() {
        synchronized (SocketWriter.class) {
            try {
                TcpLogForwarderConfiguration config = TcpLogForwarderConfiguration.get();
                final String host = config.getHost();
                final int port = Integer.parseInt(config.getPort());

                LOG.log(Level.INFO, "Initialising remote TCP connection at: [" + host + ":" + port + "]");
                final Socket socket = new Socket(host, port);
                WRITER = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                throw new TcpLogforwarderException(e);
            }
        }
    }

    private static boolean isNotReady() {
        return (WRITER == null);
    }
}
