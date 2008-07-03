package net.weta.components.test;

public class SumService implements ISumService {

  public int sum(int i, int j) {
    return i + j;
  }

  public int compute(int i, int j) {
    System.out.println("receive question: [" + i + " + " + j + "]");
    return sum(i, j);
  }
}
