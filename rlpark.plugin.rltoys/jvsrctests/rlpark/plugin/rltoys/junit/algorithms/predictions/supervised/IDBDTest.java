package rlpark.plugin.rltoys.junit.algorithms.predictions.supervised;

import org.junit.Assert;
import org.junit.Test;

import rlpark.plugin.rltoys.algorithms.predictions.supervised.Autostep;
import rlpark.plugin.rltoys.algorithms.predictions.supervised.IDBD;
import rlpark.plugin.rltoys.experiments.testing.predictions.NoisyInputSum;

public class IDBDTest {
  @Test
  public void testIDBD() {
    NoisyInputSum problem = new NoisyInputSum();
    double error = problem.evaluateLearner(new IDBD(NoisyInputSum.NbInputs, 0.001));
    Assert.assertEquals(2.0, error, 0.1);
    error = problem.evaluateLearner(new IDBD(NoisyInputSum.NbInputs, 0.01));
    Assert.assertEquals(1.5, error, 0.1);
  }

  @Test
  public void testAutostep() {
    NoisyInputSum problem = new NoisyInputSum();
    double error = problem.evaluateLearner(new Autostep(NoisyInputSum.NbInputs));
    Assert.assertEquals(2.3, error, 0.1);
  }
}
