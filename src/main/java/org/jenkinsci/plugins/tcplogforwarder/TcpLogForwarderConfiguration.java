package org.jenkinsci.plugins.tcplogforwarder;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

@Extension
public class TcpLogForwarderConfiguration extends GlobalConfiguration {

    public static TcpLogForwarderConfiguration get() {
        return GlobalConfiguration.all().get(TcpLogForwarderConfiguration.class);
    }

    private String host;
    private String port;
    private boolean enabled;
    private Socket socket;

    public TcpLogForwarderConfiguration() {
        load();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @DataBoundSetter
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public String getHost() {
        return this.host;
    }

    @DataBoundSetter
    public void setHost(String host) {
        this.host = host;
        save();
    }

    public String getPort() {
        return this.port;
    }

    @DataBoundSetter
    public void setPort(String port) {
        this.port = port;
        save();
    }

    public Socket getSocket() {
        if (this.socket == null || this.socket.isClosed()) {
            try {
                this.socket = new Socket(this.host, Integer.parseInt(this.port));
            } catch (IOException e) {
                throw new TcpLogforwarderException(e);
            }
        }
        return this.socket;
    }

    public void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new TcpLogforwarderException(e);
        }
    }

    public FormValidation doCheckHost(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a host.");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckPort(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a port.");
        }
        return FormValidation.ok();
    }

}
