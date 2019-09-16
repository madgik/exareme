import sys
import os

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))) + '/utils/')

from health_check_lib import HealthCheckLocalDT


def main():
    # Get node_name from env variable and return it
    local_out = HealthCheckLocalDT(os.environ['NODE_NAME'])

    # Return the output data (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main()
