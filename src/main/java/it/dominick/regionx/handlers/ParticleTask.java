package it.dominick.regionx.handlers;

import java.util.concurrent.CompletableFuture;

public class ParticleTask {
    boolean isCancelled = false;
    CompletableFuture<?> task;

    public ParticleTask(CompletableFuture<?> task) {
        this.task = task;
    }

    public void setCancelled() {
        isCancelled = true;
    }
}
