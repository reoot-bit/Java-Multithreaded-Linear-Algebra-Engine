package memory;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class SharedMatrixTest {

    @Test
    @DisplayName("Constructor should throw exception for null input")
    void testConstructorNullCases() {
        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(null));
    }

    @Test
    @DisplayName("Constructor should handle empty input")
    void testConstructorEmptyCase() {
        SharedMatrix empty = new SharedMatrix(new double[0][0]);
        assertEquals(0, empty.length());
    }

    @Test
    @DisplayName("Constructor should throw exception for inconsistent row lengths")
    void testInconsistentRows() {
        double[][] data = {
                { 1, 2 },
                { 1, 2, 3 } // longer row
        };
        assertThrows(IllegalArgumentException.class, () -> new SharedMatrix(data));
    }

    // ------------test loading new data ------------

    // --- loadColumnMajor Tests ---

    @Test
    @DisplayName("loadColumnMajor: Standard transposition (Rows to Columns)")
    void testLoadColumnMajorSuccess() {
        SharedMatrix matrix = new SharedMatrix();
        // 2 rows, 3 columns matrix
        double[][] data = {
                { 1.0, 2.0, 3.0 },
                { 4.0, 5.0, 6.0 }
        };

        matrix.loadColumnMajor(data);

        // Verification: 3 columns means 3 internal vectors
        assertEquals(3, matrix.length(), "Matrix should have 3 vectors (one for each column)");
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());

        // Column 0 should be [1.0, 4.0]
        SharedVector col0 = matrix.get(0);
        assertEquals(2, col0.length());
        assertEquals(1.0, col0.get(0), 0.001);
        assertEquals(4.0, col0.get(1), 0.001);

        // Column 2 should be [3.0, 6.0]
        SharedVector col2 = matrix.get(2);
        assertEquals(3.0, col2.get(0), 0.001);
        assertEquals(6.0, col2.get(1), 0.001);
    }

    @Test
    @DisplayName("loadColumnMajor: Error case - Inconsistent row lengths")
    void testLoadColumnMajorJagged() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] jaggedData = {
                { 1, 2, 3 },
                { 4, 5 } // Length 2 instead of 3
        };

        assertThrows(IllegalArgumentException.class, () -> matrix.loadColumnMajor(jaggedData));
    }

    @Test
    @DisplayName("loadColumnMajor: Error case - Null matrix input")
    void testLoadColumnMajorNull() {
        SharedMatrix matrix = new SharedMatrix(); 
        assertThrows(IllegalArgumentException.class, () -> matrix.loadColumnMajor(null));
    }

    @Test
    @DisplayName("loadColumnMajor: Error case - Row contains null")
    void testLoadColumnMajorNullRow() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {
                { 1, 2 },
                null // Should trigger your null check inside the loop
        };

        assertThrows(IllegalArgumentException.class, () -> matrix.loadColumnMajor(data));
    }

    @Test
    @DisplayName("loadColumnMajor: Edge case - Square matrix")
    void testLoadColumnMajorSquare() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] squareData = {
                { 1, 2 },
                { 3, 4 }
        };

        matrix.loadColumnMajor(squareData);

        // Column 1 should be [2, 4]
        SharedVector col1 = matrix.get(1);
        assertEquals(2.0, col1.get(0), 0.001);
        assertEquals(4.0, col1.get(1), 0.001);
    }

    // --- loadRowMajor Tests ---

    @Test
    @DisplayName("loadRowMajor: Standard case with valid data")
    void testLoadRowMajorSuccess() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {
                { 1.0, 2.0, 3.0 },
                { 4.0, 5.0, 6.0 }
        };

        matrix.loadRowMajor(data);

        // Verify dimensions: 2 rows (vectors)
        assertEquals(2, matrix.length());
        // Verify orientation
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
        // Verify specific values
        assertEquals(1.0, matrix.get(0).get(0), 0.001);
        assertEquals(6.0, matrix.get(1).get(2), 0.001);
    }

    @Test
    @DisplayName("loadRowMajor: Edge case with single element matrix")
    void testLoadRowMajorSingleElement() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = { { 42.0 } };

        matrix.loadRowMajor(data);

        assertEquals(1, matrix.length());
        assertEquals(42.0, matrix.get(0).get(0), 0.001);
    }

    @Test
    @DisplayName("loadRowMajor: Error case - Null matrix input")
    void testLoadRowMajorNull() {
        SharedMatrix matrix = new SharedMatrix();
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(null));
    }

    @Test
    @DisplayName("loadRowMajor: Error case - Matrix with null row")
    void testLoadRowMajorNullRow() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {
                { 1, 2 },
                null, // This should trigger the exception in your loop
                { 5, 6 }
        };

        // Verification of the exception you throw: "Matrix rows cannot be null."
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(data));
    }

    @Test
    @DisplayName("loadRowMajor: Error case - Inconsistent row lengths (Jagged Matrix)")
    void testLoadRowMajorInconsistentRows() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] jaggedData = {
                { 1, 2, 3 },
                { 4, 5 } // Shorter row - should trigger the length check
        };

        // Verification of the exception: "All rows in the matrix must have the same length."
        assertThrows(IllegalArgumentException.class, () -> matrix.loadRowMajor(jaggedData));
    }

    @Test
    @DisplayName("loadRowMajor: Edge case - Empty array input")
    void testLoadRowMajorEmptyArray() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] emptyData = new double[0][0];

        matrix.loadRowMajor(emptyData);

        assertEquals(0, matrix.length(), "Loading empty array should result in 0 vectors");
    }

    // --- readRowMajor Tests ---

    @Test
    @DisplayName("readRowMajor: Read from Row-Major storage")
    void testReadFromRowMajor() {
        // Arrange: Create a matrix stored as rows
        double[][] expected = {
                { 1.0, 2.0 },
                { 3.0, 4.0 }
        };
        SharedMatrix matrix = new SharedMatrix(expected);

        // Act
        double[][] result = matrix.readRowMajor();

        // Assert: Compare 2D arrays directly
        assertArrayEquals(expected[0], result[0], "First row should match");
        assertArrayEquals(expected[1], result[1], "Second row should match");
        assertEquals(2, result.length);
        assertEquals(2, result[0].length);
    }

    @Test
    @DisplayName("readRowMajor: Read from Column-Major storage (Implicit Transpose)")
    void testReadFromColumnMajor() {
        // Arrange: Input rows, but load them as columns internally
        double[][] inputRows = {
                { 10.0, 20.0, 30.0 },
                { 40.0, 50.0, 60.0 }
        };
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(inputRows);

        // Act: Request row-major output
        double[][] result = matrix.readRowMajor();

        // Assert: Result should look exactly like inputRows
        assertEquals(2, result.length, "Should return 2 rows");
        assertEquals(3, result[0].length, "Should return 3 columns per row");

        assertArrayEquals(inputRows[0], result[0], 0.001);
        assertArrayEquals(inputRows[1], result[1], 0.001);
    }

    @Test
    @DisplayName("readRowMajor: Handle empty matrix")
    void testReadRowMajorEmpty() {
        SharedMatrix matrix = new SharedMatrix(); // Empty by default

        double[][] result = matrix.readRowMajor();

        assertEquals(0, result.length, "Empty matrix should return 0-length array");
    }

    @Test
    @DisplayName("readRowMajor: Thread-safe snapshot check (Concept)")
    void testReadRowMajorSnapshotSafety() {
        double[][] data = { { 1, 1 }, { 1, 1 } };
        SharedMatrix matrix = new SharedMatrix(data);

        // This test ensures that the return value is a NEW array (Deep Copy)
        double[][] result = matrix.readRowMajor();
        result[0][0] = 99.0; // Modify the returned array

        // Internal matrix should remain unchanged
        assertNotEquals(99.0, matrix.get(0).get(0), "Internal data must not be affected by outside modifications");
    }

    // --- Advanced Large Matrix Tests ---

    @Test
    @DisplayName("loadRowMajor: Testing with a larger 3x4 matrix")
    void testLoadRowMajorLarge() {
        double[][] data = {
                { 1, 2, 3, 4 },
                { 5, 6, 7, 8 },
                { 9, 10, 11, 12 }
        };
        SharedMatrix matrix = new SharedMatrix();

        matrix.loadRowMajor(data);

        assertEquals(3, matrix.length(), "Should have 3 row vectors");
        assertEquals(4, matrix.get(0).length(), "Each vector should have length 4");
        assertEquals(12.0, matrix.get(2).get(3), 0.001); // Last element
    }

    @Test
    @DisplayName("loadColumnMajor: Transposing a 4x2 matrix into 2 columns of length 4")
    void testLoadColumnMajorLarge() {
        double[][] data = {
                { 1, 5 },
                { 2, 6 },
                { 3, 7 },
                { 4, 8 }
        };
        SharedMatrix matrix = new SharedMatrix();

        matrix.loadColumnMajor(data);

        assertEquals(2, matrix.length(), "Should have 2 column vectors");
        assertEquals(4, matrix.get(0).length(), "Each column vector should have length 4");

        // Verify second column: [5, 6, 7, 8]
        SharedVector col1 = matrix.get(1);
        double[] expectedCol1 = { 5.0, 6.0, 7.0, 8.0 };
        for (int i = 0; i < 4; i++) {
            assertEquals(expectedCol1[i], col1.get(i), 0.001);
        }
    }

    @Test
    @DisplayName("readRowMajor: Round-trip test with 3x3 matrix")
    void testReadRowMajorRoundTrip() {
        // Arrange: 3x3 matrix
        double[][] original = {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 }
        };
        SharedMatrix matrix = new SharedMatrix();

        matrix.loadColumnMajor(original);
        double[][] result = matrix.readRowMajor();

        assertEquals(3, result.length);
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(original[i], result[i], 0.001, "Row " + i + " mismatch");
        }
    }
}