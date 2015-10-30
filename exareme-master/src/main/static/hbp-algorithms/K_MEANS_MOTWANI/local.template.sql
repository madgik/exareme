requirevars 'input_local_tbl' 'column1' 'column2' 'k';
--hidden var 'input_local_tbl'  '(postgres h:127.0.0.1 port:5432 u:postgres p:postgres.2015 db:chuv_mips select cast(substring(brain_feature_set_id from 6) as INT) as rid, feature_name as colname, cast(tissue1_volume as FLOAT) as val from brain_feature)';
--hidden var 'column1' 'Lingual_L';
--hidden var 'column2' 'Amygdala_L';
--hidden var 'k' 4;

drop table if exists inputlocaltbl;
create table inputlocaltbl as  select __rid as rid, __colname as colname, cast(__val as float) as val from %{input_local_tbl};

drop table if exists clustercentersnew;
create table clustercentersnew as
select  hashmodarchdep2(rid, %{k}) as clid,
	    colname as clcolname,
        avg(val) as clval
from ( select *
       from inputlocaltbl
       where colname in ('%{column1}', '%{column2}') )
group by clid, clcolname;


drop table if exists clustercenters;
create table clustercenters as select * from clustercentersnew;
update clustercenters set clval = clval-1;


drop table if exists assignnearestcluster;
create table assignnearestcluster(rid  text primary key, clid, mindist);


-- Run Loop
execnselect 'column1' 'column2' 'k'
select filetext('kmeanslooplocal.sql')
from ( whilevt select min(diff)=0
               from (  select clold.clval = clnew.clval as diff
                       from clustercenters as clold,
                            clustercentersnew as clnew
                       where clold.clid = clnew.clid and clold.clcolname = clnew.clcolname)
 );


select clid as rid,
       clcolname as colname,
       clval as val,
       clpoints as weight
from clustercenters,
     ( select clid as clid1, count(*) as clpoints
       from assignnearestcluster
       group by clid )
where clid1 = clid;