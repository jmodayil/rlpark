package rlpark.plugin.rltoys.junit.experiments.reinforcementlearning;

import org.junit.Assert;
import org.junit.Test;

import rlpark.plugin.rltoys.experiments.parametersweep.offpolicy.evaluation.ContinuousOffPolicyEvaluation;
import rlpark.plugin.rltoys.experiments.parametersweep.parameters.FrozenParameters;
import rlpark.plugin.rltoys.experiments.parametersweep.reinforcementlearning.OffPolicyProblemFactory;
import rlpark.plugin.rltoys.junit.experiments.reinforcementlearning.OffPolicyComponentTest.OffPolicySweepDescriptor;
import rlpark.plugin.rltoys.junit.experiments.reinforcementlearning.problemtest.OffPolicyRLProblemFactoryTest;

public class OffPolicyContinuousEvaluationSweepTest extends AbstractOffPolicyRLSweepTest {
  @Test
  public void testSweepOneEpisode() {
    OffPolicyProblemFactory problemFactory = new OffPolicyRLProblemFactoryTest(1, NbTimeSteps);
    ContinuousOffPolicyEvaluation evaluation = new ContinuousOffPolicyEvaluation(10);
    OffPolicySweepDescriptor descriptor = new OffPolicySweepDescriptor(problemFactory, evaluation);
    testSweep(descriptor);
    checkFile(descriptor, Integer.MAX_VALUE);
    Assert.assertTrue(isBehaviourPerformanceChecked());
  }

  @Override
  protected void checkParameters(String testFolder, String filename, int divergedOnSlice, FrozenParameters parameters) {
    for (String label : parameters.labels()) {
      int checkPoint = 0;
      if (label.contains("Reward"))
        checkPoint = Integer.parseInt(label.substring(label.length() - 2, label.length()));
      int sliceSize = NbTimeSteps / NbRewardCheckPoint;
      if (label.contains("Start")) {
        Assert.assertEquals(checkPoint * sliceSize, (int) parameters.get(label));
        continue;
      }
      if (label.contains("Behaviour")) {
        checkBehaviourPerformanceValue(filename, label, parameters.get(label));
        continue;
      }
      int multiplierAdjusted = Integer.parseInt(testFolder.substring(testFolder.length() - 2));
      if (label.contains("Slice"))
        assertValue(checkPoint >= divergedOnSlice, multiplierAdjusted, parameters.get(label));
      if (label.contains("Cumulated"))
        assertValue(divergedOnSlice <= NbRewardCheckPoint, multiplierAdjusted, parameters.get(label));
    }
  }
}
