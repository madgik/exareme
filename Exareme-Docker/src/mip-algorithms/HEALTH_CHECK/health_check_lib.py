import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import TransferData, ExaremeError

# Set the data class that will transfer the data between local-global
class HealthCheckLocalDT(TransferData):
    def __init__(self, name):
        self.node_name = name

    def get_data(self):
        return (self.node_name)

    def __add__(self, other):
        result = HealthCheckLocalDT(self.node_name + ', ' + other.node_name)
        return result
