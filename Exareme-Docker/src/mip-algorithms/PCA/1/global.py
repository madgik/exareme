import sys
from PCA import PCA

def main(args):
    PCA(args[1:]).global_init()

if __name__ == "__main__":
    PCA(sys.argv[1:]).global_init()
