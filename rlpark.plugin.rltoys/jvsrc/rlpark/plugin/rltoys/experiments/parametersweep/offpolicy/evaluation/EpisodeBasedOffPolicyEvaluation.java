package rlpark.plugin.rltoys.experiments.parametersweep.offpolicy.evaluation;

import rlpark.plugin.rltoys.agents.offpolicy.OffPolicyAgentEvaluable;
import rlpark.plugin.rltoys.agents.representations.RepresentationFactory;
import rlpark.plugin.rltoys.envio.rl.RLAgent;
import rlpark.plugin.rltoys.experiments.helpers.Runner;
import rlpark.plugin.rltoys.experiments.helpers.Runner.RunnerEvent;
import rlpark.plugin.rltoys.experiments.parametersweep.offpolicy.internal.OffPolicyEpisodeRewardMonitor;
import rlpark.plugin.rltoys.experiments.parametersweep.parameters.Parameters;
import rlpark.plugin.rltoys.experiments.parametersweep.reinforcementlearning.AgentEvaluator;
import rlpark.plugin.rltoys.experiments.parametersweep.reinforcementlearning.OffPolicyProblemFactory;
import rlpark.plugin.rltoys.problems.RLProblem;
import zephyr.plugin.core.api.signals.Listener;

public class EpisodeBasedOffPolicyEvaluation extends AbstractOffPolicyEvaluation {
  private static final long serialVersionUID = -654783411988105997L;
  private final int maxTimeStepsPerEpisode;
  private final int nbEpisodePerEvaluation;

  public EpisodeBasedOffPolicyEvaluation(int nbRewardCheckpoint, int maxTimeStepsPerEpisode, int nbEpisodePerEvaluation) {
    super(nbRewardCheckpoint);
    this.maxTimeStepsPerEpisode = maxTimeStepsPerEpisode;
    this.nbEpisodePerEvaluation = nbEpisodePerEvaluation;
  }

  @Override
  public AgentEvaluator connectEvaluator(final int counter, Runner behaviourRunner,
      final OffPolicyProblemFactory problemFactory, final RepresentationFactory projectorFactory,
      final OffPolicyAgentEvaluable learningAgent, final Parameters parameters) {
    RLProblem problem = createEvaluationProblem(counter, problemFactory);
    RLAgent evaluatedAgent = learningAgent.createEvaluatedAgent();
    Runner runner = new Runner(problem, evaluatedAgent, Integer.MAX_VALUE, maxTimeStepsPerEpisode);
    final OffPolicyEpisodeRewardMonitor rewardMonitor = new OffPolicyEpisodeRewardMonitor(runner, nbRewardCheckpoint,
                                                                                          parameters.nbEpisode(),
                                                                                          nbEpisodePerEvaluation);
    rewardMonitor.runEvaluationIFN(0);
    behaviourRunner.onEpisodeEnd.connect(new Listener<Runner.RunnerEvent>() {
      @Override
      public void listen(RunnerEvent eventInfo) {
        rewardMonitor.runEvaluationIFN(eventInfo.nbEpisodeDone);
      }
    });
    return rewardMonitor;
  }
}
