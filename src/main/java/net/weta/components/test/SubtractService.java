package net.weta.components.test;

import java.io.Serializable;

public class SubtractService implements Serializable, ISubtractService {


  public int subtract(int i, int j) {
    return i - j;
  }

  public int compute(int i, int j) {
    System.out.println("receive question: [" + i + " - " + j + "]");
    return subtract(i, j);
  }


}
