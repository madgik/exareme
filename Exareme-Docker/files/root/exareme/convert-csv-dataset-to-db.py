#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
This script creates multiple dbs for each pathology folder containing a dataset csv file and a metadata json file.
"""

import os
import sys
import csv
import sqlite3
import json
from argparse import ArgumentParser

# This metadata dictionary contains only code and sqltype so that processing will be faster
# It also includes the subjectcode
def createMetadataDictionary(CDEsMetadataPath):
    CDEsMetadata = open(CDEsMetadataPath)
    metadataJSON = json.load(CDEsMetadata)

    metadataDictionary = {}
    metadataDictionary['subjectcode'] = 'text'
    metadataDictionary = addGroupVariablesToDictionary(metadataJSON,
            metadataDictionary)
    return metadataDictionary


def addGroupVariablesToDictionary(groupMetadata, metadataDictionary):
    if 'variables' in groupMetadata:
        for variable in groupMetadata['variables']:
            if 'sql_type' not in variable:
                raise ValueError('The variable "' + variable['code'] + '" does not contain the sql_type field in the metadata.')
            metadataDictionary[variable['code']] = variable['sql_type']
    if 'groups' in groupMetadata:
        for group in groupMetadata['groups']:
            metadataDictionary = addGroupVariablesToDictionary(group,
                    metadataDictionary)
    return metadataDictionary


# This metadata list is used to create the metadata table. It contains all the knows information for each variable.
def createMetadataList(CDEsMetadataPath):
    CDEsMetadata = open(CDEsMetadataPath)
    metadataJSON = json.load(CDEsMetadata)

    metadataList = []
    metadataList = addGroupVariablesToList(metadataJSON,
            metadataList)
    return metadataList


def addGroupVariablesToList(groupMetadata, metadataList):
    if 'variables' in groupMetadata:
        for variable in groupMetadata['variables']:
            variableDictionary = {}
            variableDictionary['code'] = variable['code']
            if 'sql_type' not in variable:
                raise ValueError('The variable "' + variable['code'] + '" does not contain the sql_type field in the metadata.')
            variableDictionary['sql_type'] = variable['sql_type']
            if 'isCategorical' not in variable:
                raise ValueError('The variable "' + variable['code'] + '" does not contain the isCategorical field in the metadata.')
            variableDictionary['isCategorical'] = '1' if variable['isCategorical'] else '0'
            if variable['isCategorical'] and 'enumerations' not in variable:
                raise ValueError('The variable "' + variable['code'] + '" does not contain enumerations even though it is categorical.')
            if 'enumerations' in variable: 
                enumerations = []
                for enumeration in variable['enumerations']:
                    enumerations.append(unicode(enumeration['code']))
                variableDictionary['enumerations'] = ','.join(enumerations)

            else:
                variableDictionary['enumerations'] = 'null'
            if 'min' in variable:
                variableDictionary['min'] = variable['min']
            else:
                variableDictionary['min'] = 'null'
            if 'max' in variable:
                variableDictionary['max'] = variable['max']
            else:
                variableDictionary['max'] = 'null'
            metadataList.append(variableDictionary)
    if 'groups' in groupMetadata:
        for group in groupMetadata['groups']:
            metadataList = addGroupVariablesToList(group,
                    metadataList)
    return metadataList


def addMetadataInTheDatabase(CDEsMetadataPath, cur):
    # Transform the metadata json into a column name -> column type dictionary
    metadataList = createMetadataList(CDEsMetadataPath)

    # Create the query for the metadata table
    createMetadataTableQuery = 'CREATE TABLE METADATA('
    createMetadataTableQuery += ' code TEXT PRIMARY KEY ASC'
    createMetadataTableQuery += ', sql_type TEXT'
    createMetadataTableQuery += ', isCategorical INTEGER'
    createMetadataTableQuery += ', enumerations TEXT'
    createMetadataTableQuery += ', min INTEGER'
    createMetadataTableQuery += ', max INTEGER)'

    # Create the metadata table
    cur.execute('DROP TABLE IF EXISTS METADATA')
    cur.execute(createMetadataTableQuery)

    # Add data to the metadata table        TODO
    columnsQuery = 'INSERT INTO METADATA (code, sql_type, isCategorical, enumerations, min, max) VALUES ('

    for variable in metadataList:
        insertVariableQuery = columnsQuery
        insertVariableQuery += "'" + variable['code'] + "'"
        insertVariableQuery += ", '" + variable['sql_type'] + "'"
        insertVariableQuery += ", '" + variable['isCategorical'] + "'"
        insertVariableQuery += ", '" + variable['enumerations'] + "'"
        insertVariableQuery += ", '" + variable['min'] + "'"
        insertVariableQuery += ", '" + variable['max'] + "'"
        insertVariableQuery += ");"
        cur.execute(insertVariableQuery)


def createDataTable(metadataDictionary, cur):
    # Create the query for the sqlite data table
    createDataTableQuery = 'CREATE TABLE DATA('
    for column in metadataDictionary:
        createDataTableQuery += column + ' ' + metadataDictionary[column] + ', '
    # Remove the last comma
    createDataTableQuery = createDataTableQuery[:-2]
    createDataTableQuery += ')'

    # Create the data table
    cur.execute('DROP TABLE IF EXISTS DATA')
    cur.execute(createDataTableQuery)


def addCSVInTheDataTable(csvFilePath, metadataDictionary, cur):
    # Get the csv file name that will be the dataset name
    datasetName = os.path.splitext(os.path.basename(csvFilePath))[0]

    # Open the csv
    csvFile = open(csvFilePath, 'r')
    csvReader = csv.reader(csvFile)
    
    # Create the csv INSERT statement
    csvHeader = next(csvReader)
    columnsString = csvHeader[0]
    for column in csvHeader[1:]:
        columnsString += ', ' + column
    columnsQuery = 'INSERT INTO DATA (' + columnsString + ') VALUES ('

    # Insert data
    for row in csvReader:
        insertRowQuery = columnsQuery
        for (value, column) in zip(row, csvHeader):
            if metadataDictionary[column] == 'text':
                insertRowQuery += "'" + value + "', "
            elif value == '':
                insertRowQuery += 'null, '
            else:
                insertRowQuery += value + ", "
        insertRowQuery = insertRowQuery[:-2]
        insertRowQuery += ');'

        try:
            cur.execute(insertRowQuery)
        except:
            raise ValueError('Row: ' + str(row) + ', Query: ' + str(insertRowQuery) + ', could not be inserted in the database.')


def main():

    # Read the parameters
    parser = ArgumentParser()
    parser.add_argument('-f', '--pathologiesFolderPath', required=True,
                        help='The folder with the pathologies data.')
    parser.add_argument('-t', '--nodeType', required=True,
                        help='Is this a master or a worker node?'
                        )
    args = parser.parse_args()

    pathologiesFolderPath = os.path.abspath(args.pathologiesFolderPath)
    
    # Get all pathologies
    pathologiesList = next(os.walk(pathologiesFolderPath))[1]
     
    # Create the datasets db for each pathology
    for pathologyName in pathologiesList:
        
        # Initializing metadata and output absolute path
        CDEsMetadataPath = os.path.join(pathologiesFolderPath,pathologyName,"CDEsMetadata.json")
        outputDBAbsPath = os.path.join(pathologiesFolderPath,pathologyName,"datasets.db")

        # Connect to the database
        con = sqlite3.connect(outputDBAbsPath)
        cur = con.cursor()
        
        # Add the metadata table + rows
        addMetadataInTheDatabase(CDEsMetadataPath, cur)
        
        
        # Transform the metadata json into a column name -> column type list
        metadataDictionary = createMetadataDictionary(CDEsMetadataPath)
        
        # Create the data table with the header
        createDataTable(metadataDictionary, cur)
        
        # Add all the csvs in the database
        for csv in os.listdir(os.path.join(pathologiesFolderPath,pathologyName)):
            if csv.endswith('.csv'):
                csvFilePath = os.path.join(pathologiesFolderPath,pathologyName,csv)
                addCSVInTheDataTable(csvFilePath, metadataDictionary, cur)
                
        

        '''
        if os.path.isfile(csvFilePath):
            # Create the query for the sqlite data table

            createDataTableQuery = 'CREATE TABLE DATA('

            csvFile = open(csvFilePath, 'r')
            csvReader = csv.reader(csvFile)
            csvHeader = next(csvReader)
            subjectcode = csvHeader[0]
            createDataTableQuery += ' ' + subjectcode + ' TEXT'
            for column in csvHeader[1:]:
                if column not in variablesTypesDict:
                    raise ValueError('Column: "' + column + '" does not exist in the metadata file provided.')
                columnType = variablesTypesDict[column]
                createDataTableQuery += ', ' + column + ' ' + columnType
            createDataTableQuery += ')'

            # Create the data table
            cur.execute('DROP TABLE IF EXISTS DATA')
            cur.execute(createDataTableQuery)

            # Add data
            columnsString = csvHeader[0]
            for column in csvHeader[1:]:
                columnsString += ', ' + column
            columnsQuery = 'INSERT INTO DATA (' + columnsString + ') VALUES ('

            for row in csvReader:
                insertRowQuery = columnsQuery + "'" + row[0] + "'"
                for (value, column) in zip(row[1:], csvHeader[1:]):
                    if variablesTypesDict[column] == 'text':
                        insertRowQuery += ", '" + value + "'"
                    elif value == '':
                        insertRowQuery += ', null'
                    else:
                        insertRowQuery += ', ' + value
                insertRowQuery += ');'
                try:
                    cur.execute(insertRowQuery)
                except:
                    raise ValueError('Row: ' + str(row) + ', Query: ' + str(insertRowQuery))
        else:           # If datasets.csv does not exist.
            if args.nodeType == 'master':
                # Create the query for the sqlite data table from the metadata

                createDataTableQuery = 'CREATE TABLE DATA('
                for column, columnType in variablesTypesDict.iteritems():
                    createDataTableQuery += column + ' ' + columnType + ', '
                createDataTableQuery = createDataTableQuery[:-2]
                createDataTableQuery += ')'

                # Create the data table
                cur.execute('DROP TABLE IF EXISTS DATA')
                cur.execute(createDataTableQuery)
            else:
                raise IOError('The datasets.csv file does not exist for the ' + pathologyName + ' pathology')
        '''
        

    
        con.commit()
        con.close()


if __name__ == '__main__':
    main()