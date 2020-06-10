requirevars 'defaultDB' 'y' 'prv_output_global_tbl';
attach database '%{defaultDB}' as defaultDB;

--E1. Compute residuals y-ypredictive = Y-sum(X(i)*estimate(i)) (Local Layer)
var 'a' from select tabletojson(attr1,estimate,"attr1,estimate",0) from %{prv_output_global_tbl} where tablename ="coefficients";
var 'grandmean' from select mean as mean_observed_value from %{prv_output_global_tbl} where tablename ="statistics" and colname = '%{y}';

drop table if exists residuals;
create temp table residuals as
residualscomputation coefficients:%{a} y:%{y} select * from input_local_tbl_LR_Final;
hidden var 'partial_sse' from select sum(val*val) from residuals;

hidden var 'partial_sst' from
select sum( (%{y}-%{grandmean})*(%{y}-%{grandmean}))
from defaultdb.localinputtblflat;

-- localsss;
select '%{partial_sst}' as sst,'%{partial_sse}' as sse;
