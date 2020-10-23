/*
 * **************************************************-
 * ingrid-communication
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package net.weta.components.test;

import java.util.TimerTask;

public class ComputeCaller extends TimerTask {

  private boolean shouldRun = true;

  private ICompute _compute;
  private String _remotePeerName;
  private String _s;

  public ComputeCaller(ICompute compute, String remotePeerName) {
    _compute = compute;
    _remotePeerName = remotePeerName;
    if (_compute instanceof ISumService) {
      _s = "+";
    } else if (_compute instanceof ISubtractService) {
      _s = "-";
    }
  }

  public void run() {

    if (shouldRun) {
      for (int i = 0; i < 2; i++) {
        shouldRun = false;
        for (int j = 0; j < 2; j++) {
          try {
            int result = _compute.compute(i, j);
            System.out.println("send question to " + _remotePeerName + ": " + i + " " + _s + " " + j + " = " + result);
          } catch (Exception e) {
            System.err.println("error by calling the method to " + _remotePeerName + ". " + e.getMessage());
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
