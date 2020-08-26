requirevars 'defaultDB' 'input_global_tbl' 'y';
attach database '%{defaultDB}' as defaultDB;

drop table if exists gramian;
create temp table gramian as
select attr1,attr2, sum(val) as val, sum(reccount) as reccount
from %{input_global_tbl}
where tablename = "gramian"
group by attr1,attr2;

drop table if exists statistics;
create temp table statistics as
select  colname, S1A/NA as mean, NA as N
from ( select colname, sum(S1) as S1A,sum(N) as NA
       from %{input_global_tbl}
       where tablename = "statistics"
       group by colname );

--------------------------------------------------------------------------------------------
--D. COMPUTE b estimators (X'X)^-1 * X'y = b  (GLOBAL LAYER)
--D1. Create X'X table
drop table if exists XTX;
create temp table XTX as
select attr1, attr2, val from gramian where attr1 != "%{y}" and  attr2 != "%{y}" order by attr1,attr2;

--D2. Invert table (X'X)^-1
drop table if exists defaultDB.XTXinverted;
create table defaultDB.XTXinverted as
select invertarray(attr1,attr2,val,sizeofarray)
from ( select attr1,attr2,val, sizeofarray
       from XTX, (select count(distinct attr1) as sizeofarray from XTX ));

--D3. Create X'y table
drop table if exists XTy;
create temp table XTy as
select attr2 as attr,val from gramian where attr1 = "%{y}" and attr1!=attr2
order by attr;

--D4 COMPUTE b estimators (X'X)^-1 * X'y = b
drop table if exists defaultDB.coefficients;
create table defaultDB.coefficients as
select  attr1, sum(XTXinverted.val*XTy.val) as estimate from
defaultDB.XTXinverted
join
XTy
on attr2 = attr
group by attr1;

drop table if exists globalcoefficientsandstatistics;
create temp table globalcoefficientsandstatistics (tablename text, attr1 text,estimate real, colname text, mean real);

insert into globalcoefficientsandstatistics
select "coefficients" as tablename, attr1, estimate, null, null
from defaultDB.coefficients;

insert into globalcoefficientsandstatistics
select "statistics" as tablename, null, null, colname, mean
from statistics;

select * from globalcoefficientsandstatistics;
