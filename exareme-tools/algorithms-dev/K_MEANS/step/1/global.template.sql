requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;


drop table if exists defaultDB.clustercentersnew;
create table defaultDB.clustercentersnew as
select  clid,
        clcolname,
        sum(clS)/sum(clN) as clval
from %{input_global_tbl}
group by clid, clcolname;

select "ok";