<b><h2><center>Hold Out Validation/center></h1></b>

<b><h4> Some General Remarks </h4></b>
The general architecture of the MIP follows a Master/Worker paradigm where many Workers, operating in multiple medical centers, are coordinated by one Master. Only Workers are allowed access to the anonymized data in each medical center and the Master only sees aggregate data, derived from the full data and sent to him by the Workers.

As a consequence, every algorithm has to be refactored in a form that fits this model.

In general, this means two things.

1. On the one hand, isolating the parts of the algorithm that operate on the full data and implement them in procedures that run on Workers.  
2. On the other hand, identifying the parts of the algorithm that need to see the aggregates from all Workers and implementing these parts in procedures that run on Master.

Our naming convention is that procedures run on Workers are given the adjective _local_ whereas those running on Master are called _global_.

<b><h4>Federated Hold Out Validation</b></h4>
In Hold-Out Validation the dataset is random partitioned into two parts, training set and validation set.
Federated Hold Out Validation runs on Workers and it provides train/test indices that split local datasets in train/test sets.
