requirevars 'defaultDB' 'input_local_tbl' 'variable' 'covariables' 'groupings';
attach database '%{defaultDB}' as defaultDB;

var 'y' from (select '%{variable}');


var 'x' from
(select group_concat(x,'+')
from (
select group_concat(x2,'*') as x from (select strsplitv('%{groupings}','delimiter:,') as x2)
union
select group_concat(x1,'+') as x from (select strsplitv('%{covariables}','delimiter:,') as x1)));


--E2. Compute rows, columns and SSE<--sum((y-ypredictive)^2)  (Local Layer)
hidden var 'partial_myrow' from select count(distinct rid) from defaultDB.input_local_tbl_LR_Final ;
hidden var 'mycol' from select count(distinct colname)-1 from defaultDB.input_local_tbl_LR_Final ;

hidden var 'partial_sst' from
select sum( (val-mean_observed_value)*(val-mean_observed_value))
from defaultDB.input_local_tbl_LR_Final,
     ( select avgvalue as mean_observed_value
       from defaultDB.globalstatistics
       where colname = "%{y}")
where colname = "%{y}";

drop table if exists myvariables;
create table myvariables as select "partial_myrow" as varname, var('partial_myrow') as varvalue;
insert into myvariables select "mycol" as varname, var('mycol') as varvalue;
insert into myvariables select "partial_sst" as varname, var('partial_sst') as varvalue;

select * from myvariables;

