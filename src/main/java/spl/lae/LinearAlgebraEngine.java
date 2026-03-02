package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;
import java.util.ArrayList;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        try{// סידור העץ מחדש של הצמתים בצורה אסוציאטיבית
        computationRoot.associativeNesting();
        // תנאי העצירה- השורש הוא מטריצה
        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX) {
            // מוצאים את הצומת הבא שמוכן לחישוב (זה שהילדים שלו הם מטריצות)
            ComputationNode nodeToCompute = computationRoot.findResolvable();
            loadAndCompute(nodeToCompute);
        }

        return computationRoot;
    }
        finally {
            try{
            executor.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // מניחים שהעץ בינארי, שהצומת הוא "מוכן" לחישוב ולא עלה. טוענים את המטריצות
    // הנדרשות לזיכרון ומחשבים את הצומת הנתון
    public void loadAndCompute(ComputationNode node) {
        double[][] M1 = node.getChildren().get(0).getMatrix();
        leftMatrix.loadRowMajor(M1);
        if (node.getChildren().size() == 2) {
            double[][] M2 = node.getChildren().get(1).getMatrix();
            // המטריצות לפי שורות
            rightMatrix.loadRowMajor(M2);
        }
        List<Runnable> tasks = new ArrayList<>();
        if (node.getNodeType() == ComputationNodeType.ADD) {
            if (leftMatrix.length() != rightMatrix.length()
                    || leftMatrix.get(0).length() != rightMatrix.get(0).length()) {
                throw new IllegalArgumentException("Matrix addition failure: Dimensions mismatch");
            }
            tasks = createAddTasks();
        }

        if (node.getNodeType() == ComputationNodeType.MULTIPLY) {
            if (leftMatrix.get(0).length() != rightMatrix.length()) {
                throw new IllegalArgumentException("Matrix multiplying failure: Dimensions mismatch");
            }
            tasks = createMultiplyTasks();
        }

        if (node.getNodeType() == ComputationNodeType.NEGATE) {
            tasks = createNegateTasks();
        }

        if (node.getNodeType() == ComputationNodeType.TRANSPOSE) {
            double[][] currentData = leftMatrix.readRowMajor();
            leftMatrix.loadColumnMajor(currentData);
            tasks = createTransposeTasks();
        }
        executor.submitAll(tasks);
        node.resolve(leftMatrix.readRowMajor());
    }

    public List<Runnable> createAddTasks() {
        List<Runnable> toSubmit = new ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            int rowIndex = i;
            Runnable task = () -> {
                leftMatrix.get(rowIndex).add(rightMatrix.get(rowIndex));
            };
            toSubmit.add(task);
        }
        return toSubmit;

    }

    public List<Runnable> createMultiplyTasks() {
        List<Runnable> toSubmit = new ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            int rowIndex = i;
            Runnable task = () -> {
                leftMatrix.get(rowIndex).vecMatMul(rightMatrix);
            };
            toSubmit.add(task);
        }
        return toSubmit;
    }

    public List<Runnable> createNegateTasks() {
        List<Runnable> toSubmit = new ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            int rowIndex = i;
            Runnable task = () -> {
                leftMatrix.get(rowIndex).negate();
            };
            toSubmit.add(task);
        }
        return toSubmit;
    }

    public List<Runnable> createTransposeTasks() {
        List<Runnable> toSubmit = new ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            int rowIndex = i;
            Runnable task = () -> {
                leftMatrix.get(rowIndex).transpose();
            };
            toSubmit.add(task);
        }
        return toSubmit;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }
}
