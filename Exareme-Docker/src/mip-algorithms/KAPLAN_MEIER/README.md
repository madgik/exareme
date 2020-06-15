## Kaplan-Meier Estimator 

#### Some General Remarks

The general architecture of the MIP follows a Master/Worker paradigm where many Workers
, operating in multiple medical centers, are coordinated by one Master. Only Workers 
are allowed access to the anonymized data in each medical center and the Master only 
sees aggregate data, derived from the full data and sent to him by the Workers. These
aggregates are defined as a scalar computed from a set of records of size at least
equal to some `privacy_threshold` equal to 10 in our case.

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

The Kaplan-Meier estimator is a non-parametric statistic, used to estimate the survival 
function for longitudinal data. It is useful for tracking the number of occurrences of 
an event over time in some population. It might track, for example, the evolution of 
the onset of Alzheimer’s disease in some initially healthy population. In the 
current implementation there is the possibility to compute multiple survival functions 
for subpopulations grouped by some trait, e.g. some genetic marker. The user selects 
one categorical variable and one categorical covariable. The occurrence of a particular
value of the categorical variable is used to signal the occurrence of some event (*e.g.* 
the onset of a disease) and the categories of the covariable correspond to the population
sub-groups.

Given our privacy constraints (only anonymized aggregates are sent to Master) this is an 
approximate version of the original algorithm. The difference is that, in order to fulfill
the aggregation contract ([see here](https://github.com/madgik/exareme/tree/master/Exareme-Docker/src/mip-algorithms/KAPLAN_MEIER#some-general-remarks)), every Worker send only aggregated timeline information about the events
occurrences. This means that the event occurrence information is grouped into groups of
size `privacy_threshold` and only one timestamp is reported, the last in the group, 
with all events in the group appearing simultaneously. This makes it impossible to discern 
single events but at the same time reduces the amount of information present in the final result.

Once the aggregated timelines are sent to Master they are given to a python library called 
`lifelines` which then computes the Kaplan-Meier estimator for the survival curve. 

![pseudo](pseudocode.png)

