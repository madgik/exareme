
requirevars 'defaultDB' 'input_local_tbl' 'variable';


drop table if exists inputlocaltbl;
create table inputlocaltbl as
select __rid as rid, __colname as colname, tonumber(__val)  as val
from %{input_local_tbl}
where __colname = '%{variable}';

var 'categorical' from select case when (select count(distinct val) from inputlocaltbl)< 20 then "True" else "False" end;
var 'valIsText' from select case when (select typeof(val) from inputlocaltbl limit 1) ='text' then "True" else "False" end;


--drop table if exists output_local_table;
--create table output_local_table as

--1. case when  val is a categorical number
select * from (
select *
from ( select colname,
              val,
              val as minval,
              val as maxval,
              FSUM(val) as S1,
              FSUM(FARITH('*', val, val)) as S2,
              count(val) as N
       from ( select * from inputlocaltbl where '%{categorical}'='True' and '%{valIsText}'='False')
       where val <> 'NA' and val is not null and val <> ""
       group by val
)

union all
--2. case when val is a number but not categorical
select *
from ( select colname,
              val,
              min(val) as minval,
              max(val) as maxval,
              FSUM(val) as S1,
              FSUM(FARITH('*', val, val)) as S2,
              count(val) as N
       from ( select * from inputlocaltbl where '%{categorical}'='False' and '%{valIsText}'='False')
       where val <> 'NA' and val is not null and val <> ""
)

union all
--3. case when val is text
select *
from ( select colname,
              val,
              "NA" as minval,
              "NA" as maxval,
              1 as S1,
              1 as S2,
              count(val) as N
       from ( select * from inputlocaltbl where '%{valIsText}'='True')
       where val <> 'NA' and val is not null and val <> ""
       group by val
)

union all
--4. case when val is null
select *
from ( select colname,
              "NA" as val,
              "NA" as minval,
              "NA" as maxval,
               1 as S1,
               1 as S2,
              count(val) as N
       from inputlocaltbl
       where val is 'NA' or val is null or val == ""
)) where colname is not null or val is not null;
