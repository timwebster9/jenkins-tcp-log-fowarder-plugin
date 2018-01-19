package org.jenkinsci.plugins.tcplogforwarder;

import hudson.util.ByteArrayOutputStream2;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForwarderFilterOutputStream extends FilterOutputStream {

    private static final Logger LOG = Logger.getLogger(ForwarderFilterOutputStream.class.getName());
    private static final int LF = 0x0A;

    private Socket socket;
    private ByteArrayOutputStream2 rawOutputStream = new ByteArrayOutputStream2(512);
    private ByteArrayOutputStream2 textOutputStream = new ByteArrayOutputStream2(512);
    private PrintWriter printWriter;
    private String fullDisplayName;

    public ForwarderFilterOutputStream(Socket socket, OutputStream logger, String fullDisplayName) throws IOException {
        super(logger);
        this.socket = socket;
        this.fullDisplayName = fullDisplayName;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void write(int i) throws IOException {
        super.write(i);
        this.rawOutputStream.write(i);
        if (i == LF) {
            writeLine();
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.rawOutputStream.flush();
        this.textOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.printWriter.close();
        this.rawOutputStream.close();
        this.textOutputStream.close();
        this.socket.close();
    }

    private void writeLine() throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.fullDisplayName).append('-');

        this.textOutputStream.write(stringBuilder.toString().getBytes());
        Utils.decodeConsoleBase64Text(this.rawOutputStream.getBuffer(), this.rawOutputStream.size(), this.textOutputStream);

        final String logLine = textOutputStream.toString();

        LOG.log(Level.INFO, "Writing line to TCP forwarder: " + logLine);
        this.printWriter.print(textOutputStream.toString());
        this.printWriter.flush();
        this.rawOutputStream.reset();
        this.textOutputStream.reset();
    }
}
