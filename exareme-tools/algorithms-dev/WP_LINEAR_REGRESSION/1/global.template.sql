
requirevars 'defaultDB' 'input_global_tbl' 'variable';
attach database '%{defaultDB}' as defaultDB;

-- drop table if exists defaultDB.globalstatistics;
-- create table defaultDB.globalstatistics as
select  colname,
        FARITH('/',S1A,NA) as avgvalue
from ( select colname,
              FSUM(S1) as S1A,
              SUM(N) as NA
        from %{input_global_tbl}
        group by colname );

-- select "ok";
