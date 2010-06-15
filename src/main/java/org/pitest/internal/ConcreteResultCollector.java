/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.internal;

import java.util.concurrent.BlockingQueue;

import org.pitest.TestResult;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.testunit.TestUnitState;

public final class ConcreteResultCollector implements ResultCollector {

  private final BlockingQueue<TestResult> feedback;

  public ConcreteResultCollector(final BlockingQueue<TestResult> feedback) {
    this.feedback = feedback;
  }

  public void notifyEnd(final TestResult testResult) {
    this.feedback.add(testResult);
  }

  public void notifyStart(final TestUnit tu) {
    this.feedback.add(new TestResult(tu, null, TestUnitState.STARTED));
  }

  public void notifySkipped(final TestUnit tu) {
    this.feedback.add(new TestResult(tu, null, TestUnitState.NOT_RUN));
  }

}