package net.weta.components.test;

import java.util.TimerTask;

public class SumCaller extends TimerTask {
  private ISumService _sumService;

  private boolean shouldRun = true;

  SumCaller(ISumService sumService) {
    _sumService = sumService;
  }

  @Override
  public void run() {

    if (shouldRun) {
      for (int i = 0; i < 10; i++) {
        shouldRun = false;
        for (int j = 0; j < 10; j++) {
          System.out.println("call sum method...");
          try {
            int sum = _sumService.sum(i, j);
            System.out.println(i + " + " + j + " = " + sum);
          } catch (Exception e) {
            System.err.println("error by calling the sum method. " + e.getMessage());
            break;
          }
        }
      }
      shouldRun = true;
    } else {
      System.out.println("do not run again because i am running.");
    }

  }
}
