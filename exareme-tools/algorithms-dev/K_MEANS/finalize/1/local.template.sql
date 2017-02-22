requirevars 'defaultDB' 'input_local_tbl' 'columns' 'k';
attach database '%{defaultDB}' as defaultDB;

select clid as clid1, count(*) as clpoints
from defaultDB.assignnearestcluster
group by clid;