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
		csvFilePath = os.path.join(pathologiesFolderPath,pathologyName,"datasets.csv")
		CDEsMetadataPath = os.path.join(pathologiesFolderPath,pathologyName,"CDEsMetadata.json")
		outputDBAbsPath = os.path.join(pathologiesFolderPath,pathologyName,"datasets.db")

		# Transform the metadata json into a column name -> column type dictionary

		variablesTypesDict = createMetadataDictionary(CDEsMetadataPath)

		# Connect to the database
		con = sqlite3.connect(outputDBAbsPath)
		cur = con.cursor()

		if not os.path.isfile(csvFilePath) and args.nodeType != 'master':
			raise IOError('The datasets.csv file does not exist for the ' + pathologyName + ' pathology')
		
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

		# Transform the metadata JSON to a list
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

		# Add data to the metadata table		TODO
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

		con.commit()
		con.close()



if __name__ == '__main__':
    main()