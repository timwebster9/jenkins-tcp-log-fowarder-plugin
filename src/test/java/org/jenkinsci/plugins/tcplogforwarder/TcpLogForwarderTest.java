package org.jenkinsci.plugins.tcplogforwarder;

import hudson.model.Run;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TcpLogForwarderTest {

    private static final String FULL_DISPLAY_NAME = "Job #45";
    private static final String HOST = "localhost";
    private static final String PORT = "666";

    @Mock
    private TcpLogForwarderConfiguration config;

    @Mock
    private Socket socket;

    @Mock
    private OutputStream logger;

    @Mock
    private OutputStream socketOutputStream;

    @Mock
    private Run build;

    @Spy
    @InjectMocks
    private TcpLogForwarder freestyleTestSubject = new TcpLogForwarder(null);;

    @Spy
    @InjectMocks
    private TcpLogForwarder testSubject = new TcpLogForwarder(FULL_DISPLAY_NAME);

    @Test
    public void original_logger_outputstream_returned_when_plugin_disabled() throws IOException {
        when(this.config.isEnabled()).thenReturn(false);
        doReturn(this.config).when(this.testSubject).getConfig();

        final OutputStream result = this.testSubject.decorateLogger(this.build, this.logger);
        assertThat(result).isEqualTo(this.logger);
    }

    @Test
    public void plugin_enabled_for_pipeline() throws IOException {

        doReturn(this.config).when(this.testSubject).getConfig();
        doReturn(this.socket).when(this.testSubject).getSocket(HOST, Integer.parseInt(PORT));

        when(this.config.isEnabled()).thenReturn(true);
        when(this.config.getHost()).thenReturn(HOST);
        when(this.config.getPort()).thenReturn(PORT);
        when(this.socket.getOutputStream()).thenReturn(this.socketOutputStream);

        final OutputStream result = this.testSubject.decorateLogger(this.build, this.logger);
        assertThat(result).isInstanceOf(ForwarderFilterOutputStream.class);
    }

    @Test
    public void plugin_enabled_for_freestyle() throws IOException {

        doReturn(this.config).when(this.freestyleTestSubject).getConfig();
        doReturn(this.socket).when(this.freestyleTestSubject).getSocket(HOST, Integer.parseInt(PORT));

        when(this.config.isEnabled()).thenReturn(true);
        when(this.config.getHost()).thenReturn(HOST);
        when(this.config.getPort()).thenReturn(PORT);
        when(this.socket.getOutputStream()).thenReturn(this.socketOutputStream);
        when(this.build.getFullDisplayName()).thenReturn(FULL_DISPLAY_NAME);

        final OutputStream result = this.freestyleTestSubject.decorateLogger(this.build, this.logger);
        assertThat(result).isInstanceOf(ForwarderFilterOutputStream.class);
    }
}