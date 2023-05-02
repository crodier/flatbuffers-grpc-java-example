package com.example.fbgrpc;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityStrategy;

import java.util.concurrent.ThreadFactory;

public class PinnedCoreAffinityThreadFactory implements ThreadFactory {

    private final int[] cpus;
    String name = "pinned_";
    private final int core;

    public PinnedCoreAffinityThreadFactory(String name, int core) {
        this.name = name;
        this.core = core;
        this.cpus = new int[]{core};
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(() -> {
            try (AffinityLock lock = AffinityLock.acquireLock(cpus)) {
                r.run();
            }
        }, name);
        t.setDaemon(true);
        return t;
    }
}
