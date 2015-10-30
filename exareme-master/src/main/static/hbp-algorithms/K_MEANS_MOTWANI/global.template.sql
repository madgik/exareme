requirevars 'input_global_tbl' 'k' 'column1' 'column2';

drop table if exists eavdatatable;
create table eavdatatable as
select (__local_id) * %{k} + rid as rid, colname, val, weight
from %{input_global_tbl};

drop table if exists clustercentersnew;
create table clustercentersnew as
select  hashmodarchdep(rid, %{k}) as clid,
	    colname as clcolname,
        avg(val) as clval
from ( select * from eavdatatable where colname in ('%{column1}', '%{column2}') )
group by clid, clcolname;


drop table if exists clustercenters;
create table clustercenters as select * from clustercentersnew;
update clustercenters set clval = clval-1;



drop table if exists assignnearestcluster;
create table assignnearestcluster(rid integer primary key, clid, mindist);


-- Run Loop
execnselect 'k' 'column1' 'column2'
 select filetext('kmeansloopglobal.sql')
 from ( whilevt select min(diff)=0
                from (  select clold.clval = clnew.clval as diff
                        from clustercenters as clold, clustercentersnew as clnew
                        where clold.clid = clnew.clid and clold.clcolname = clnew.clcolname
                      )
 );

select clid as rid,
      clcolname as colname,
      clval as val,
      weight as noofpoints
from clustercenters,
    ( select rid1, clid1, sum(weight) as weight
      from ( select distinct e.rid as rid1, a.clid as clid1, e.weight as weight
             from eavdatatable as e,
                  assignnearestcluster as a
             where a.rid = e.rid
           )
      group by clid1
    )
where clid1 = clid;
