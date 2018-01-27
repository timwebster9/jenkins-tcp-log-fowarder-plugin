package org.jenkinsci.plugins.tcplogforwarder;

import com.google.common.annotations.VisibleForTesting;
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

    static final long UNLIMITED_MESSAGE_SIZE = -1L;

    public static TcpLogForwarderConfiguration get() {
        return GlobalConfiguration.all().get(TcpLogForwarderConfiguration.class);
    }

    // Form values
    private String host;
    private String port;
    private String maxMessageSize;

    private boolean enabled;

    public TcpLogForwarderConfiguration() {
        load();
    }

    @VisibleForTesting
    public TcpLogForwarderConfiguration(final boolean test) {}

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

    public String getMaxMessageSize() {
        return maxMessageSize;
    }

    @DataBoundSetter
    public void setMaxMessageSize(String maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        save();
    }

    public long getMaxMessageValue() {
        if (StringUtils.isNotEmpty(this.maxMessageSize)) {
            return Long.parseLong(this.maxMessageSize);
        }
        return UNLIMITED_MESSAGE_SIZE;
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
        if (isNotInteger(value)) {
            return FormValidation.warning("Please specify an integer.");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckMaxMessageSize(@QueryParameter String value) {
        if (StringUtils.isNotEmpty(value)) {
            if (isNotInteger(value)) {
                return FormValidation.warning("Please specify a long value.");
            }
        }
        return FormValidation.ok();
    }

    private static boolean isNotLong(final String value) {
        try {
            Long.parseLong(value);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static boolean isNotInteger(final String value) {
        try {
            Integer.parseInt(value);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

}
