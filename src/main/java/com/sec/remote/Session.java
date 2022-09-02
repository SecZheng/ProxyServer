package com.sec.remote;

import io.netty.channel.Channel;

public class Session {
    Channel client;
    int remoteIndex;

    public Session(Channel client, int remoteIndex) {
        this.client = client;
        this.remoteIndex = remoteIndex;
    }

    public Channel getClient() {
        return client;
    }

    public int getRemoteIndex() {
        return remoteIndex;
    }
}
