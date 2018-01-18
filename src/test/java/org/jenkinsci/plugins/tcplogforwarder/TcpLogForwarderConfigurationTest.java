package org.jenkinsci.plugins.tcplogforwarder;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

public class TcpLogForwarderConfigurationTest {

    @Rule
    public RestartableJenkinsRule rr = new RestartableJenkinsRule();

    /**
     * Tries to exercise enough code paths to catch common mistakes:
     * <ul>
     * <li>missing {@code load}
     * <li>missing {@code save}
     * <li>misnamed or absent getter/setter
     * <li>misnamed {@code textbox}
     * </ul>
     */
    @Test
    @Ignore
    public void uiAndStorage() {
        rr.then(r -> {
            assertNull("not set initially", TcpLogForwarderConfiguration.get().getHost());
            HtmlForm config = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlTextInput textbox = config.getInputByName("_.host");
            textbox.setText("hello");
            r.submit(config);
            assertEquals("global config page let us edit it", "hello", TcpLogForwarderConfiguration.get().getHost());
        });
        rr.then(r -> {
            assertEquals("still there after restart of Jenkins", "hello", TcpLogForwarderConfiguration.get().getHost());
        });
    }

}
