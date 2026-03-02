package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
        vectors = new SharedVector[0];
        }
        loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Input matrix cannot be null");
        }
        if (matrix.length == 0) {
            vectors = new SharedVector[0];
        }
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("Input matrix rows cannot be null");
            }
            if (matrix[i].length != matrix[i - 1].length) {
                throw new IllegalArgumentException("All rows must have the same length");
            }
        }
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            double[] row = new double[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                row[j] = matrix[i][j];
            }
            SharedVector rowI = new SharedVector(row, VectorOrientation.ROW_MAJOR);
            newVectors[i] = rowI;
        }
        SharedVector[] oldVectors = this.vectors;
        acquireAllVectorWriteLocks(oldVectors);
        try {
            this.vectors = newVectors;
        } finally {
            releaseAllVectorWriteLocks(oldVectors);
        }
    }

    public void loadColumnMajor(double[][] matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("matrix can't be null");
        }
        if (matrix.length == 0) {
            vectors = new SharedVector[0];
        } else {
            if (matrix[0] == null)
                throw new IllegalArgumentException("Row 0 cannot be null");
            int numOfColumns = matrix[0].length;
            int numOfRows = matrix.length;
            SharedVector[] cols = new SharedVector[numOfColumns];
            for (int i = 0; i < numOfColumns; i++) {
                double[] colVector = new double[numOfRows];
                for (int j = 0; j < numOfRows; j++) {
                    if (matrix[j] == null) {
                        throw new IllegalArgumentException("Input matrix rows cannot be null");
                    }
                    if (matrix[j].length != numOfColumns) {
                        throw new IllegalArgumentException("All rows must have the same number of columns");
                    }
                    colVector[j] = matrix[j][i];
                }
                cols[i] = new SharedVector(colVector, VectorOrientation.COLUMN_MAJOR);
            }
            SharedVector[] oldVectors = this.vectors;
            acquireAllVectorWriteLocks(oldVectors);
            try {
                vectors = cols;
            } finally {
                releaseAllVectorWriteLocks(oldVectors);
            }
        }
    }

    public double[][] readRowMajor() {
        if (vectors.length == 0)
            return new double[0][0];

        acquireAllVectorReadLocks(vectors);
        try {
            if (vectors[0].getOrientation() == VectorOrientation.ROW_MAJOR) {
                double[][] res = new double[vectors.length][vectors[0].length()];
                for (int i = 0; i < vectors.length; i++) {
                    for (int j = 0; j < vectors[i].length(); j++) {
                        res[i][j] = vectors[i].get(j);
                    }
                }
                return res;
            } else {
                int rows = vectors[0].length();
                int cols = vectors.length;
                double[][] res = new double[rows][cols];

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        res[i][j] = vectors[j].get(i);
                    }
                }
                return res;
            }
        } finally {
            releaseAllVectorReadLocks(vectors);
        }
    }

    public int length() {
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        if (vectors.length == 0) {
            throw new IllegalArgumentException("Matrix is empty");
        }

        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        if (vecs != null) {
            for (int i = 0; i < vecs.length; i++) {
                vecs[i].readLock();
            }
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        if (vecs != null) {
            for (int i = 0; i < vecs.length; i++) {
                vecs[i].readUnlock();
            }
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        for (int i = 0; i < vecs.length; i++) {
            vecs[i].writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        for (int i = 0; i < vecs.length; i++) {
            vecs[i].writeUnlock();
        }
    }

    public SharedVector get(int index) {
        if (index < 0 || index >= vectors.length) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        return vectors[index];
    }
}