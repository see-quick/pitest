package org.pitest.mutationtest.execute;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.DetectionStatus.NON_VIABLE;
import static org.pitest.mutationtest.LocationMother.aLocation;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.environment.ResetEnvironment;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;

import junit.framework.AssertionFailedError;

public class MutationTestWorkerTest {

  private MutationTestWorker                          testee;

  @Mock
  private ClassLoader                                 loader;

  @Mock
  private Mutater                                     mutater;

  @Mock
  private HotSwap                                     hotswapper;

  @Mock
  private TimeOutDecoratedTestSource                  testSource;

  @Mock
  private Reporter                                    reporter;

  @Mock
  private ResetEnvironment                             reset;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.testee = new MutationTestWorker(this.hotswapper, this.mutater,
        this.loader, this.reset,false);
  }

  @Test
  public void shouldDescribeEachExaminedMutation() throws IOException {
    final MutationDetails mutantOne = makeMutant("foo", 1);
    final MutationDetails mutantTwo = makeMutant("foo", 2);
    final Collection<MutationDetails> range = Arrays.asList(mutantOne,
        mutantTwo);
    this.testee.run(range, this.reporter, this.testSource);
    verify(this.reporter).describe(mutantOne.getId());
    verify(this.reporter).describe(mutantTwo.getId());
  }

  @Test
  @Ignore("disabled while checking coverage issue")
  public void shouldReportNoCoverageForMutationWithNoTestCoverage()
      throws IOException {
    final MutationDetails mutantOne = makeMutant("foo", 1);
    final Collection<MutationDetails> range = Arrays.asList(mutantOne);
    this.testee.run(range, this.reporter, this.testSource);
    verify(this.reporter).report(mutantOne.getId(),
        MutationStatusTestPair.notAnalysed(0, DetectionStatus.NO_COVERAGE, Collections.emptyList()));
  }

  @Test
  public void shouldReportWhenMutationNotDetected() throws IOException {
    final MutationDetails mutantOne = makeMutant("foo", 1);
    final Collection<MutationDetails> range = Arrays.asList(mutantOne);
    final TestUnit tu = makePassingTest();
    when(this.testSource.translateTests(any(List.class))).thenReturn(
        Collections.singletonList(tu));
    when(
        this.hotswapper.insertClass(any(ClassName.class), any(ClassLoader.class),
            any(byte[].class))).thenReturn(true);
    this.testee.run(range, this.reporter, this.testSource);
    verify(this.reporter).describe(mutantOne.getId());
  }

  @Test
  public void shouldReportWhenMutationNotViable() throws IOException {
    final MutationDetails mutantOne = makeMutant("foo", 1);
    final Collection<MutationDetails> range = Arrays.asList(mutantOne);
    final TestUnit tu = makePassingTest();
    when(this.testSource.translateTests(any(List.class))).thenReturn(
        Collections.singletonList(tu));
    when(
        this.hotswapper.insertClass(any(ClassName.class), any(ClassLoader.class),
            any(byte[].class))).thenReturn(false);
    this.testee.run(range, this.reporter, this.testSource);
    verify(this.reporter).describe(mutantOne.getId());
    verify(this.reporter).report(mutantOne.getId(),
        MutationStatusTestPair.notAnalysed(0, NON_VIABLE,Collections.singletonList("atest")));
  }

  @Test
  public void shouldReportWhenMutationKilledByTest() throws IOException {
    final MutationDetails mutantOne = makeMutant("foo", 1);
    final Collection<MutationDetails> range = Arrays.asList(mutantOne);
    final TestUnit tu = makeFailingTest();
    when(this.testSource.translateTests(any(List.class))).thenReturn(
        Collections.singletonList(tu));
    when(
        this.hotswapper.insertClass(any(ClassName.class), any(ClassLoader.class),
            any(byte[].class))).thenReturn(true);
    this.testee.run(range, this.reporter, this.testSource);
    verify(this.reporter).report(
        mutantOne.getId(),
        new MutationStatusTestPair(1, DetectionStatus.KILLED, tu
            .getDescription().getName()));
  }

  private TestUnit makeFailingTest() {
    return new TestUnit() {

      @Override
      public void execute(final ResultCollector rc) {
        rc.notifyStart(getDescription());
        rc.notifyEnd(getDescription(), new AssertionFailedError());
      }

      @Override
      public Description getDescription() {
        return new Description("atest");
      }

    };
  }

  private TestUnit makePassingTest() {
    return new TestUnit() {

      @Override
      public void execute(final ResultCollector rc) {
        rc.notifyStart(getDescription());
        rc.notifyEnd(getDescription());
      }

      @Override
      public Description getDescription() {
        return new Description("atest");
      }

    };
  }

  public MutationDetails makeMutant(final String clazz, final int index) {
    final MutationIdentifier id = aMutationId()
        .withLocation(aLocation().withClass(ClassName.fromString(clazz)))
        .withIndex(index).withMutator("mutator").build();
    final MutationDetails md = new MutationDetails(id, "sourceFile", "desc",
        42, 0);

    when(this.mutater.getMutation(md.getId())).thenReturn(
        new Mutant(md, new byte[0]));

    return md;
  }

}
