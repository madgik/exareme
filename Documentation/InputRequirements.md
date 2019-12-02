# Input Requirements

This documentation describes the input that exareme requires from the dataset csv files and the CDEsMetadata.

## CDEsMetadata:

The CDEsMetadata (Common Data Elements Metadata) is a json file that is used to get information for the variables inside the csv files.

The metadata file should follow these rules:
<ul>
	<li>Exareme source code (src/exareme).</li>
	<li>The mip algorithms that run on exareme (src/mip-algorithms).</li>
	<li>Scripts for creating a docker image with the exareme source code and the mip-algorithms that will run on exareme.</li>
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
An example can be seen here: https://github.com/madgik/exareme/blob/dev_multiple_csvs/Exareme-Docker/src/mip-algorithms/unit_tests/data/dementia/CDEsMetadata.json .


## CSV Files

The csv files should contain the datasets. A csv file should follow these rules:
<ul><li>The csv file should contain one or more dataset rows. </li>
	<li>The first row should contain the variable names, corresponding to the following rows.</li>
	<li>It should also contain a subjectcode variable, with the id of each record,</li>
	<li>and the dataset column, that declares in which dataset the row belongs to.</li>
	<li>All the variable names should exist in the metadata file, apart from <code>subjectcode</code> and <code>dataset</code>.</li>
<ul>