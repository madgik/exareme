requirevars 'k' 'columns';


drop table if exists columnstable;
create table columnstable as
select strsplitv('%{columns}' ,'delimiter:+') as col;



drop table if exists clustercenters;
alter table clustercentersnew rename to clustercenters;

--Assign Data to Nearest Cluster
delete from assignnearestcluster;
insert into assignnearestcluster
select  rid as rid,
        clid,
        min(dist) as mindist
from ( select rid, clid, sum( (val-clval) * (val-clval) ) as dist
       from ( select rid, colname, val, clid, clval
              from ( select * from inputlocaltbl where colname in (select * from columnstable) )
              join clustercenters
              where colname = clcolname )
       group by rid,clid )
group by rid;



-- Compute new cluster centers
create table clustercentersnew as
select clid,
       colname as clcolname,
       avg(val) as clval
from ( select * from inputlocaltbl where colname in (select * from columnstable) ) as h,
      assignnearestcluster
where  h.rid = assignnearestcluster.rid
group by clid, colname;

select 'OK';