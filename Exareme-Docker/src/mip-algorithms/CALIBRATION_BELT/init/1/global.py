import sys

from CALIBRATION_BELT import CalibrationBelt

if __name__ == "__main__":
    CalibrationBelt(sys.argv[1:]).global_init()
