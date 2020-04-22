# Input Requirements

This documentation describes the input that exareme requires from the csv files and the CDEsMetadata.

## CDEsMetadata:

The CDEsMetadata (Common Data Elements Metadata) is a json file that is used to get information for the variables inside the csv files.

The metadata file should follow these rules:
<ul>
	<li>It should follow a tree structure. The <code>variables</code> lists are the leafs and the <code>groups</code> lists are the branches where one or more <code>variables</code> lists can exist.</li>
	<li>A <code>variable</code> inside the <code>variables</code> list must have these fields:
		<ul>
			<li><b>code</b> (Variable name)</li>
			<li><b>isCategorical</b> (true/false)</li>
			<li><b>sql_type</b> (TEXT, REAL, INT)</li>
		</ul>
	</li>
	<li>It can also contain:
		<ul>
			<li><b>min</b> (Integer)</li>
			<li><b>max</b> (Integer)</li>
			<li><b>enumerations</b> (List of codes)</li>
		</ul>
	</li>
</ul>
An example can be seen here: https://github.com/madgik/exareme/blob/master/Exareme-Docker/src/mip-algorithms/tests/data/dementia/CDEsMetadata.json .


## CSV Files

As input we can have one or more csvs. The csv file should contain rows that are used as input for the engine. A csv file should follow these rules:
<ul><li>The csv file should contain at least one row that contains the variable names, corresponding to the following rows.</li>
	<li>It should also contain a subjectcode variable, with the id of each record,</li>
	<li>and the dataset column, that declares in which dataset the row belongs to.</li>
	<li>All the variable names should exist in the metadata file, except from the <code>subjectcode</code> and <code>dataset</code>.</li>
<ul>