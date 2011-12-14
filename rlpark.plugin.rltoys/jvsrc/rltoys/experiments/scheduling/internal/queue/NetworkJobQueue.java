package rltoys.experiments.scheduling.internal.queue;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import rltoys.experiments.scheduling.interfaces.JobDoneEvent;
import rltoys.experiments.scheduling.interfaces.JobQueue;
import rltoys.experiments.scheduling.internal.messages.MessageJob;
import rltoys.experiments.scheduling.internal.messages.Messages;
import rltoys.experiments.scheduling.internal.network.NetworkClassLoader;
import rltoys.experiments.scheduling.internal.network.SyncSocket;
import rltoys.experiments.scheduling.queue.LocalQueue;
import zephyr.plugin.core.api.signals.Signal;
import zephyr.plugin.core.api.synchronization.Chrono;

public class NetworkJobQueue implements JobQueue {
  private static final double MessagePeriod = 1800;
  public Signal<JobQueue> onJobReceived = new Signal<JobQueue>();
  private final SyncSocket syncSocket;
  private final Map<Runnable, Integer> jobToId = new HashMap<Runnable, Integer>();
  private final NetworkClassLoader classLoader;
  private final Chrono chrono = new Chrono();
  private final Signal<JobDoneEvent> onJobDone = new Signal<JobDoneEvent>();
  private int nbJobsSinceLastMessage = 0;
  private boolean denyNewJobRequest = false;
  private final LocalQueue localQueue = new LocalQueue();

  public NetworkJobQueue(String serverHostName, int port) {
    syncSocket = new SyncSocket(connectToServer(serverHostName, port));
    syncSocket.sendClientName();
    classLoader = NetworkClassLoader.newClassLoader(syncSocket);
  }

  private void requestJobsToServer() {
    MessageJob messageJobTodo = syncSocket.jobTransaction(classLoader, jobToId.isEmpty());
    if (messageJobTodo == null)
      return;
    Runnable[] jobs = messageJobTodo.jobs();
    int[] ids = messageJobTodo.jobIds();
    ArrayList<Runnable> newJobs = new ArrayList<Runnable>();
    for (int i = 0; i < jobs.length; i++) {
      jobToId.put(jobs[i], ids[i]);
      newJobs.add(jobs[i]);
    }
    localQueue.add(newJobs.iterator(), null);
    if (messageJobTodo.nbJobs() > 0)
      onJobReceived.fire(this);
  }

  @Override
  synchronized public Runnable request() {
    if (denyNewJobRequest)
      return null;
    Runnable job = localQueue.request();
    if (job != null)
      return job;
    requestJobsToServer();
    return localQueue.request();
  }

  @Override
  synchronized public void done(Runnable todo, Runnable done) {
    Integer jobId = jobToId.remove(todo);
    syncSocket.write(new MessageJob(jobId, done));
    nbJobsSinceLastMessage += 1;
    if (chrono.getCurrentChrono() > MessagePeriod) {
      Messages.println(nbJobsSinceLastMessage / chrono.getCurrentChrono() + " jobs per seconds");
      chrono.start();
      nbJobsSinceLastMessage = 0;
    }
    onJobDone.fire(new JobDoneEvent(todo, done));
  }

  public void close() {
    classLoader.dispose();
    syncSocket.close();
  }

  public boolean canAnswerJobRequest() {
    return !syncSocket.isClosed() && !denyNewJobRequest;
  }

  @Override
  public Signal<JobDoneEvent> onJobDone() {
    return onJobDone;
  }

  public void denyNewJobRequest() {
    denyNewJobRequest = true;
  }

  public NetworkClassLoader classLoader() {
    return classLoader;
  }

  static private Socket connectToServer(String serverHostName, int port) {
    Socket socket = null;
    Random random = null;
    Exception lastException = null;
    Chrono connectionTime = new Chrono();
    while (socket == null) {
      try {
        if (lastException != null)
          System.err.println("Retrying to connect...");
        socket = new Socket(serverHostName, port);
      } catch (Exception e) {
        e.printStackTrace();
        lastException = e;
        if (random == null)
          random = new Random();
        if (connectionTime.getCurrentChrono() > 3600)
          break;
        sleepForConnection(random, 120);
      }
    }
    if (lastException != null)
      throw new RuntimeException(lastException);
    return socket;
  }

  private static void sleepForConnection(Random random, int maxWaitingTime) {
    long sleepingTime = (long) (random.nextDouble() * maxWaitingTime + 5);
    System.err.println(sleepingTime + "s of sleeping time before another attempt to connect");
    try {
      Thread.sleep(sleepingTime * 1000);
    } catch (InterruptedException e) {
    }
  }
}
