from os import path

from utils.algorithm_utils import TransferData, ExaremeError

def merge_two_dicts(x, y):
    z = x.copy()   # start with x's keys and values
    z.update(y)    # modifies z with y's keys and values & returns None
    return z

# Set the data class that will transfer the data between local-global
class ListDatasetLocalDT(TransferData):
    def __init__(self, pathologies):
        self.pathologies = pathologies

    def get_data(self):
        return (self.pathologies)

    def __add__(self, other):
        result = ListDatasetLocalDT(merge_two_dicts(self.pathologies, other.pathologies))
        return result
