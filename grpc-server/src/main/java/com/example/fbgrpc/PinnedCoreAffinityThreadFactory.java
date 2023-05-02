package com.example.fbgrpc;

import net.openhft.affinity.AffinityLock;

import java.util.concurrent.ThreadFactory;

public class PinnedCoreAffinityThreadFactory implements ThreadFactory {

    String name = "pinned_";
    private final int core;

    public PinnedCoreAffinityThreadFactory(String name, int core) {
        this.name = name;
        this.core = core;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(() -> {
            try (AffinityLock lock = AffinityLock.acquireLock(core)) {
                r.run();
            }
        }, name);
        t.setDaemon(true);
        return t;
    }
}
