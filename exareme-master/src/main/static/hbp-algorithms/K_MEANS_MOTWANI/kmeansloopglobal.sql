requirevars  'column1' 'column2' 'k' ;

drop table if exists clustercenters;
alter table clustercentersnew rename to clustercenters;

--Assign Data to Nearest Cluster
delete from assignnearestcluster;
insert into assignnearestcluster
select  rid as rid, clid, min(dist) as mindist
from ( select rid, clid, weight * sum( (val-clval) * (val-clval) ) as dist
       from ( select rid, colname, val, weight, clid, clval
              from ( select * from eavdatatable where colname in ('%{column1}', '%{column2}') )
              join clustercenters
              where colname=clcolname )
       group by rid,clid )
group by rid;



-- Compute new cluster centers
create table clustercentersnew as
select clid,
       colname as clcolname,
       avg(val) as clval
from ( select * from eavdatatable where colname in ('%{column1}', '%{column2}') ) as h,
      assignnearestcluster
where  h.rid = assignnearestcluster.rid
group by clid, colname;

select 'OK';