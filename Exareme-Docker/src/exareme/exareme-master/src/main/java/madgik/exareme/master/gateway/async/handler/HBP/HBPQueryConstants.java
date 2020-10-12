package madgik.exareme.master.gateway.async.handler.HBP;

public class HBPQueryConstants {
    public static String pathologyXNotAvailable = "Pathology %s is not available.";
    public static String pathologyNotProvided = "Please provide a pathology.";
    public static String datasetXDoesNotExistInPathologyY = "Dataset(s) %s does not exist in pathology %s.";

    public static String datasetsXYZAreInactive =
            "The following datasets %s are currently unavailable. Please try again later.";

    public static String nodesUnavailable = "Some nodes are unavailable. Please try again later.";

    public static String serverErrorOccurred =
            "Something went wrong. Please consult the system administrator or try again later.";
}
