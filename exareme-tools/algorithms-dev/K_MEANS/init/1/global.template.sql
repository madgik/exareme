requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;


drop table if exists defaultDB.clustercentersnew;
create table defaultDB.clustercentersnew as
select  clid,
        clcolname,
        sum(clS)/sum(clN) as clval
from %{input_global_tbl}
group by clid, clcolname;

drop table if exists defaultDB.clustercenters;
create table defaultDB.clustercenters as select * from defaultDB.clustercentersnew;
update defaultDB.clustercenters set clval = clval-1;

select "ok";
