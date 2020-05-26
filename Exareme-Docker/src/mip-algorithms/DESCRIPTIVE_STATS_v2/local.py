import sys
from descriptive_stats import DescriptiveStats

def main(args):
    return DescriptiveStats(args[1:]).local_()

if __name__ == "__main__":
    DescriptiveStats(sys.argv[1:]).local_()
