requirevars 'defaultDB'  'k';
attach database '%{defaultDB}' as defaultDB;

--kmeanslocalresult;
select clid as clid1, count(*) as clpoints
from defaultDB.assignnearestcluster
group by clid;
