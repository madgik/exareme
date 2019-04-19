#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
This script uses the csv file with the data and the metadata for the csv and it produces an sqlite DB.

TODO: Add the metadata inside the DB in a metadata table.
"""

import os
import sys
import csv
import sqlite3
import json
from argparse import ArgumentParser


def createMetadataDictionary(variablesMetadataPath):
    variablesMetadata = open(variablesMetadataPath)
    metadataJSON = json.load(variablesMetadata)

    metadataDictionary = {}
    metadataDictionary['rid'] = 'integer'
    metadataDictionary['row_id'] = 'text'
    metadataDictionary['subjectcode'] = 'text'
    metadataDictionary = addGroupVariablesToDictionary(metadataJSON,
            metadataDictionary)
    return metadataDictionary


def addGroupVariablesToDictionary(groupMetadata, metadataDictionary):
    if 'variables' in groupMetadata:
        for variable in groupMetadata['variables']:
            metadataDictionary[variable['code']] = variable['sql_type']
    if 'groups' in groupMetadata:
        for group in groupMetadata['groups']:
            metadataDictionary = addGroupVariablesToDictionary(group,
                    metadataDictionary)
    return metadataDictionary


def main():

    # Read the parameters

    parser = ArgumentParser()
    parser.add_argument('-c', '--csvFilePath', required=True,
                        help='The folder of the csv dataset.')
    parser.add_argument('-v', '--variablesMetadataPath', required=True,
                        help='The folder of the metadata file.')
    parser.add_argument('-o', '--outputDBAbsPath', required=True,
                        help='The folder where the output db file is going to be.'
                        )
    args = parser.parse_args()

    csvFilePath = os.path.abspath(args.csvFilePath)
    variablesMetadataPath = os.path.abspath(args.variablesMetadataPath)
    outputDBAbsPath = args.outputDBAbsPath

    # Transform the metadata json into a column name -> column type dictionary

    variablesTypesDict = createMetadataDictionary(variablesMetadataPath)

    # Create the query that will create the sqlite table

    createTableQuery = 'CREATE TABLE DATA('

    csvFile = open(csvFilePath, 'r')
    csvReader = csv.reader(csvFile)
    csvHeader = next(csvReader)
    rid = csvHeader[0]
    createTableQuery += ' ' + rid + ' INTEGER PRIMARY KEY ASC'
    for column in csvHeader[1:]:
        columnType = variablesTypesDict[column]
        createTableQuery += ', ' + column + ' ' + columnType
    createTableQuery += ')'

    # Create the table

    con = sqlite3.connect(outputDBAbsPath)
    cur = con.cursor()
    cur.execute('DROP TABLE IF EXISTS DATA')
    cur.execute(createTableQuery)

    # Add data

    columnsString = csvHeader[0]
    for column in csvHeader[1:]:
        columnsString += ', ' + column
    columnsQuery = 'INSERT INTO DATA (' + columnsString + ') VALUES ('

    for row in csvReader:
        insertRowQuery = columnsQuery + row[0] + ",'" + row[1] + "','" \
            + row[2] + "'"
        for (value, column) in zip(row[3:], csvHeader[3:]):
            if variablesTypesDict[column] == 'text':
                insertRowQuery += ", '" + value + "'"
            elif value == '':
                insertRowQuery += ', null'
            else:
                insertRowQuery += ', ' + value
        insertRowQuery += ');'
        cur.execute(insertRowQuery)

    con.commit()
    con.close()


if __name__ == '__main__':
    main()