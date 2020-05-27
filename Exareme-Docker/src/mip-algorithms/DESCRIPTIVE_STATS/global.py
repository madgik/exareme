import sys
from descriptive_stats import DescriptiveStats

def main(args):
    DescriptiveStats(args[1:]).global_()

if __name__ == "__main__":
    DescriptiveStats(sys.argv[1:]).global_()
