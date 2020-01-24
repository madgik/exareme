requirevars 'defaultDB' 'input_global_tbl' 'y' ;
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'localcounts';
----------------------------------------------------------------------------------------------------------
var 'PRIVACY_MAGIC_NUMBER' 10;  -- 0.5 == no privacy
var 'x' CL_age,CL_spectacle_prescrip,CL_astigmatism,CL_tear_prod_rate;
var 'y' CL_contact_lenses;

-- Merge local_counts
drop table if exists defaultDB.globalcounts;
create table  defaultDB.globalcounts as
select  colname, val, classval, sum(quantity) as quantity
from %{input_global_tbl}
group by colname, classval,val;

--Compute gain
drop table if exists defaultDB.gain;
create table defaultDB.gain as
select colname, max(sumofentropies) from (
    select colname, sum(nentropy)/sum(n) as sumofentropies
    from( select colname, val, n,  sumnlong - n* pyfun('math.log', n ,2)  as nentropy
          from( select colname, val, sum(quantity) as n, sum(quantity * pyfun('math.log', quantity ,2)) as sumnlong
                from defaultDB.globalcounts
                group by colname, val )
          where colname!=var('y')
        )
    group by colname
);

var 'distinctvalues' from select group_concat(val) from (select distinct %{y} as val from defaultDB.localinputtblcurrent);
var 'noofcolumns' from select count(*) from (coltypes select * from defaultdb.localinputtblcurrent);



drop table if exists defaultdb.globalnewnodesoftree;
create table defaultdb.globalnewnodesoftree (no int, colname text, colval text, nextnode int, leafval text, samplesperclass text);
insert into  defaultdb.globalnewnodesoftree
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
       from defaultdb.globalcounts where colname in (select colname from gain)
       group by colname,val),
     ( select case when no is null then '1' else cast(max(no)+1 as text)   end as no from defaultdb.globaltree),
     ( select val as val1,
       group_concat(classval) as classes,
       group_concat(classval||"="|| farith('/',quantity,totalquantity)) as samplesperclass,
       --case when min(quantity) < %{PRIVACY_MAGIC_NUMBER} then group_concat(classval) else group_concat(classval||"="||quantity/totalquantity) end as samplesperclass,
       --group_concat(classval||case when quantity <%{PRIVACY_MAGIC_NUMBER} then "<%{PRIVACY_MAGIC_NUMBER}" else "="||quantity end) as samplesperclass,
       min(quantity) as minsampleperclass,
       totalquantity
       from defaultdb.globalcounts,
           ( select colname as c1, val as v1, sum(quantity) as totalquantity
             from defaultdb.globalcounts where colname in (select colname from defaultdb.gain)  group by val)
       where colname in (select colname from defaultdb.gain) and colname = c1 and val = v1 group by val )
       where val1 = colval);



 select colname as c1, val as v1, sum(quantity) as totalquantity from globalcounts where colname in (select colname from gain)  group by val;


--
-- --2. Find new nodes of tree and update global_tree
-- drop table if exists defaultDB.globalnewnodesoftree;
-- create table defaultDB.globalnewnodesoftree (no int, colname text, colval text, nextnode int, leafval text, samplesperclass text);
-- insert into  globalnewnodesoftree
-- select no, colname, colval, nextnode, leafval,1
-- from ( select distinct colname, val as colval,
--        case when count(*) = 1 then "-" else "?" end as nextnode,
--        case when count(*) == 1 then classval else "?" end as  leafval
--        from defaultDB.globalcounts where colname in (select colname from defaultDB.gain)
--        group by colname,val),
--      ( select case when no is null then '1' else cast(max(no)+1 as text)   end as no from defaultdb.globaltree );
--
-- select * from defaultDB.globalnewnodesoftree;


-- drop table if exists defaultDB.globalnewnodesoftree;
-- create table defaultDB.globalnewnodesoftree (no int, colname text, colval text, nextnode int, leafval text, samplesperclass text);
-- insert into  defaultDB.globalnewnodesoftree
-- select no, colname, colval, nextnode, leafval, samplesperclass
-- from ( select distinct colname, val as colval,
--       case when count(*) == 1 then "-" else "?" end as nextnode,
--       case when count(*) == 1 then classval else "?" end as  leafval
--       from defaultDB.globalcounts where colname in (select colname from defaultDB.gain)
--       group by colname,val),
--     ( select case when no is null then '1' else cast(max(no)+1 as text)   end as no from defaultDB.globaltree),
--     ( select val as val1, group_concat(classval||"="||quantity) as samplesperclass from defaultDB.globalcounts where colname in (select colname from defaultDB.gain) group by val)
--     where val1 = colval;


--2. Find new nodes of tree and update global_tree
-- drop table if exists defaultDB.globalnewnodesoftree;
-- create table defaultDB.globalnewnodesoftree (no int, colname text, colval text, nextnode int, leafval text, samplesperclass text);
-- insert into  defaultDB.globalnewnodesoftree
-- select no,colname,colval,
-- case when minsampleperclass < %{PRIVACY_MAGIC_NUMBER} then "-" else nextnode end as nextnode,
-- case when minsampleperclass < %{PRIVACY_MAGIC_NUMBER} then classes else nextnode end as leafval,
-- samplesperclass
-- from (
-- select no, colname, colval, nextnode, leafval, classes, samplesperclass, minsampleperclass
-- from ( select distinct colname, val as colval,
--        case when count(*) == 1 then "-" else "?" end as nextnode,
--        case when count(*) == 1 then classval else "?" end as  leafval
--        from defaultDB.globalcounts where colname in (select colname from defaultDB.gain)
--        group by colname,val),
--      ( select case when no is null then '1' else cast(max(no)+1 as text)   end as no from defaultDB.globaltree),
--      ( select val as val1,
--        group_concat(classval) as classes,
--        group_concat(classval||case when quantity <%{PRIVACY_MAGIC_NUMBER} then "<%{PRIVACY_MAGIC_NUMBER}" else "="||quantity end) as samplesperclass,
--        min(quantity) as minsampleperclass
--        from defaultDB.globalcounts where colname in (select colname from defaultDB.gain)  group by val)
--        where val1 = colval);


--select * from defaultDB.globalnewnodesoftree;

update defaultDB.globalnewnodesoftree
set leafval= '%{distinctvalues}' where leafval='?' and %{noofcolumns}=2;
select * from defaultDB.globalnewnodesoftree;

update defaultdb.globaltree set nextnode = (select no from defaultDB.globalnewnodesoftree)
where  jmerge(no,colname,colval) is (select jmerge (no,colname,colval) from defaultDB.globalpathforsplittree);

insert into defaultdb.globaltree select * from defaultDB.globalnewnodesoftree;

--3. Find the path in order to split the input dataset/table
drop table if exists defaultDB.globalpathforsplittree;
create table defaultDB.globalpathforsplittree as
pathtree value:? select * from defaultdb.globaltree;

select * from defaultDB.globalpathforsplittree;
