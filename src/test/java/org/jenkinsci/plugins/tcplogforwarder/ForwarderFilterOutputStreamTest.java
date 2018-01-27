package org.jenkinsci.plugins.tcplogforwarder;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class ForwarderFilterOutputStreamTest {

    private static final String FULL_DISPLAY_NAME = "test-build #7";
    private static final int A = 0x41;  // the letter 'A'

    private ForwarderFilterOutputStream testSubject;
    private BufferedWriter writer;

    // This is the thing we actually write to and can assert against
    private ByteArrayOutputStream outputStream;

    @Before
    public void setup() {
        this.outputStream = new ByteArrayOutputStream();
        this.writer = new BufferedWriter(new OutputStreamWriter(this.outputStream));
    }

    @Test
    public void write_message_exactly_of_maximum_size() throws IOException {
        this.testSubject = new ForwarderFilterOutputStream(this.writer, new ByteArrayOutputStream(),
            FULL_DISPLAY_NAME, 20);

        for (int i = 0; i < 3; i++) {
            this.testSubject.write(A);
        }
        this.testSubject.write(ForwarderFilterOutputStream.LF);

        final String expected = FULL_DISPLAY_NAME + ForwarderFilterOutputStream.SEPARATOR + "AAA\n";
        assertThat(this.outputStream.toString()).isEqualTo(expected);
    }

    @Test
    public void write_message_under_maximum_size() throws IOException {
        this.testSubject = new ForwarderFilterOutputStream(this.writer, new ByteArrayOutputStream(),
            FULL_DISPLAY_NAME, 20);

        for (int i = 0; i < 1; i++) {
            this.testSubject.write(A);
        }
        this.testSubject.write(ForwarderFilterOutputStream.LF);

        final String expected = FULL_DISPLAY_NAME + ForwarderFilterOutputStream.SEPARATOR + "A\n";
        assertThat(this.outputStream.toString()).isEqualTo(expected);
    }

    @Test
    public void write_message_with_no_maximum_size() throws IOException {
        this.testSubject = new ForwarderFilterOutputStream(this.writer, new ByteArrayOutputStream(),
            FULL_DISPLAY_NAME, TcpLogForwarderConfiguration.UNLIMITED_MESSAGE_SIZE);

        for (int i = 0; i < 50; i++) {
            this.testSubject.write(A);
        }
        this.testSubject.write(ForwarderFilterOutputStream.LF);

        final String expected = FULL_DISPLAY_NAME + ForwarderFilterOutputStream.SEPARATOR +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n";
        assertThat(this.outputStream.toString()).isEqualTo(expected);
    }

    @Test
    public void assert_message_over_maximum_size_is_split() throws IOException {
        this.testSubject = new ForwarderFilterOutputStream(this.writer, new ByteArrayOutputStream(),
            FULL_DISPLAY_NAME, 20);

        for (int i = 0; i < 10; i++) {
            this.testSubject.write(A);
        }
        this.testSubject.write(ForwarderFilterOutputStream.LF);

        final String expected = FULL_DISPLAY_NAME + ForwarderFilterOutputStream.SEPARATOR + "AAA\n" +
                                FULL_DISPLAY_NAME + ForwarderFilterOutputStream.SEPARATOR + "AAA\n" +
                                FULL_DISPLAY_NAME + ForwarderFilterOutputStream.SEPARATOR + "AAA\n" +
                                FULL_DISPLAY_NAME + ForwarderFilterOutputStream.SEPARATOR + "A\n";
        assertThat(this.outputStream.toString()).isEqualTo(expected);
    }

    @Test(expected = TcpLogforwarderException.class)
    public void assert_exception_when_max_message_size_too_small() throws Exception {
        this.testSubject = new ForwarderFilterOutputStream(this.writer, new ByteArrayOutputStream(),
            FULL_DISPLAY_NAME, 5);

        for (int i = 0; i < 6; i++) {
            this.testSubject.write(A);
        }
        this.testSubject.write(ForwarderFilterOutputStream.LF);
    }
}