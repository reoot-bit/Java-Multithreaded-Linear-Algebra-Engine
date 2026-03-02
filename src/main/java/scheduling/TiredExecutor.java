package scheduling;

import java.util.concurrent.PriorityBlockingQueue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    // יצירת תרדים והכנסתם לבריכה
    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads];
        Random random = new Random();
        for (int i = 0; i < numThreads; i++) {
            int id = i;
            double fatigueFactor = random.nextDouble() + 0.5;
            workers[i] = new TiredThread(id, fatigueFactor);
            workers[i].start();
            idleMinHeap.add(workers[i]);
        }
    }

    public void submit(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        TiredThread worker;
            try{
                worker = idleMinHeap.take();
                inFlight.incrementAndGet();
            } catch (InterruptedException e) {
                inFlight.decrementAndGet();
                throw new RuntimeException("Executor interrupted while waiting for an idle worker", e);
            }
        Runnable wrappedTask = () -> {
            int remainingTasks;
            try {
                task.run();
            } finally {
                remainingTasks = inFlight.decrementAndGet();
                    idleMinHeap.offer(worker);
                }

                // המשימה האחרונה התבצעה, נודיע לכל הממתינים
                if (remainingTasks == 0) {
                    synchronized (inFlight) {
                        inFlight.notifyAll();
                    }
                }
            

        };
        worker.newTask(wrappedTask);
    }

    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable runnable : tasks) {
            submit(runnable);
        }
        // המתנה לסיום כל המשימות
        synchronized (inFlight) {
            while (inFlight.get() > 0) {
                try {
                    inFlight.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        for (TiredThread worker : workers) {
            worker.shutdown();
        }
        for (TiredThread worker : workers) {
            try {
                worker.join(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // שחזור סטטוס הפרעה במידת הצורך
            }
        }
        System.out.println("Executor has shut down cleanly.");
    }

   public synchronized String getWorkerReport() {
        String report = "=== Executor Report ===\n";
        double avgFatigue = 0.0;
        double fairness = 0.0;

        for (TiredThread worker : workers) {
            report += "Worker ID: " + worker.getWorkerId() + "\n";
            report += "Fatigue: " + worker.getFatigue() + "\n";
            report += "Time Used: " + worker.getTimeUsed() + "\n";
            report += "Time Idle: " + worker.getTimeIdle() + "\n";
            report += "\n";
            
            avgFatigue += worker.getFatigue();
        }

        avgFatigue /= workers.length;

        for (TiredThread worker : workers) {
            fairness += Math.pow(worker.getFatigue() - avgFatigue, 2);
        }

        report += "FAIRNESS VALUE: " + fairness + "\n";
        report += "Total tasks in flight: " + inFlight.get() + "\n";

        return report;
    }
}
