import sys
import csv, sqlite3
import json

def getParameters(argv):
    opts = {}
    while argv:
        if argv[0][0] == '-':
            opts[argv[0]] = argv[1]
            argv = argv[2:]
        else:
            argv = argv[1:]
    return opts

def createMetadataDictionary(variablesMetadataPath):
    variablesMetadata = open(variablesMetadataPath)
    metadataJSON = json.load(variablesMetadata)
    variables = metadataJSON['variables']
    dict = {}
    for variable in variables:
        if variable['code'] == 'neurodegenerativescategories':
            dict['neurogenerativescategories'] = variable['sql_type']
        else:
            dict[variable['code']] = variable['sql_type']
    dict['brainstem'] = 'real'
    dict['tiv'] = 'text'
    dict['rid'] = 'integer'
    dict['row_id'] = 'text'
    dict['subjectcode'] = 'text'
    return dict

# Read the parameters
parameters = getParameters(sys.argv[1:])
if not parameters or len(parameters) < 3:
    raise ValueError("There should be 3 parameters")

csvFilePath = parameters.get("-csvFilePath")
if csvFilePath == None :
    raise ValueError("-csvFilePath not provided as parameter.")

variablesMetadataPath = parameters.get("-variablesMetadataPath")
if variablesMetadataPath == None :
    raise ValueError("-variablesMetadataPath not provided as parameter.")

outputDBPath = parameters.get("-outputDBPath")
if outputDBPath == None :
    raise ValueError("-outputDBPath not provided as parameter.")

# Transform the metadata json into a column name -> column type dictionary
variablesTypesDict = createMetadataDictionary(variablesMetadataPath)

# Create the query that will create the sqlite table
createTableQuery = "CREATE TABLE DATA("

csvFile = open(csvFilePath,"r")
csvReader = csv.reader(csvFile)
csvHeader = next(csvReader)
rid = csvHeader[0]
createTableQuery += " " + rid + " INTEGER PRIMARY KEY ASC"
for column in csvHeader[1:]:
    columnType = variablesTypesDict[column]
    createTableQuery += ", " + column + " " + columnType
createTableQuery += ")"

# Create the table
con = sqlite3.connect(outputDBPath)
cur = con.cursor()
cur.execute("DROP TABLE IF EXISTS DATA") 
cur.execute(createTableQuery) 

# Add data
columnsString = csvHeader[0]
for column in csvHeader[1:]:
    columnsString += ", " + column
columnsQuery = "INSERT INTO DATA (" + columnsString + ") VALUES ("

for row in csvReader:
    insertRowQuery = columnsQuery + row[0] + ",'" + row[1] + "','" + row[2] + "'"
    for value, column in zip(row[3:],csvHeader[3:]):
        if variablesTypesDict[column] == 'text':
            insertRowQuery += ", '" + value + "'"
        elif value == '':
            insertRowQuery += ", null"
        else:
            insertRowQuery += ", " + value
    insertRowQuery += ");"
    cur.execute(insertRowQuery) 

con.commit()
con.close()