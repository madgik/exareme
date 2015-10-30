hidden var 'k' 2;

--A. Initialize clusters : clid|key|val =(cluster id, measurement name, mean value)

-- Run Local Query:
create table partial_clustercenters as
select  HASHMD5MOD(rid, cast( var('k') as int)) as clid,
	colname as clcolname,
        sum(val) as clS,
        count(val) as clN
from (  select *
        from hospital
        where colname = 'Frontal_Sup_R_469' or colname = 'Pallidum_L_4854')
group by clid, clcolname;


-- Run Global Query:
attach database 'hospital1.db' as h1;
attach database 'hospital2.db' as h2;

create table clustercenters as
select  clid,
        clcolname,
        sum(clS)/sum(clN) as clval
from (  select * from h1.partial_clustercenters
        union all
        select * from h2.partial_clustercenters)
group by clid, clcolname;


--B. Compute min distance and assign new centers  : rid | clid | mindist =( patient id, cluster id, min distance)

-- Run Local Query:
attach database 'centralnode.db' as cn;

drop table if exists partial_assignnearestcluster;
create table partial_assignnearestcluster as
select  rid, clid, min(dist) as mindist
from (
     	select rid, clid, sum(valD * valD) as dist
        	from (
            		select rid, colname, clid, val-clval as valD
            		from hospital
            		join cn.clustercenters
            		where colname=clcolname
        	     )
        	group by rid,clid
          )
group by rid;

--C. Update step: Assign new centers in the clusters

-- Run Local Query
drop table if exists partial_clustercenters ;
create table partial_clustercenters as
select  clid,
       	colname as clcolname,
       	sum(val) as clS,
        count(val) as clN
from ( 	select *
	from hospital
	where colname = 'Frontal_Sup_R_469' or colname = 'Pallidum_L_4854' ) as h,
    partial_assignnearestcluster
where    h.rid =partial_assignnearestcluster.rid
group by clid, colname;


-- Run Global Query
attach database 'hospital1.db' as h1;
attach database 'hospital2.db' as h2;

create table clustercentersnew as
select  clid,
        clcolname,
        sum(clS)/sum(clN) as clval
from ( select * from h1.partial_clustercenters
       union all
       select * from h2.partial_clustercenters)
group by clid, clcolname;


--D. Check if converenge
-- Run global query
select distinct (diff)
from (
       	select    clnew.clid,
                  clnew.clcolname,
                  clold.clval = clnew.clval  as diff
       	from  	  clustercenters as clold,
                  clustercentersnew as clnew
       	where   clold.clid = clnew.clid and
clold.clcolname = clnew.clcolname
          );


--E. Rename tables...
drop table if exists clustercenters;
alter table clustercentersNew rename to clustercenters;

