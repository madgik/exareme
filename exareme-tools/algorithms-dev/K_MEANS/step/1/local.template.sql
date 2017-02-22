requirevars 'defaultDB' 'input_local_tbl' 'columns' 'k';
attach database '%{defaultDB}' as defaultDB;


--drop table if exists defaultDB.clustercenters;
--alter table defaultDB.clustercentersnew rename to defaultDB.clustercenters;

--Assign Data to Nearest Cluster
delete from defaultDB.assignnearestcluster;
insert into defaultDB.assignnearestcluster
select  rid as rid,
        clid,
        min(dist) as mindist
from ( select rid, clid, sum( (val-clval) * (val-clval) ) as dist
       from ( select rid, colname, val, clid, clval
              from ( select * from defaultDB.inputlocaltbl where colname in (select * from defaultDB.columnstable) )
              join defaultDB.clustercenters
              where colname = clcolname )
       group by rid,clid )
group by rid;


-- Compute new cluster centers (partial)
--create table clustercentersnew as
select clid,
       colname as clcolname,
       --avg(val) as clval
       sum(val) as clS,
       count(val) as clN
from ( select * from defaultDB.inputlocaltbl where colname in (select * from defaultDB.columnstable) ) as h,
      defaultDB.assignnearestcluster
where  h.rid = assignnearestcluster.rid
group by clid, colname;






