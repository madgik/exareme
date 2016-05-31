requirevars 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

--4. Global Query: Scott method
--create table attr_scotts as
drop table if exists globalstats;
create table defaultDB.globalstats as
select  colname,

        minvalue,
        maxvalue,

        NA,
        FARITH('/',S1A,NA) as avgvalue,
        SQROOT( FARITH('/', '-', '*', NA, S2A, '*', S1A, S1A, '*', NA, '-', NA, 1)) as stdvalue,

        3.5 *SQROOT( FARITH('/', '-', '*', NA, S2A, '*', S1A, S1A, '*', NA, '-', NA, 1)) / pyfun('math.pow',NA,1.0/3.0) as step

from ( select colname,
              min(minvalue) as minvalue,
              max(maxvalue) as maxvalue,
              FSUM(S1) as S1A,
              FSUM(S2) as S2A,
              SUM(N) as NA
        from %{input_global_tbl}
        group by colname );


select *
from defaultDB.globalstats;


