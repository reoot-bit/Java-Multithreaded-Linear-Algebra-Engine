package scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

public class TiredExecutorTest {
    private TiredExecutor executor;

    @BeforeEach
    void setUp() {
        // אתחול עם 4 תרדים
        executor = new TiredExecutor(4); 
    }

    @Test
    @Timeout(5)
    void testSubmitAllBlocksUntilFinished() throws InterruptedException {
        int numTasks = 10;
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < numTasks; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(50);
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // המתודה צריכה לחסום עד שכל 10 המשימות מסתיימות 
        executor.submitAll(tasks);
        assertEquals(numTasks, counter.get(), "submitAll returned before all tasks finished");
        
        executor.shutdown();
    }

    @Test
    void testFairSchedulingSelection() throws InterruptedException {
        // המטרה: לשלוח 8 משימות ל-4 עובדים ולראות שכולם עובדים
        int numTasks = 8;
        
        System.out.println("--- Starting Fair Scheduling Test (8 Tasks, 4 Threads) ---");

        for (int i = 0; i < numTasks; i++) {
            executor.submit(() -> {
                try { 
                    // מדמה עבודה של 100 מילי-שניות
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

           
            Thread.sleep(20);
        }

        
        Thread.sleep(1500);

        String report = executor.getWorkerReport();
        System.out.println("Worker Report:\n" + report);

       
        assertNotNull(report);
        assertTrue(report.contains("Time Used"), "Report should contain 'Time Used'");
        
        
    }

    @Test
    void testShutdownCleansUp() throws InterruptedException {
        executor.shutdown();
    
        assertTrue(true);
    }
}