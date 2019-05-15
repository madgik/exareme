requirevars 'defaultDB'  'y' ;
attach database '%{defaultDB}' as defaultDB;

--E2. Compute rows, columns and SSE<--sum((y-ypredictive)^2)  (Local Layer)
hidden var 'partial_myrow' from setschema 'c1' select count(distinct rid) from defaultDB.input_local_tbl_LR_Final ;
hidden var 'mycol' from setschema 'c1' select max(count(distinct colname)-1,0) from defaultDB.input_local_tbl_lr_final ;
hidden var 'partial_sst' from setschema 'c1'
select sum( (val-mean_observed_value)*(val-mean_observed_value))
from input_local_tbl_LR_Final,
     ( select avgvalue as mean_observed_value
       from globalstatistics
       where colname = '%{y}')
where colname = '%{y}';

drop table if exists myvariables;
create table myvariables as select 'partial_myrow' as varname, %{partial_myrow} as varvalue;
insert into myvariables select 'mycol' as varname, case when %{mycol} is null then 0 else %{mycol}  end as varvalue;	
insert into myvariables select 'partial_sst' as varname, case when var('partial_sst') is null then 0 else var('partial_sst') end as varvalue;

select * from myvariables;

