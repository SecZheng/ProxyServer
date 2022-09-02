package com.sec.remote.util;

import com.sec.remote.Session;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelUtils {
    public static final int length = 18;
    public static AtomicInteger count = new AtomicInteger(0);
    private static PriorityQueue<Integer> idle = new PriorityQueue<>(length);

    //所有本地远程通道
    private static Channel[] channels = new Channel[length];
    //channelKey : [clientChannel,remoteIndex]
    private static HashMap<Integer, Session> used = new HashMap<>(24);

    static AtomicBoolean closed = new AtomicBoolean(true);

    static ReentrantLock lock = new ReentrantLock();
    static Condition wait = lock.newCondition();

    private ChannelUtils() {
    }

    public static Channel getRemoteById(int id, Channel client) {
        if (closed.get()) {
            return null;
        }
        if (used.containsKey(id)) {
            return channels[used.get(id).getRemoteIndex()];
        }
        Integer poll;
        try {
            lock.lock();
            while (idle.isEmpty()) {
                wait.await();
            }
            poll = idle.poll();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.unlock();
        }
        used.put(id, new Session(client, poll));
        return channels[poll];
    }

    public static Channel getClientById(int id) {
        if (closed.get()) {
            return null;
        }
        Session session = used.get(id);
        if (session == null) {
            return null;
        }
        return session.getClient();
    }

    public static boolean add(Channel remote) {
        if (closed.get()) {
            synchronized (closed) {
                if (closed.get()) {
                    idle.clear();
                    used.clear();
                    count.set(0);
                    for (int i = 0; i < length; i++) {
                        idle.add(i);
                    }
                    closed.set(false);
                }
            }
        }
        int index = count.getAndIncrement();
        if (index >= length) {
            return false;
        }
        channels[index] = remote;
        return true;
    }

    public static void remove(int id) {
        if (closed.get()) {
            return;
        }
        try {
            lock.lock();
            int remoteIndex = used.get(id).getRemoteIndex();
            used.remove(id);
            idle.add(remoteIndex);
            wait.signal();
        }finally {
            lock.unlock();
        }
    }

    public static void closed() {
        closed.set(true);
    }

}
