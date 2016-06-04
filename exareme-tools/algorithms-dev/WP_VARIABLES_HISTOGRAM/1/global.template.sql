requirevars 'input_global_tbl' 'column1' 'column2' 'nobuckets';
-- attach database '%{defaultDB}' as defaultDB;

select  colname,
        minvalue,
        maxvalue,
        FARITH('/',S1A,NA) as avgvalue,
        SQROOT( FARITH('/', '-', '*', NA, S2A, '*', S1A, S1A, '*', NA, '-', NA, 1)) as stdvalue
from ( select colname,
              min(minvalue) as minvalue,
              max(maxvalue) as maxvalue,
              FSUM(S1) as S1A,
              FSUM(S2) as S2A,
              SUM(N) as NA
       from %{input_global_tbl}
       where colname = '%{column1}'
);
