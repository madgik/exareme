import sys
from PCA import PCA

def main(args):
    PCA(args[1:]).global_final()

if __name__ == "__main__":
    PCA(sys.argv[1:]).global_final()
