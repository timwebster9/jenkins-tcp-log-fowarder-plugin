package org.jenkinsci.plugins.tcplogforwarder;

import hudson.console.ConsoleNote;
import hudson.util.ByteArrayOutputStream2;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ForwarderFilterOutputStream extends FilterOutputStream {

    private static final int LF = 0x0A;

    private ByteArrayOutputStream2 rawOutputStream = new ByteArrayOutputStream2(512);
    private ByteArrayOutputStream2 textOutputStream = new ByteArrayOutputStream2(512);
    private String fullDisplayName;

    /**
     * Writes the console log to both the original logger OutputStream as well as the remote TCP socket.
     *
     * @param logger
     *      the original console logger OutputStream
     * @param fullDisplayName
     *      the Job name/number.  Included in the TCP log for easy grepping.
     *
     * @throws IOException
     */
    public ForwarderFilterOutputStream(OutputStream logger, String fullDisplayName) {
        super(logger);
        this.fullDisplayName = fullDisplayName;
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
        this.rawOutputStream.close();
        this.textOutputStream.close();
    }

    private void writeLine() throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.fullDisplayName).append(" - ");

        this.textOutputStream.write(stringBuilder.toString().getBytes());
        decodeConsoleBase64Text(this.rawOutputStream.getBuffer(), this.rawOutputStream.size(), this.textOutputStream);

        // Send the log
        SocketWriter.write(textOutputStream.toString());

        // re-use
        this.rawOutputStream.reset();
        this.textOutputStream.reset();
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
            } catch (IOException ex) {
                //Logger.getLogger(LogEventHelper.class.getName()).log(Level.SEVERE, "failed to filter blob", ex);
            }

            int bytesUsed = rest - b.available(); // bytes consumed by annotations
            written += bytesUsed;

            next = ConsoleNote.findPreamble(in, written, length - written);
        }
        // finish the remaining bytes->chars conversion
        out.write(in, written, length - written);
    }
}
