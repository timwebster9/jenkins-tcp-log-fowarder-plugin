package org.jenkinsci.plugins.tcplogforwarder;

import com.google.common.annotations.VisibleForTesting;
import hudson.console.ConsoleNote;
import hudson.util.ByteArrayOutputStream2;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForwarderFilterOutputStream extends FilterOutputStream {  // NOSONAR

    private static final Logger LOG = Logger.getLogger(ForwarderFilterOutputStream.class.getName());

    static final int LF = 0x0A;
    static final String SEPARATOR = " - ";

    private BufferedWriter writer;
    private ByteArrayOutputStream2 rawOutputStream = new ByteArrayOutputStream2(512);
    private ByteArrayOutputStream2 textOutputStream = new ByteArrayOutputStream2(512);
    private String fullDisplayName;
    private long maxMessageSize;

    public ForwarderFilterOutputStream(final BufferedWriter writer, final OutputStream logger,
                                       final String fullDisplayName, final long maxMessageSize) {
        super(logger);
        this.writer = writer;
        this.fullDisplayName = fullDisplayName;
        this.maxMessageSize = maxMessageSize;
    }

    @Override
    public void write(int i) throws IOException {
        super.write(i);

        if (this.isFull()) {

            // add newline and flush the message
            this.rawOutputStream.write(LF);
            writeLine();

            // carry on with the now-reset buffer
            this.rawOutputStream.write(i);
        }
        else {
            this.rawOutputStream.write(i);
            if (i == LF) {
                writeLine();
            }
        }
    }

    private boolean isFull() {

        if (this.maxMessageSize == TcpLogForwarderConfiguration.UNLIMITED_MESSAGE_SIZE) {
            return false;
        }

        final long maxBufferSize = this.getMaxBufferSize();

        if (maxBufferSize < 1) {
            throw new TcpLogforwarderException("Calculated message size is less than 1.  Is your 'Maximum Message Size' set too low?");
        }

        return (this.rawOutputStream.size() == maxBufferSize);
    }

    @VisibleForTesting
    private long getMaxBufferSize() {
        // The prefix counts towards the max message length
        // Also subtract 1 for the newline
        return this.maxMessageSize - this.getPrefix().length() - 1;
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.writer.flush();
        this.rawOutputStream.flush();
        this.textOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.rawOutputStream.close();
        this.textOutputStream.close();
        this.writer.close(); // closes socket as well
    }

    private void writeLine() throws IOException {
        this.textOutputStream.write(this.getPrefix().getBytes(Charset.forName("UTF-8")));
        decodeConsoleBase64Text(this.rawOutputStream.getBuffer(), this.rawOutputStream.size(), this.textOutputStream);

        // Send the log
        this.writer.write(textOutputStream.toString());
        this.writer.flush();

        // re-use
        this.rawOutputStream.reset();
        this.textOutputStream.reset();
    }

    private String getPrefix() {
        return this.fullDisplayName + SEPARATOR;
    }

    /**
     *  Blatantly stolen from: https://github.com/jenkinsci/splunk-devops-plugin
     */
    private void decodeConsoleBase64Text(byte[] in, int length, ByteArrayOutputStream2 out) {
        int next = ConsoleNote.findPreamble(in, 0, length);

        // perform byte[]->char[] while figuring out the char positions of the BLOBs
        int written = 0;
        while (next >= 0) {
            if (next > written) {
                out.write(in, written, next - written);
                written = next;
            } else {
                assert next == written;
            }

            int rest = length - next;
            ByteArrayInputStream b = new ByteArrayInputStream(in, next, rest);

            try {
                ConsoleNote.skip(new DataInputStream(b));
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "failed to filter blob", e);
            }

            int bytesUsed = rest - b.available(); // bytes consumed by annotations
            written += bytesUsed;

            next = ConsoleNote.findPreamble(in, written, length - written);
        }
        // finish the remaining bytes->chars conversion
        out.write(in, written, length - written);
    }
}
