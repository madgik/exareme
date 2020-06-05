## Pearson Correlation

#### Some General Remarks

The general architecture of the MIP follows a Master/Worker paradigm where many Workers
, operating in multiple medical centers, are coordinated by one Master. Only Workers 
are allowed access to the anonymized data in each medical center and the Master only 
sees aggregate data, derived from the full data and sent to him by the Workers.

As a consequence, every algorithm has to be refactored in a form that fits this model.  
In general, this means two things. 
1. On the one hand, isolating the parts of the algorithm that operate on the full data 
and implement them in procedures that run on Workers.  
2. On the other hand, identifying the parts of the algorithm that need to see the 
aggregates from all Workers and implementing these parts in procedures that run on 
Master.

Our naming convention is that procedures run on Workers are given the adjective _local_
whereas those running on Master are called _global_.

#### Algorithm Description

This algorithm computes the Pearson correlation coefficient between two vectors *x* and 
*y* using the eq.(1)

![pseudo](pseudocode.png)


