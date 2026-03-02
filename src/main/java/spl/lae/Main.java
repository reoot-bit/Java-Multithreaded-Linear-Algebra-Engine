package spl.lae;

import java.io.IOException;

import memory.SharedMatrix;
import parser.*;

public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length < 3) {
      System.err.println("Usage: <numThreads> <inputFile> <outputFile>");
      return;
    }

    int numThreads = Integer.parseInt(args[0]);
    String inputFile = args[1];
    String outputFile = args[2];

    LinearAlgebraEngine lae = new LinearAlgebraEngine(numThreads);
    try {
      // JSON בניית עץ חישוב מקובץ 
      InputParser inputParser = new InputParser();
      ComputationNode root = inputParser.parse(inputFile);

      ComputationNode resultNode = lae.run(root);
      if (resultNode != null && resultNode.getMatrix() != null) {
                double[][] res = resultNode.getMatrix();
                System.out.println("DEBUG: Calculation finished! Result size: " + 
                                   res.length + " columns.");
            } else {
                System.err.println("DEBUG: Result is NULL!");
            }
      OutputWriter.write(resultNode.getMatrix(), outputFile);
      System.out.println("Computation completed. Generating report...");
      System.out.println(lae.getWorkerReport());
    } catch (Exception e) {
      OutputWriter.write(e.getMessage(), outputFile);
      System.out.println(lae.getWorkerReport());
    } finally {
    
    }
}
}
    
    
    
  
