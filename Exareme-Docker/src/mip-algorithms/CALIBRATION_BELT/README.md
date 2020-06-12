## Calibration Belt

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

This is an original algorithm aiming to compute a confidence band for the calibration 
curve, in order to validate the predictive power of some model against independent 
samples. Here we follow the approach described in [1]. The algorithm is given 
samples from two variables as input, the *expected* outcome of some binary variable, 
obtained from the model we want to asses, and the actual observed outcome. Then the 
algorithm fits a series of calibration curves given by the *logit* of a polynomial in 
the *expected* variable (the degrees of the polynomial are constrained between 1 and 4). 
In the end the best fit is selected using a *likelihood-ratio test*. Finally, the 
confidence belt is derived from the variance of the logistic model (see eq.(9-10) of 
[1]).

![pseudo](pseudocode.png)

[1]: [**Calibration Belt for Quality-of-Care Assessment Based on Dichotomous Outcomes**, *S. Finazzi,  D. Poole, D. Luciani, P.E. Cogo, G. Bertolini*](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3043050/#pone.0016110.s001)  
