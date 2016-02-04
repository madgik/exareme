requirevars 'input_global_tbl' ;

select  colname,
        FARITH(NA) as counts,
        min(minval) as mivalue,
        max(maxval) as maxvalue,
        FARITH(S1A) as sumvalue,
        FARITH('/',S1A,NA) as avgvalue,
        SQROOT( FARITH('/', '-', '*', NA, S2A, '*', S1A, S1A, '*', NA, '-', NA, 1)) as stdvalue
from (  select colname, FSUM(S2) as S2A, FSUM(S1) as S1A, SUM(N) as NA
        from %{input_global_tbl}
        group by colname
);
