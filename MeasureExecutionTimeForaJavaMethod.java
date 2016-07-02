/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerPackage;

import com.google.common.base.Stopwatch;

/**
 *
 * @author MalahovKS
 */






public class MeasureExecutionTimeForaJavaMethod {

    /**
     * @param args the command line arguments
     */
    
    
    
    public static void main(String[] args) {

     
      //With System.currentTimeMillis()
        
//      long startTime = System.currentTimeMillis();
//
//      long total = 0;
//      for (int i = 0; i < 10000000; i++) {
//         total += i;
//      }
//
//      long stopTime = System.currentTimeMillis();
//      long elapsedTime = stopTime - startTime;
//      System.out.println(elapsedTime);
        
        
      //With a StopWatch Guava class
        
      Stopwatch stopwatch = Stopwatch.createStarted();

      long total = 0;
      for (int i = 0; i < 10000000; i++) {
         total += i;
      }

      stopwatch.stop(); // optional
      //long millis = stopwatch.elapsed(MILLISECONDS);
      
      System.out.println("time: " + stopwatch);
        
      
      
    }
}
