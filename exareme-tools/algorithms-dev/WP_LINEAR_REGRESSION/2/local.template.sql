requirevars 'defaultDB' 'input_local_tbl' 'variable' 'covariables' 'groupings';
attach database '%{defaultDB}' as defaultDB;

var 'y' from (select '%{variable}');

var 'x' from
(select group_concat(x,'+')
from (
select group_concat(x2,'*') as x from (select strsplitv('%{groupings}','delimiter:,') as x2)
union
select group_concat(x1,'+') as x from (select strsplitv('%{covariables}','delimiter:,') as x1)));


------------------------------------------------------------------------------------------------------------------------
--
--drop table if exists defaultDB.input_local_tbl_LR_Final_normalized;
--create table defaultDB.input_local_tbl_LR_Final_normalized as
--select rid,
--        hosp.colname as colname,
--       (val - avgvalue)/stdvalue  as val
--from defaultDB.input_local_tbl_LR_Final as hosp,
--     defaultDB.globalstatistics as attrib_stats
--where hosp.colname = attrib_stats.colname and stdvalue!=0;
--
--
--drop table if exists defaultDB.partialgramian2;
--create table defaultDB.partialgramian2 as
--select gramian(rid,colname, cast (val as real)) from
--(select * from defaultDB.input_local_tbl_LR_Final_normalized order by rid, colname);

---------------------------------------------------------------------------------------------------------------









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


select rowid as rid1,e from defaultDB.residuals;


