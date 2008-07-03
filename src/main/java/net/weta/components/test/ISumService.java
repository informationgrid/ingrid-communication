package net.weta.components.test;

import java.io.Serializable;

public interface ISumService extends Serializable, ICompute {

  int sum(int i, int j);
}
