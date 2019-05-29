requirevars  'defaultDB' 'input_global_tbl' 'y';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl'  'defaultDB.partialhistogramresults';

drop table if exists histresult;
create table histresult as
select grouping,id,val, minval,maxval,sum(num) as totalsum
from %{input_global_tbl}
group by grouping,id;

var 'enumerations' from select enumerations from %{prv_output_global_tbl} where code =='%{y}';
highchartsbasiccolumn enumerations:%{enumerations} title:Histogram ytitle:Count select * from histresult;
