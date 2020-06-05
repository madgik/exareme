## Logistic Regression

#### Some General Remarks

The general architecture of the MIP follows a Master/Worker paradigm where many Workers
, operating in multiple medical centers, are coordinated by one Master.  Only Workers 
are allowed access to the anonymized data in each medical center and the Master only 
sees aggregate data, derived from the full data and sent to him by the Workers.

As a consequence, every algorithm has to be refactored in a form that fits this model.  
In general, this means two things. 
1. On the one hand, isolating the parts of the algorithm that operate on the full data 
and implement them in procedures that run on Workers.  
2. On the other hand, identifying the parts of the algothm that need to see the 
aggregates from all Workers and implementing these parts in procedures that run on 
Master.

Our naming convention is that procedures run on Workers are given the adjective _local_
whereas those running on Master are called _global_.

#### Algorithm Description

Logistic Regression training is done by Maximum Likelihood Estimation (MLE) by gradient 
descent using, for example, Newton's method. Applying Newton's method leads to the 
following algorithm, called __Iteratively Reweighted Least Squares__ (IRLS).  Here 
the dependent variable *y* has to be binary.

![pseudo](pseudocode.png)

