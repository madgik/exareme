requirevars  'defaultDB' 'input_global_tbl';
attach database '%{defaultDB}' as defaultDB;


--var 'input_global_tbl'  'defaultDB.localResult'; --DELETE

drop table if exists histresult;
create table histresult as
select colname0, id0, minvalue0, maxvalue0, colname1, id1,val, sum(num) as total
from  (select colname0,id0,minvalue0,maxvalue0,colname1,id1,val,num from %{input_global_tbl})
group by colname0, id0, minvalue0, maxvalue0, colname1, val
order by val,id0;


select histogramresultsviewerpoc(colname0, id0, minvalue0, maxvalue0, colname1, id1, val, total) from histresult;

