package org.jenkinsci.plugins.tcplogforwarder;

import hudson.console.ConsoleNote;
import hudson.util.ByteArrayOutputStream2;

import java.io.*;

public class ForwarderFilterOutputStream extends FilterOutputStream {

    private static final int LF = 0x0A;

    private BufferedWriter writer;
    private ByteArrayOutputStream2 rawOutputStream = new ByteArrayOutputStream2(512);
    private ByteArrayOutputStream2 textOutputStream = new ByteArrayOutputStream2(512);
    private String fullDisplayName;

    public ForwarderFilterOutputStream(final BufferedWriter writer, OutputStream logger, String fullDisplayName) {
        super(logger);
        this.writer = writer;
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
        final String prefix = this.fullDisplayName + " - ";
        this.textOutputStream.write(prefix.getBytes());
        decodeConsoleBase64Text(this.rawOutputStream.getBuffer(), this.rawOutputStream.size(), this.textOutputStream);

        // Send the log
        this.writer.write(textOutputStream.toString());
        this.writer.flush();
        //SocketWriter.write(textOutputStream.toString());

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
