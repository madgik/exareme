requirevars 'defaultDB' 'columns' 'k';
attach database '%{defaultDB}' as defaultDB;


drop table if exists defaultDB.kmeanslocalresult;
create table defaultDB.kmeanslocalresult as
select clid as clid1, count(*) as clpoints
from defaultDB.assignnearestcluster
group by clid;

select * from defaultDB.kmeanslocalresult;
