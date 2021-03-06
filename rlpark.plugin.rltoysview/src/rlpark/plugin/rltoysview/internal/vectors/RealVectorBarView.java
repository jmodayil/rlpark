package rlpark.plugin.rltoysview.internal.vectors;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

import rlpark.plugin.rltoys.math.vector.RealVector;
import zephyr.plugin.core.api.synchronization.Clock;
import zephyr.plugin.core.internal.helpers.ClassViewProvider;
import zephyr.plugin.core.internal.views.helpers.BackgroundCanvasView;
import zephyr.plugin.plotting.internal.actions.CenterPlotAction;
import zephyr.plugin.plotting.internal.actions.CenterPlotAction.ViewCenterable;
import zephyr.plugin.plotting.internal.bar2d.Bar2D;
import zephyr.plugin.plotting.internal.mousesearch.MouseSearch;

@SuppressWarnings("restriction")
public class RealVectorBarView extends BackgroundCanvasView<RealVector> implements ViewCenterable {
  public static class Provider extends ClassViewProvider {
    public Provider() {
      super(RealVector.class);
    }
  }

  protected double[] data;
  private final Bar2D bar = new Bar2D();
  private MouseSearch mouseSearch;

  @Override
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    mouseSearch = new MouseSearch(bar.dataBuffer(), backgroundCanvas.canvas());
    backgroundCanvas.addOverlay(mouseSearch);
  }

  @Override
  protected void setToolbar(IToolBarManager toolBarManager) {
    toolBarManager.add(new CenterPlotAction(this));
  }

  @Override
  public boolean synchronize(RealVector vector) {
    data = vector.accessData();
    return true;
  }


  @Override
  public void paint(PainterMonitor painterListener, GC gc) {
    gc.setAntialias(SWT.OFF);
    bar.clear(gc);
    bar.draw(gc, data);
  }

  @Override
  public void dispose() {
    super.dispose();
    bar.dispose();
  }

  @Override
  public void center() {
    bar.axes().x.reset();
    bar.axes().y.reset();
  }

  @Override
  protected boolean isInstanceSupported(Object instance) {
    return RealVector.class.isInstance(instance);
  }

  @Override
  protected void setLayout(Clock clock, RealVector current) {
  }

  @Override
  protected void unsetLayout() {
    bar.clearData();
  }
}
