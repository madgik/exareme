package madgik.exareme.master.engine.iterations.handler;

import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;

import static madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState.IterativeAlgorithmPhasesModel.termination_condition;

/**
 * Utilities needed for the iterations
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class IterationsHandlerDFLUtils {
    /**
     * Generates the output table name of a given iterative phase.
     *
     * @param algorithmKey   the iterative algorithm's key
     * @param iterativePhase the iterative phase for which the table name is generated
     */
    public static String generateIterativePhaseOutputTblName(
            String algorithmKey,
            IterativeAlgorithmState.IterativeAlgorithmPhasesModel iterativePhase) {
        String iterativePhaseOutputTblName =
                IterationsConstants.iterationsOutputTblPrefix + "_" + algorithmKey + "_" + iterativePhase.name();
        if (iterativePhase.equals(IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step) ||
                iterativePhase.equals(termination_condition))
            return "${" + iterativePhaseOutputTblName + "}";
        else
            return iterativePhaseOutputTblName;
    }

    /**
     * Generates the initPhase output table name of the current algorithm
     */
    public static String getInitPhaseOutputTblName(String algorithmKey) {
        return generateIterativePhaseOutputTblName(
                algorithmKey,
                IterativeAlgorithmState.IterativeAlgorithmPhasesModel.init);
    }

    /**
     * Generates the stepPhaseOutputTbl variable name (for later substitution)
     */
    public static String getStepPhaseOutputTblVariableName(String algorithmKey) {
        return IterationsConstants.iterationsOutputTblPrefix + "_" + algorithmKey + "_"
                + IterativeAlgorithmState.IterativeAlgorithmPhasesModel.step.name();
    }

    /**
     * Generates the terminationConditionOutputTbl variable name (for later substitution)
     */
    public static String getTermConditionPhaseOutputTblVariableName(String algorithmKey) {
        return IterationsConstants.iterationsOutputTblPrefix + "_" + algorithmKey + "_"
                + termination_condition.name();
    }


}
