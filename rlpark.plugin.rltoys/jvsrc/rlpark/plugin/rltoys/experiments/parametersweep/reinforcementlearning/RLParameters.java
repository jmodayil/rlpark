package rlpark.plugin.rltoys.experiments.parametersweep.reinforcementlearning;

public class RLParameters {
  public static final String OnPolicyTimeStepsEvaluationFlag = "onPolicyTimeStepsEvaluationFlag";
  public static final String MaxEpisodeTimeSteps = "maxEpisodeTimeSteps";
  public static final String NbEpisode = "nbEpisode";
  public static final String Gamma = "gamma";
  public static final String AverageReward = "averageReward";
  public static final String Lambda = "Lambda";
  public static final String Tau = "Tau";
  public static final String AveRewardStepSize = "AveRewardStepSize";
  public static final String ActorStepSize = "ActorStepSize";
  public static final String ValueFunctionStepSize = "ValueFunctionStepSize";
  public static final String ValueFunctionSecondStepSize = "ValueFunctionSecondStepSize";
  public static final String Temperature = "Temperature";

  final static public double[] getSoftmaxValues() {
    return new double[] { 100.0, 50.0, 10.0, 5.0, 1.0, .5, .1, .05, .01 };
  }

  final static public double[] getTauValues() {
    return new double[] { 1, 2, 4, 8, 16, 32 };
  }

  final static public double[] getStepSizeValues() {
    return new double[] { .0001, .0005, .001, .005, .01, .05, .1, .5, 1. };
  }

  final static public double[] getWideSweepStepSizeValues() {
    return new double[] { 1e-8, 1e-7, 1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 1e-1, 1e0, 1e1 };
  }

  public static double[] getStepSizeValuesWithZero() {
    double[] withoutZero = getStepSizeValues();
    double[] result = new double[withoutZero.length + 1];
    System.arraycopy(withoutZero, 0, result, 1, withoutZero.length);
    result[0] = 0.0;
    return result;
  }
}
