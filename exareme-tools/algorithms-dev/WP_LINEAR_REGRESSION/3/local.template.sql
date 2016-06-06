requirevars 'defaultDB' 'input_local_tbl' 'x' 'y';
attach database '%{defaultDB}' as defaultDB;

--E2. Compute rows, columns and SSE<--sum((y-ypredictive)^2)  (Local Layer)
hidden var 'partial_myrow' from select count(distinct rid) from defaultDB.input_local_tbl_LR_Final ;
hidden var 'mycol' from select count(distinct colname) as mycol from defaultDB.input_local_tbl_LR_Final ;
hidden var 'partial_sst' from
select sum( (val-mean_observed_value)*(val-mean_observed_value))
from defaultDB.input_local_tbl_LR_Final,
     ( select avg(val) as mean_observed_value
       from defaultDB.input_local_tbl_LR_Final
       where colname = "%{y}")
where colname = "%{y}";

drop table if exists myvariables;
create table myvariables as select "partial_myrow" as varname, var('partial_myrow') as varvalue;
insert into myvariables select "mycol" as varname, var('mycol') as varvalue;
insert into myvariables select "partial_sst" as varname, var('partial_sst') as varvalue;

select * from myvariables;

