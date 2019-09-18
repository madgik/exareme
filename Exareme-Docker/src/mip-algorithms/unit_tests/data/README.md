In that folder you will find a sub-folder called ```CSVs``` containing data for running the Experiments. You will also find a .json file called
```CDEsMetadata.json``` containing all the metadata of the data, important -among others- for the pre-execution of the Experiments.

In order for you to add a dataset in the whole process, you need to add it inside folder ```CSVs``` and add its metadata in CDEsMetadata.json

The python script called ```splitCSVFiles.py``` will split the datasets in n datasets one for each node you have.