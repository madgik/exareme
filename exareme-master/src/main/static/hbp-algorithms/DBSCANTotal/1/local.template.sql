requirevars 'input_local_tbl' 'column1' 'column2';

--1. Local Query: Compute sums of values
--drop table if exists hospitalsums;
--create table hospitalsums as

select  __colname as colname,
        min(__val) as minvalue,
        max(__val) as maxvalue,
        FSUM(__val) as S1,
        FSUM(FARITH('*', __val, __val)) as S2,
        count(__val) as N
from (  select *
        from  %{input_local_tbl}
        where __val <> 'NA' and  __colname in ('%{column1}', '%{column2}') )
group by __colname;


