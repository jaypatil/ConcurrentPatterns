package org.iplusplus.barriers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class BarrierInAction {

    public static void main(String[] args) {

        class Friend implements Callable<String> {

            private CyclicBarrier barrier;

            public Friend(CyclicBarrier barrier) {
                this.barrier = barrier;
            }

            public String call() throws Exception {
                Random random = new Random();
                Thread.sleep((random.nextInt(20) * 100 + 100));
                System.out.println("I just arrived, waiting for the others...");

                barrier.await();

                System.out.println("Let's go to the cinema!");
                return "ok";
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        CyclicBarrier barrier = new CyclicBarrier(4, () -> System.out.println("Barrier openning"));
        List<Future<String>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 4; i++) {
                Friend friend = new Friend(barrier);
                futures.add(executorService.submit(friend));
            }

            futures.forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e.getMessage());
                }
            });

        } finally {
            executorService.shutdown();
        }
    }
}
