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

    metadataList = []
    metadataList = addGroupVariablesToList(metadataJSON, metadataList)
    metadataList.sort()
    metadataList.insert(0, 'subjectcode')
    return metadataList


def addGroupVariablesToList(groupMetadata, metadataList):
    if 'variables' in groupMetadata:
        for variable in groupMetadata['variables']:
            metadataList.append(variable['code'])
    if 'groups' in groupMetadata:
        for group in groupMetadata['groups']:
            metadataList = addGroupVariablesToList(group, metadataList)
    return metadataList


def headerToDict(header):
    dict = {}
    for (idx, column) in enumerate(header):
        dict[column] = idx
    return dict


def checkCSVHeadersExistInMetadata(headerDict, harmonisedColumnList):
    for column in headerDict:
        if column not in harmonisedColumnList:
            raise ValueError('Column ' + column
                             + ' does not exist in the metadata.')


def harmonizeRow(
    headerDict,
    row,
    columnList,
    subjectcode,
    ):
    harmonisedRow = [''] * len(columnList)
    for (idx, column) in enumerate(columnList):
        columnLocation = headerDict.get(column)
        if columnLocation != None:
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

    harmonisedColumnList = \
        createColumnList(os.path.abspath(args.metadataPath))

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

            # TODO Check if every column in the header exists in the metadata

            checkCSVHeadersExistInMetadata(headerDict,
                    harmonisedColumnList)

            for row in reader:

                # Define the subjectcode for the row in the specific csv

                destinationFile = totalRowsCounter % args.pieces
                harmonizedRow = harmonizeRow(headerDict, row,
                        harmonisedColumnList,
                        rowsCounters[destinationFile])
                writers[destinationFile].writerow(harmonizedRow)

                # Increasing the counters

                rowsCounters[destinationFile] += 1
                totalRowsCounter += 1


if __name__ == '__main__':
    main()
