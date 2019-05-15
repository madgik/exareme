#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
This script takes all the csv files in the folder, it harmonises them and breaks them into X pieces.
"""

import json
import csv
import os
from argparse import ArgumentParser


def createColumnList(variablesMetadataPath):
    variablesMetadata = open(variablesMetadataPath)
    metadataJSON = json.load(variablesMetadata)

    metadataDictionary = []
    metadataDictionary = addGroupVariablesToList(metadataJSON,
            metadataDictionary)
    metadataDictionary.sort()
    metadataDictionary.insert(0, 'subjectcode')
    metadataDictionary.insert(0, 'row_id')
    metadataDictionary.insert(0, 'rid')
    return metadataDictionary


def addGroupVariablesToList(groupMetadata, metadataDictionary):
    if 'variables' in groupMetadata:
        for variable in groupMetadata['variables']:
            metadataDictionary.append(variable['code'])
    if 'groups' in groupMetadata:
        for group in groupMetadata['groups']:
            metadataDictionary = addGroupVariablesToList(group,
                    metadataDictionary)
    return metadataDictionary


def headerToDict(header):
    dict = {}
    for (idx, column) in enumerate(header):
        dict[column] = idx
    return dict


def harmonizeRow(headerDict, row, columnList):
    harmonisedRow = [''] * len(columnList)
    for (idx, column) in enumerate(columnList):
        columnLocation = headerDict.get(column)
        if columnLocation:
            harmonisedRow[idx] = row[headerDict[column]]
    return harmonisedRow


def main():

    # Read the parameters

    parser = ArgumentParser()
    parser.add_argument('-mp', '--metadataPath', required=True,
                        help='The file with the metadata of the csvs.')
    parser.add_argument('-p', '--pieces', required=True, type=int,
                        help='Split into X pieces.')
    parser.add_argument('-fp', '--folderPath', default='',
                        help='The path of the folder with the csv files.'
                        )
    args = parser.parse_args()

    # Create the list with all the columns

    harmonisedColumnList = createColumnList(os.path.abspath(args.metadataPath))

    # Create the csv's that are going to be filled with the data

    os.makedirs('output', exist_ok=True)
    writers = []
    for counter in range(0, args.pieces):
        fileName = 'worker' + str(counter + 1) + '.csv'
        filePath = os.path.join('output', fileName)
        writers.append(csv.writer(open(filePath, 'w+', newline=''),
                       dialect='excel'))

    # Print the header to every csv

    for counter in range(0, args.pieces):
        writers[counter].writerow(harmonisedColumnList)

    # Split the csv files into the new harmonised pieces

    totalRowsCounter = 0
    rowsCounters = [0] * args.pieces

    for file in os.listdir(os.path.abspath(args.folderPath)):
        if file.endswith('.csv'):
            filePath = os.path.join(os.path.abspath(args.folderPath),
                                    file)
            reader = csv.reader(open(filePath, newline=''),
                                dialect='excel')
            print ('Splitting file: ' + filePath)
            headerDict = headerToDict(next(reader))
            for row in reader:
                harmonizedRow = harmonizeRow(headerDict, row,
                        harmonisedColumnList)

                # Define the rid for the row in the specific csv

                destinationFile = totalRowsCounter % args.pieces
                harmonizedRow[0] = rowsCounters[destinationFile]
                writers[destinationFile].writerow(harmonizedRow)

                # Increasing the counters

                rowsCounters[destinationFile] += 1
                totalRowsCounter += 1


if __name__ == '__main__':
    main()