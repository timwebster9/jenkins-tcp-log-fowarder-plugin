package org.jenkinsci.plugins.tcplogforwarder;

public class TcpLogforwarderException extends RuntimeException {

    public TcpLogforwarderException(Throwable throwable) {
        super(throwable);
    }

    public TcpLogforwarderException(String s) {
        super(s);
    }
}
