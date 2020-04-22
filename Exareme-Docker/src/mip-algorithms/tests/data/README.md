In this folder you will find sub-folders for *each pathology* containing data (CSVs) for running the Experiments.<br/>
In each pathology folder you will also find a .json file called
```CDEsMetadata.json``` containing all the metadata of the data, important -among others- for the pre-execution of the Experiments. 

In order for you to add a dataset in the whole process, you need to add it inside the *pathology folder* and add its metadata in the corresponding ```CDEsMetadata.json```.

Then, you have to split the datasets of a *pathology* in *N* datasets, one for each node you have in your cluster.<br/>
To achieve this, you can run:<br/>
```python3 ./splitCSVFiles.py -p *N* -fp *pathology*/ -mp *pathology*/CDEsMetadata.json```<br/>
In the above command, *N* is the number of splits and *pathology* is the *pathology folder*, for example: *dementia*, *tbi*.<br/>
The output is a folder named ```output```, which contains the created datasets.<br/>

The ```pathologies.json``` is used from the front-end and it is created from the pathologies, their metadata and the datasets available.
The creation of it could be automated in the future.
