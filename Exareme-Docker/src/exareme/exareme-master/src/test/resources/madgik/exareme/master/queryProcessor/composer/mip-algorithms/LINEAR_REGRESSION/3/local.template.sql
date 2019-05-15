requirevars 'defaultDB' 'prv_output_global_tbl' 'y';
attach database '%{defaultDB}' as defaultDB;

--E. Compute statistics For Estimators ( standardError ,  tvalue  , p value )
--E1. Compute residuals y-ypredictive = Y-sum(X(i)*estimate(i)) (Local Layer)
drop table if exists defaultDB.residuals;
create table defaultDB.residuals as
select rid1, observed_value - predicted_value as e
from ( select rid as rid1, sum(val*estimate) as predicted_value
       from defaultDB.input_local_tbl_LR_Final, %{prv_output_global_tbl}
       where colname = attr1
       group by rid ),
     ( select rid as rid2, val as observed_value
        from defaultDB.input_local_tbl_LR_Final
       where colname = "%{y}" )
where rid1=rid2;

select min(e) as min_e, max(e) as max_e , sum(e) as sum_e,sum(e*e) as sum_ee, count(e) as counte from defaultDB.residuals;

--select rowid as rid1,e from defaultDB.residuals;
