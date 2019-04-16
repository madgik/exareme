#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
This script takes all the csv files in the folder and breaks them into X pieces.
It also changes the rid on each row on the new csv files that it produces.
"""

import csv
import os
from argparse import ArgumentParser

parser = ArgumentParser()
parser.add_argument('-p', '--pieces', required=True, type=int,
                    help='Split into X pieces.')
cwd = os.getcwd()
parser.add_argument('-fp', '--folderPath', default=cwd,
                    help='The path of the folder with the csv files.')
args = parser.parse_args()

os.makedirs('output', exist_ok=True)
writers = []
for counter in range(0, args.pieces):
    fileName = 'worker' + str(counter + 1) + '.csv'
    filePath = os.path.join('output', fileName)
    writers.append(csv.writer(open(filePath, 'w+', newline=''),
                   dialect='excel'))

firstFile = True
totalRowsCounter = 0
rowsCounters = [0, 0, 0]
for file in os.listdir(args.folderPath):
    if file.endswith('.csv'):
        reader = csv.reader(open(file, newline=''), dialect='excel')
        print('Splitting file: ' + file)
        firstRow = True
        for row in reader:
            currentPiece = totalRowsCounter % args.pieces
            if firstFile == True:
                header = row
                for counter in range(0, args.pieces):
                    writers[counter].writerow(header)
                firstFile = False
                firstRow = False
                continue
            elif firstRow == True:
                if header != row:
                    raise Exception('The headers of the files do not match!')
                firstRow = False
                continue

            row[0] = rowsCounters[currentPiece]
            writers[currentPiece].writerow(row)
            rowsCounters[currentPiece] += 1
            totalRowsCounter += 1