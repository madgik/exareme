README
======

Repository :
---------------------

* Each directory corresponds to an algorithm 
  and includes templates queries based on the algorithm type :
    * Global-Local (gl): Local and global queries are madql queries.
    * Multiple Global-Local (mgl) : .

* On Local queries you can use the default environment variables :
    * '__input_local_tbl'   - input eav table.
    * '__rid'               - column.
    * '__colname'           - column.
    * '__val'               - column.
   
* On global queries you can use default environment variables :
    * '__input_global_tbl'  - the union result table of local queries.
    * '__local_id'          - auto increament integer in order to be able to  distinct endpoints.
    
* On mgl type local queries you can use also :
    * '__output_global_tbl'  - the previus result of the global query.
 
 Algorithms:
---------------------
 
* Global-Local :
    * Standard Deviation
    * Covariance matrix
    * Linear Regression
    * KMeans Motwani
 
* Multiple Global-Local:
 
    * BayesNaive
    * DBScan

  

