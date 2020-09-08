package madgik.exareme.master.queryProcessor.HBP.Exceptions;

public class AlgorithmException extends Exception{
    public AlgorithmException(String algorithmName, String message) {
        super(message + "  Algorithm: " + algorithmName);
    }
}