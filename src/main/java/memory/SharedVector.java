package memory;

import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector array cannot be null");
        }
        if (orientation == null) {
            throw new IllegalArgumentException("Orientation cannot be null");
        }
        this.vector = Arrays.copyOf(vector, vector.length);
        this.orientation = orientation;
    }

    public double get(int index) {
        if (index < 0 || index >= vector.length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        readLock();
        double value = vector[index];
        readUnlock();
        return value;
    }

    public int length() {
        readLock();
        int value = vector.length;
        readUnlock();
        return value;
    }

    public VectorOrientation getOrientation() {
        readLock();
        VectorOrientation orien = orientation;
        readUnlock();
        return orien;
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        writeLock();
        if (orientation == VectorOrientation.ROW_MAJOR) {
            orientation = VectorOrientation.COLUMN_MAJOR;
        } else {
            orientation = VectorOrientation.ROW_MAJOR;
        }
        writeUnlock();
    }

    public void add(SharedVector other) {
        if (other == null) {
            throw new IllegalArgumentException("Vector array cannot be null");
        }
        writeLock();
        try {
            if (other.length() != length()) {
                throw new IllegalArgumentException("Vectors must be of the same length to add");
            }
            for (int i = 0; i < vector.length; i++) {
                vector[i] = vector[i] + other.get(i);
            }
        } finally {
            writeUnlock();
        }
    }

    public void negate() {
        writeLock();
        try {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = -vector[i];
            }
        } finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        readLock();
        try {
            if (other == null) {
                throw new IllegalArgumentException("Other vector cannot be null");
            }
            if (other.length() != this.length()) {
                throw new IllegalArgumentException("the dot is not defiend");
            }
            double res = 0.0;
            for (int i = 0; i < vector.length; i++) {
                res += this.vector[i] * other.get(i);
            }
            return res;
        } finally {
            readUnlock();
        }

    }

    public void vecMatMul(SharedMatrix matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        if (this.getOrientation() != VectorOrientation.ROW_MAJOR) {
            throw new IllegalArgumentException("Vector must be ROW_MAJOR for multiplication");
        }
        writeLock();
        try {
            if (matrix.length() == 0) {
                this.vector = new double[0];
                return;
            }
            if (this.length() != matrix.get(0).length()) {
                throw new IllegalArgumentException("Vector length must match matrix row count");
            }
            int resultLength;
            double[] result;

            // --- מקרה א: המטריצה מסודרת בעמודות (הכי יעיל) ---
            if (matrix.getOrientation() == VectorOrientation.COLUMN_MAJOR) {
                // במטריצת עמודות, מספר השורות הוא אורך העמודה הראשונה
                if (this.length() != matrix.get(0).length()) {
                    throw new IllegalArgumentException("Vector length must match matrix row count");
                }

                resultLength = matrix.length(); // מספר העמודות
                result = new double[resultLength];

                for (int i = 0; i < resultLength; i++) {
                    result[i] = this.dot(matrix.get(i));
                }
            }
            // --- מקרה ב: המטריצה מסודרת בשורות (מה שה-Engine שולח) ---
            else {
                // במטריצת שורות, מספר השורות הוא ה-length של המטריצה עצמה
                if (this.length() != matrix.length()) {
                    throw new IllegalArgumentException("Vector length must match matrix row count");
                }

                // מספר העמודות הוא האורך של השורה הראשונה
                resultLength = matrix.get(0).length();
                result = new double[resultLength];

                // חישוב ידני: סכום משוקלל של השורות
                for (int i = 0; i < matrix.length(); i++) {
                    double scalar = this.vector[i];
                    SharedVector row = matrix.get(i);
                    for (int j = 0; j < resultLength; j++) {
                        result[j] += scalar * row.get(j);
                    }
                }
            }

            // עדכון הנתונים בסוף
            this.vector = result;

        } finally {
            writeUnlock();
        }
    }
}
