In this folder you will find sub-folders for each *pathology* containing data (CSVs) for running the Experiments.<br/>
In each pathology folder you will also find a .json file called
```CDEsMetadata.json``` containing all the metadata of the data, important -among others- for the pre-execution of the Experiments. 

In order for you to add a dataset in the whole process, you need to add it inside the *pathology folder* and add its metadata in the corresponding ```CDEsMetadata.json```.

Then, you have to **split the datasets** of a *pathology* in *N* datasets, one for each node you have in your cluster.<br/>
To achieve this, you can run:<br/>
```python3 ./splitCSVFiles.py -p *N* -fp *pathology*/ -mp *pathology*/CDEsMetadata.json```<br/>
In the above command, *N* is the number of splits and *pathology* is the *pathology folder*, for example: *dementia*, *tbi*.<br/>
The output of this command, is a folder named ```output```, which contains the created datasets (worker1.csv, worker2.csv, etc.).<br/>
<br/>
Then, you have to **create** a *pathology folder* inside the ```data_path``` of each of your nodes, **copy** the corresponding output-dataset there and **rename** it to "datasets.csv".<br/>
For example, the "worker1.csv" in your 1st node, the "worker2.csv" in your 2nd node, etc.<br/>
(The ```data_path``` is the one you specified in your [```hosts.ini```](https://github.com/LSmyrnaios/exareme/blob/kubernetes/Federated-Deployment/Documentation/Optionals.md#optional-initialize-hosts) file.)
<br/>
Lastly, you have to **copy** the ```*pathology*/CDEsMetadata.json``` file in each of your nodes, inside the ```data_path/pathology``` folder.<br/>
<br/>
After running your cluster, you may click [here](https://github.com/LSmyrnaios/exareme/blob/kubernetes/Federated-Deployment/Documentation/Troubleshoot.md#check-that-all-workers-are-seen-by-exareme),
for a way to check that all workers and their datasets, are seen by Exareme.
