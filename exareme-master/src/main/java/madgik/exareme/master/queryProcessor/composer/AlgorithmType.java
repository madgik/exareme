package madgik.exareme.master.queryProcessor.composer;

/**
 * @author alex
 */
public enum AlgorithmType {
  local_global,                             // simple
  multiple_local_global,                    // sequential local_global
  pipeline,                                 // model, updated model
  multiple_local_multiple_global,           // simple but multiple sequential local and multiple sequential global
  multiple__multiple_local_multiple_global  // sequential multiple sequential local and multiple sequential global
}
