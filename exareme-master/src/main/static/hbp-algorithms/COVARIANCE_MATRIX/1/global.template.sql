requirevars 'input_global_tbl' ;

select  colname,
        FARITH('/',S1A,NA) as avgvalue
from (  select colname, FSUM(S1) as S1A, SUM(N) as NA
        from %{input_global_tbl}
        group by colname);

