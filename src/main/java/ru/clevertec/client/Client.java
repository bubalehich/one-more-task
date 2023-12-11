package ru.clevertec.client;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.clevertec.data.Request;
import ru.clevertec.data.Response;
import ru.clevertec.exception.ClientException;
import ru.clevertec.server.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
public class Client {
    private final List<Integer> data;
    private final int dataCount;
    private final ExecutorService executor;
    private final Server server;
    private final AtomicInteger accumulator;

    public Client(Server server, int dataCount) {
        this.dataCount = dataCount;
        data = new ArrayList<>();
        populateData();
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.server = server;
        accumulator = new AtomicInteger(0);
    }

    public List<Integer> getData() {
        return data;
    }

    public int getAccumulatorValue() {
        return accumulator.get();
    }

    public void doSend() {
        List<Callable<Response>> tasks = new ArrayList<>();

        IntStream.range(0, dataCount).forEachOrdered(i -> {
            Collections.shuffle(data);
            var value = data.remove(0);
            var request = new Request(value);
            Callable<Response> response = () -> server.process(request);
            tasks.add(response);
        });

        log.info(String.format("Tasks have been created. Count: %d", tasks.size()));

        List<Future<Response>> futures;
        try {
            futures = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            log.error("CLIENT: Error while invoking tasks. Reason:", e);
            Thread.currentThread().interrupt();
            throw new ClientException(e);
        }

        futures.parallelStream()
                .forEach(this::incrementAccumulator);

        destroy();
    }

    private void incrementAccumulator(@NotNull Future<Response> future) {
        try {
            accumulator.getAndAdd(future.get().message());
            log.info(String.format("CLIENT: Response received: %d", future.get().message()));
        } catch (InterruptedException | ExecutionException e) {
            log.error(String.format("CLIENT: Error while receiving response: %s.", future), e);
            Thread.currentThread().interrupt();
            throw new ClientException(e);
        }
    }

    private void destroy() {
        var notFinishedTasks = executor.shutdownNow();
        if (!notFinishedTasks.isEmpty()) {
            log.info(String.format("Completed with not finished tasks. Count: %s.", notFinishedTasks.size()));
        } else {
            log.warn("Shutdown executor service. Completed without not finished tasks.");
        }
    }

    private void populateData() {
        IntStream.range(0, dataCount).forEachOrdered(data::add);
    }
}
