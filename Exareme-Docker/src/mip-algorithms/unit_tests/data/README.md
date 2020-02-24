In that folder you will find sub-folders for *each pathology* containing data (CSVs) for running the Experiments. In each pathology folder you will also find a .json file called
```CDEsMetadata.json``` containing all the metadata of the data, important -among others- for the pre-execution of the Experiments. 

In order for you to add a dataset in the whole process, you need to add it inside the *pathology folder* and add its metadata in CDEsMetadata.json

The python script called ```splitCSVFiles.py``` will split the datasets in N datasets one for each node you have. Run:
```python3 ./splitCSVFiles.py -p *N* -fp *pathology*/ -mp *pathology*/CDEsMetadata.json``` for splitting the datasets existing in each pathology, where *N* the number of splits and *pathology* the pathology folder, in that case *dementia*,*tbi*.

The ```pathologies.json``` is used from the front end and it is created from the pathologies, their metadata and the datasets available.
The creation of it could be automated in the future.
