package io.warp10.script.ext.egads;

import java.util.List;

import com.yahoo.egads.data.Anomaly.IntervalSequence;
import com.yahoo.egads.data.TimeSeries.DataSequence;
import com.yahoo.egads.models.adm.AnomalyDetectionModel;

import io.warp10.continuum.gts.GeoTimeSerie;
import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

/**
 * Tune anomaly detection model
 */
public class EGADSTUNE extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  public EGADSTUNE(String name) {
    super(name);
  }

  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    //
    // Extract the GTS
    //
    
    Object top = stack.pop();
    
    if (!(top instanceof List)) {
      throw new WarpScriptException(getName() + " expects a list of anomalies on top of the stack.");
    }
        
    top = stack.pop();
    
    if (!(top instanceof GeoTimeSerie)) {
      throw new WarpScriptException(getName() + " expects an expected Geo Time Series below the anomaly list.");
    }

    GeoTimeSerie expectedGTS = (GeoTimeSerie) top;
    
    top = stack.pop();
    
    if (!(top instanceof GeoTimeSerie)) {
      throw new WarpScriptException(getName() + " expects an observed Geo Time Series below the expewcted one.");
    }
    
    GeoTimeSerie observedGTS = (GeoTimeSerie) top;
    
    top = stack.pop();
    
    if (!(top instanceof SnapshotableModel)) {
      throw new WarpScriptException(getName() + " expects an EGADS model below the observed and expected Geo Time Series.");
    }
    
    SnapshotableModel model = (SnapshotableModel) top;
    
    if (model.getModel() instanceof AnomalyDetectionModel) {
      try {
        DataSequence observed = EGADSUtils.fromGTS(observedGTS);
        DataSequence expected = EGADSUtils.fromGTS(expectedGTS);
        IntervalSequence anomalies = null;
        
        ((AnomalyDetectionModel) model.getModel()).tune(observed, expected, anomalies);
        
        stack.push(model);
      } catch (Exception e) {
        throw new WarpScriptException(getName() + " encountered an exception when training the model.");
      }
    } else {
      throw new WarpScriptException(getName() + " invalid model.");
    }
    
    return stack;
  }
}
