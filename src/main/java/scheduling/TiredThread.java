package scheduling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TiredThread extends Thread implements Comparable<TiredThread> {

    private static final Runnable POISON_PILL = () -> {
    }; // Special task to signal shutdown

    private final int id; // Worker index assigned by the executor
    private final double fatigueFactor; // Multiplier for fatigue calculation

    private final AtomicBoolean alive = new AtomicBoolean(true); // Indicates if the worker should keep running

    // Single-slot handoff queue; executor will put tasks here
    private final BlockingQueue<Runnable> handoff = new ArrayBlockingQueue<>(1);

    private final AtomicBoolean busy = new AtomicBoolean(false); // Indicates if the worker is currently executing a
                                                                 // task

    private final AtomicLong timeUsed = new AtomicLong(0); // Total time spent executing tasks
    private final AtomicLong timeIdle = new AtomicLong(0); // Total time spent idle
    private final AtomicLong idleStartTime = new AtomicLong(0); // Timestamp when the worker became idle

    public TiredThread(int id, double fatigueFactor) {
        this.id = id;
        this.fatigueFactor = fatigueFactor;
        this.idleStartTime.set(System.nanoTime());
        setName(String.format("FF=%.2f", fatigueFactor));
    }

    public int getWorkerId() {
        return id;
    }

    public double getFatigue() {
        return fatigueFactor * timeUsed.get();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public long getTimeUsed() {
        return timeUsed.get();
    }

    public long getTimeIdle() {
        return timeIdle.get();
    }

    /**
     * Assign a task to this worker.
     * This method is non-blocking: if the worker is not ready to accept a task,
     * it throws IllegalStateException.
     */
    public void newTask(Runnable task) {
        if (!alive.get()) {
            throw new IllegalStateException("Worker is shut down");
        }

        if (!handoff.offer(task)) {
            throw new IllegalStateException("Worker is not ready to accept a task");
        }

    }

    /**
     * Request this worker to stop after finishing current task.
     * Inserts a poison pill so the worker wakes up and exits.
     */
    public void shutdown() {
        alive.set(false);
        try {
            handoff.offer(POISON_PILL);
        } catch (Exception e) {
            System.out.println("Failed to send shutdown signal to worker " + id);
        }
    }

    @Override
    // שליפת משימה מהתור וביצועה
    public void run() {
        while (alive.get()) {
            try {
                Runnable task = handoff.take();
                if (task == POISON_PILL) {
                    break;
                }
                // ברגע שהגענו לשורה הזו קיבלנו משימה וההמתנה (הבטלה) נגמרה
                long currentTime = System.nanoTime();
                // כמה זמן התבטלנו עד עכשיו
                long idleDuration = currentTime - idleStartTime.get();
                timeIdle.addAndGet(idleDuration);

                // המשימה אמיתית תחילת זמן עבודה
                busy.set(true);
                long startTime = System.nanoTime();
                try {
                    // ננסה להריץ את המשימה, אם היא לא חוקית נתפוס את השגיאה ונמשיך הלאה
                    task.run();
                } catch (Exception e) {
                    System.err.println("!!! TASK FAILED !!!");
                    System.err.println("Error message: " + e.getMessage());
                    e.printStackTrace(); 
                }
                long endTime = System.nanoTime();
                long taskDuration = endTime - startTime;
                timeUsed.addAndGet(taskDuration);
                busy.set(false);

                // Mark the start of the next idle period
                idleStartTime.set(System.nanoTime());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

    }

    @Override
    public int compareTo(TiredThread o) {
        if (o.getFatigue() < this.getFatigue()) {
            return 1;
        } else if (o.getFatigue() == this.getFatigue()) {
            return 0;
        } else {
            return -1;
        }
    }

    // פונקציית עזר
    public void setTimeUsed(long timeUsed) {
        this.timeUsed.set(timeUsed);
    }
    public double getFactor(){
        return fatigueFactor;
    }
}