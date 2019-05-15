requirevars 'defaultDB' 'input_global_tbl' 'y';
attach database '%{defaultDB}' as defaultDB;

--C2. (GLOBAL LAYER)
drop table if exists gramian;
create table gramian as
select attr1,attr2, sum(val) as val, sum(reccount) as reccount
from %{input_global_tbl}
group by attr1,attr2;

--------------------------------------------------------------------------------------------
--D. COMPUTE b estimators (X'X)^-1 * X'y = b  (GLOBAL LAYER)
--D1. Create X'X table
drop table if exists XTX;
create table XTX as
select *
from ( select attr1, attr2, val
       from gramian
       where attr1 != "%{y}" and  attr2 != "%{y}"
       union all
       select attr2 as attr1,attr1 as attr2, val
       from gramian
       where attr1 != "%{y}" and  attr2 != "%{y}" and attr1!=attr2
     )
order by attr1,attr2;

--D2. Invert table (X'X)^-1
drop table if exists defaultDB.XTXinverted;
create table defaultDB.XTXinverted as
select invertarray(attr1,attr2,val,sizeofarray)
from ( select attr1,attr2,val, sizeofarray
       from XTX,
       ( select count(distinct attr1) as sizeofarray from XTX ));

--D3. Create X'y table
drop table if exists XTy;
create table XTy as
select *
from (select attr2 as attr,val from gramian where attr1 = "%{y}" and attr1!=attr2
      union all
      select attr1 as attr, val from gramian where  attr2 = "%{y}" and attr1!=attr2)
order by attr;

--D4 COMPUTE b estimators (X'X)^-1 * X'y = b
drop table if exists defaultDB.coefficients;
create table defaultDB.coefficients as
select  attr1, sum(XTXinverted.val*XTy.val) as estimate from
XTXinverted
join
XTy
on attr2 = attr
group by attr1;
select * from defaultDB.coefficients;
