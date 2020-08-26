requirevars 'defaultDB' 'input_global_tbl' 'y' ;
attach database '%{defaultDB}' as defaultDB;
----------------------------------------------------------------------------------------------------------
var 'PRIVACY_MAGIC_NUMBER' 10;  -- 0.5 == no privacy

-- Merge local_counts
drop table if exists globalcounts;
create temp table  globalcounts as
select  colname, val, classval, sum(quantity) as quantity
from %{input_global_tbl}
group by colname, classval,val;

--Compute gain
drop table if exists gain;
create temp table gain as
select colname, max(sumofentropies) from (
    select colname, sum(nentropy)/sum(n) as sumofentropies
    from( select colname, val, n,  sumnlong - n* pyfun('math.log', n ,2)  as nentropy
          from( select colname, val, sum(quantity) as n, sum(quantity * pyfun('math.log', quantity ,2)) as sumnlong
                from globalcounts
                group by colname, val )
          where colname!=var('y')
        )
    group by colname
);

var 'distinctvalues' from select group_concat(val) from (select distinct %{y} as val from defaultDB.localinputtblcurrent);
var 'noofcolumns' from select count(*) from (coltypes select * from defaultdb.localinputtblcurrent);



drop table if exists globalnewnodesoftree;
create temp table globalnewnodesoftree (no int, colname text, colval text, nextnode int, leafval text, samplesperclass text);
insert into  globalnewnodesoftree
select no,colname,colval,
case when minsampleperclass < %{PRIVACY_MAGIC_NUMBER} then "-" else nextnode end as nextnode,
case when minsampleperclass < %{PRIVACY_MAGIC_NUMBER} and leafval = "?" then classes ||" (Stopped due to privacy)" else leafval end as leafval,
samplesperclass
from
(
select no, colname, colval, nextnode, leafval, classes, samplesperclass, minsampleperclass
from ( select distinct colname, val as colval,
       case when count(*) == 1 then "-" else "?" end as nextnode,
       case when count(*) == 1 then classval else "?" end as  leafval
       from globalcounts where colname in (select colname from gain)
       group by colname,val),
     ( select case when no is null then '1' else cast(max(no)+1 as text)   end as no from defaultdb.globaltree),
     ( select val as val1,
       group_concat(classval) as classes,
       group_concat(classval||"="|| farith('/',quantity,totalquantity)) as samplesperclass,
       --case when min(quantity) < %{PRIVACY_MAGIC_NUMBER} then group_concat(classval) else group_concat(classval||"="||quantity/totalquantity) end as samplesperclass,
       --group_concat(classval||case when quantity <%{PRIVACY_MAGIC_NUMBER} then "<%{PRIVACY_MAGIC_NUMBER}" else "="||quantity end) as samplesperclass,
       min(quantity) as minsampleperclass,
       totalquantity
       from globalcounts,
           ( select colname as c1, val as v1, sum(quantity) as totalquantity
             from globalcounts where colname in (select colname from gain)  group by val)
       where colname in (select colname from gain) and colname = c1 and val = v1 group by val )
       where val1 = colval);

update globalnewnodesoftree
set leafval= '%{distinctvalues}' where leafval='?' and %{noofcolumns}=2;
select * from globalnewnodesoftree;

update defaultdb.globaltree set nextnode = (select no from globalnewnodesoftree)
where  jmerge(no,colname,colval) is (select jmerge (no,colname,colval) from defaultDB.globalpathforsplittree);

insert into defaultdb.globaltree select * from globalnewnodesoftree;

--3. Find the path in order to split the input dataset/table
drop table if exists defaultDB.globalpathforsplittree;
create table defaultDB.globalpathforsplittree as
pathtree value:? select * from defaultdb.globaltree;

select * from defaultDB.globalpathforsplittree;
