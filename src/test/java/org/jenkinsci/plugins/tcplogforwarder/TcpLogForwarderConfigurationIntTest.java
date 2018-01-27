package org.jenkinsci.plugins.tcplogforwarder;

import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import static org.assertj.core.api.Assertions.assertThat;

public class TcpLogForwarderConfigurationIntTest {

    private static final String HOST = "localhost";
    private static final String PORT = "666";
    private static final String MAX_MESSAGE_SIZE = "65000";

    @Rule
    public RestartableJenkinsRule rr = new RestartableJenkinsRule();

    @Test
    public void uiAndStorage() {
        rr.then(r -> {

            // assert initial state
            assertThat(TcpLogForwarderConfiguration.get().isEnabled()).isFalse();
            assertThat(TcpLogForwarderConfiguration.get().getHost()).isNull();
            assertThat(TcpLogForwarderConfiguration.get().getPort()).isNull();
            assertThat(TcpLogForwarderConfiguration.get().getMaxMessageSize()).isNull();

            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");

            HtmlCheckBoxInput enabled = config.getInputByName("_.enabled");
            enabled.setChecked(true);

            HtmlTextInput host = config.getInputByName("_.host");
            host.setText(HOST);

            HtmlTextInput port = config.getInputByName("_.port");
            port.setText(PORT);


            HtmlTextInput maxMessageSize = config.getInputByName("_.maxMessageSize");
            maxMessageSize.setText(MAX_MESSAGE_SIZE);

            r.submit(config);
            assertThat(TcpLogForwarderConfiguration.get().isEnabled()).isTrue();
            assertThat(TcpLogForwarderConfiguration.get().getHost()).isEqualTo(HOST);
            assertThat(TcpLogForwarderConfiguration.get().getPort()).isEqualTo(PORT);
            assertThat(TcpLogForwarderConfiguration.get().getMaxMessageSize()).isEqualTo(MAX_MESSAGE_SIZE);
        });

        // assert persistence
        rr.then(r -> {
            assertThat(TcpLogForwarderConfiguration.get().isEnabled()).isTrue();
            assertThat(TcpLogForwarderConfiguration.get().getHost()).isEqualTo(HOST);
            assertThat(TcpLogForwarderConfiguration.get().getPort()).isEqualTo(PORT);
            assertThat(TcpLogForwarderConfiguration.get().getMaxMessageSize()).isEqualTo(MAX_MESSAGE_SIZE);
        });
    }
}
