package rltoys.math.vector;

import static rltoys.utils.Utils.notImplemented;

import java.util.HashMap;
import java.util.Map;

import rltoys.utils.Utils;
import zephyr.plugin.core.api.monitoring.abstracts.DataMonitor;
import zephyr.plugin.core.api.monitoring.abstracts.MonitorContainer;
import zephyr.plugin.core.api.monitoring.abstracts.Monitored;

public class SVector extends AbstractVector implements SparseRealVector, MonitorContainer {
  private static final long serialVersionUID = -9095949488538243211L;
  public final Map<Integer, Double> values = new HashMap<Integer, Double>();
  public final int size;

  public SVector(int size) {
    this.size = size;
  }

  public SVector(RealVector orig) {
    this(orig.getDimension());
    if (orig instanceof SVector)
      set((SVector) orig);
    else if (orig instanceof BinaryVector)
      initWithConstantValue((BinaryVector) orig, 1.0);
    else
      set(orig.accessData());
  }

  private void initWithConstantValue(BinaryVector binaryVector, double value) {
    for (int index : binaryVector.activeIndexes())
      setEntry(index, value);
  }

  public SVector(BinaryVector orig) {
    this(orig.getDimension());
    initWithConstantValue(orig, 1.0);
  }

  public SVector(double... o) {
    this(o.length);
    set(o);
  }

  private void set(SVector other) {
    values.putAll(other.values);
  }

  private void set(double... o) {
    for (int i = 0; i < size; i++)
      setEntry(i, o[i]);
  }

  @Override
  public ModifiableVector addToSelf(RealVector other) {
    if (other instanceof BVector) {
      BVector bother = (BVector) other;
      for (int index : bother.activeIndexes())
        setEntry(index, getEntry(index) + 1);
      return this;
    }
    if (other instanceof SparseVector) {
      ((SparseVector) other).forEach(new ElementIterator() {
        @Override
        public void element(int index, double value) {
          setEntry(index, getEntry(index) + value);
        }
      });
      return this;
    }
    double[] o = other.accessData();
    for (int i = 0; i < size; i++)
      setEntry(i, getEntry(i) + o[i]);
    return this;
  }

  @Override
  public SVector copy() {
    SVector copy = newInstance(size);
    copy.set(this);
    return copy;
  }

  @Override
  public int getDimension() {
    return size;
  }

  @Override
  public double getEntry(int i) {
    Double value = values.get(i);
    return value != null ? value : 0.0;
  }

  @Override
  public ModifiableVector mapMultiplyToSelf(double d) {
    if (d == 0.0) {
      set(0.0);
      return this;
    }
    for (Map.Entry<Integer, Double> entry : values.entrySet())
      entry.setValue(entry.getValue() * d);
    return this;
  }

  @Override
  public SVector newInstance(int size) {
    return new SVector(size);
  }

  @Override
  public void set(double d) {
    if (d == 0.0) {
      clear();
      return;
    }
    for (int i = 0; i < size; i++)
      setEntry(i, d);
  }

  @Override
  public void setEntry(int i, double d) {
    if (d == 0.0)
      values.remove(i);
    else
      values.put(i, d);
  }

  @Override
  public void setSubVector(final int i, RealVector other) {
    if (other instanceof SparseVector) {
      ((SparseVector) other).forEach(new ElementIterator() {
        @Override
        public void element(int index, double value) {
          setEntry(index + i, value);
        }
      });
      return;
    }
    int oSize = other.getDimension();
    for (int j = 0; j < oSize; j++)
      setEntry(j + i, other.getEntry(j));
  }

  @Override
  public ModifiableVector subtractToSelf(RealVector other) {
    if (other instanceof SVector) {
      ((SVector) other).subtractSelfTo(this);
      return this;
    } else if (other instanceof SparseVector) {
      ((SparseVector) other).subtractSelfTo(this);
      return this;
    }
    double[] o = other.accessData();
    for (int i = 0; i < size; i++)
      setEntry(i, getEntry(i) - o[i]);
    return this;
  }

  @Override
  public double dotProduct(RealVector other) {
    return dotProduct(this, other);
  }

  @Override
  public double dotProduct(double[] data) {
    return dotProduct(this, new PVector(data));
  }

  @Override
  public double[] accessData() {
    double[] result = new double[size];
    for (Map.Entry<Integer, Double> entry : values.entrySet())
      result[entry.getKey()] = entry.getValue();
    return result;
  }

  @Override
  public boolean checkValues() {
    for (Double value : values.values())
      if (!Utils.checkValue(value))
        return false;
    return true;
  }

  @Override
  public void addSelfTo(double[] data) {
    for (Map.Entry<Integer, Double> entry : values.entrySet())
      data[entry.getKey()] += entry.getValue();
  }

  @Override
  public void subtractSelfTo(double[] data) {
    for (Map.Entry<Integer, Double> entry : values.entrySet())
      data[entry.getKey()] -= entry.getValue();
  }

  @Override
  public void subtractSelfTo(SparseRealVector other) {
    for (Map.Entry<Integer, Double> entry : values.entrySet()) {
      Integer i = entry.getKey();
      other.setEntry(i, other.getEntry(i) - entry.getValue());
    }
  }

  @Override
  public int nonZeroElements() {
    return values.size();
  }

  @Override
  public void forEach(ElementIterator elementIterator) {
    for (Map.Entry<Integer, Double> entry : values.entrySet())
      elementIterator.element(entry.getKey(), entry.getValue());
  }

  public static String toString(SVector svector) {
    StringBuilder result = new StringBuilder("[");
    int nbActiveElement = 0;
    for (Map.Entry<Integer, Double> entry : svector.values.entrySet()) {
      result.append(String.format("%d:%f,", entry.getKey(), entry.getValue()));
      nbActiveElement++;
    }
    if (nbActiveElement == 0)
      return "[]";
    return result.substring(0, result.length() - 1) + "]";
  }

  static public double dotProduct(SVector svector, RealVector other) {
    double result = 0.0;
    for (Map.Entry<Integer, Double> entry : svector.values.entrySet())
      result += entry.getValue() * other.getEntry(entry.getKey());
    return result;
  }

  @Override
  public void addToMonitor(DataMonitor monitor) {
    monitor.add("nbActive", 0, new Monitored() {
      @Override
      public double monitoredValue() {
        return nonZeroElements();
      }
    });
  }

  @Override
  public ModifiableVector getSubVector(int index, int n) {
    notImplemented();
    return null;
  }

  @Override
  public ModifiableVector mapAbsToSelf() {
    for (Map.Entry<Integer, Double> entry : values.entrySet())
      entry.setValue(Math.abs(entry.getValue()));
    return this;
  }

  @Override
  public ModifiableVector ebeMultiplyToSelf(RealVector other) {
    for (Map.Entry<Integer, Double> entry : values.entrySet())
      entry.setValue(other.getEntry(entry.getKey()) * entry.getValue());
    return this;
  }

  @Override
  public ModifiableVector copyAsMutable() {
    return copy();
  }

  public void clear() {
    values.clear();
  }
}
