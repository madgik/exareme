requirevars 'defaultDB' 'input_local_tbl' 'columns' 'k';
attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.columnstable;
create table defaultDB.columnstable as
select strsplitv('%{columns}' ,'delimiter:+') as col;

drop table if exists defaultDB.inputlocaltbl;
create table defaultDB.inputlocaltbl as  select __rid as rid, __colname as colname, cast(__val as float) as val from %{input_local_tbl};

drop table if exists defaultDB.assignnearestcluster;
create table defaultDB.assignnearestcluster(rid  text primary key, clid, mindist);


--drop table if exists partialclustercentersnew;
--create table partialclustercentersnew as
select  hashmodarchdep2(rid, %{k}) as clid,
	    colname as clcolname,
        --avg(val) as clval
        sum(val) as clS,
        count(val) as clN
from ( select *
       from inputlocaltbl
       where colname in (select * from defaultDB.columnstable))
group by clid, clcolname;




---- Run Loop
--execnselect 'columns' 'k'
--select filetext('kmeanslooplocal.sql')
--from ( whilevt select min(diff)=0
--               from (  select clold.clval = clnew.clval as diff
--                       from clustercenters as clold,
--                            clustercentersnew as clnew
--                       where clold.clid = clnew.clid and clold.clcolname = clnew.clcolname)
-- );
--
--
--select clid as rid,
--       clcolname as colname,
--       clval as val,
--       clpoints as weight
--from clustercenters,
--     ( select clid as clid1, count(*) as clpoints
--       from assignnearestcluster
--       group by clid )
--where clid1 = clid;
--


