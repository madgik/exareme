<b><h2><center>k-Fold Cross Validation/center></h1></b>

<b><h4> Some General Remarks </h4></b>
The general architecture of the MIP follows a Master/Worker paradigm where many Workers, operating in multiple medical centers, are coordinated by one Master. Only Workers are allowed access to the anonymized data in each medical center and the Master only sees aggregate data, derived from the full data and sent to him by the Workers.

As a consequence, every algorithm has to be refactored in a form that fits this model.

In general, this means two things.

1. On the one hand, isolating the parts of the algorithm that operate on the full data and implement them in procedures that run on Workers.  
2. On the other hand, identifying the parts of the algorithm that need to see the aggregates from all Workers and implementing these parts in procedures that run on Master.

Our naming convention is that procedures run on Workers are given the adjective _local_ whereas those running on Master are called _global_.


<b><h4>Federated k-Fold Cross Validation</b></h4>
In k-Fold Cross Validation the dataset is random partitioned into k consecutive folds. Each fold is then used once as a validation while the k - 1 remaining folds form the training set.
Federated k-Fold Cross Validation runs on Workers and it provides train/test indices that split local datasets in training/testing sets.
