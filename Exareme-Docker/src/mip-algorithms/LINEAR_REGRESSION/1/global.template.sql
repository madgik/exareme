requirevars 'defaultDB' 'input_global_tbl';
attach database '%{defaultDB}' as defaultDB;

select  colname,
        FARITH('/',S1A,NA) as avgvalue
from ( select colname,
              FSUM(S1) as S1A,
              SUM(N) as NA
        from %{input_global_tbl}
        group by colname );
