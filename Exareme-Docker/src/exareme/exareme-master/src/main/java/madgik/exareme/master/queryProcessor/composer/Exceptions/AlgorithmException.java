package madgik.exareme.master.queryProcessor.composer.Exceptions;

public class AlgorithmException extends Exception{
    public AlgorithmException(String algorithmName, String message) {
        super(message + "  Algorithm: " + algorithmName);
    }
}