
package vurst.visual.base.request;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import net.minecraft.MinecraftClient;
import vurst.visual.client.modules.api.Module;

public class RequestHandler<T> {
    private int currentTick = 0;
    private final PriorityBlockingQueue<Request<T>> activeRequests = new PriorityBlockingQueue<Request>(11, Comparator.comparingInt(r -> -r.priority));

    public void tick(int deltaTime) {
        this.currentTick += deltaTime;
    }

    public void tick() {
        this.tick(1);
    }

    public void request(Request<T> request) {
        this.activeRequests.removeIf(existing -> existing.provider == request.provider);
        request.expiresIn += this.currentTick;
        this.activeRequests.add(request);
    }

    public T getActiveRequestValue() {
        Request<T> top = this.activeRequests.peek();
        if (top == null) {
            return null;
        }
        if (MinecraftClient.getInstance().isOnThread()) {
            while (!(top == null || top.expiresIn > this.currentTick && top.provider.isEnabled())) {
                this.activeRequests.poll();
                top = this.activeRequests.peek();
            }
        }
        return top != null ? (T)top.value : null;
    }

    public static class Request<T> {
        public int expiresIn;
        public final int priority;
        public final Module provider;
        public final T value;

        public Request(int expiresIn, int priority, Module provider, T value) {
            this.expiresIn = expiresIn;
            this.priority = priority;
            this.provider = provider;
            this.value = value;
        }
    }
}

