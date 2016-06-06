requirevars 'input_global_tbl';

var 'categorical' from select case when (select count(distinct val) from %{input_global_tbl})< 20 then "True" else "False" end;
var 'valIsText' from select case when (select typeof(val) from %{input_global_tbl} limit 1) ='text' then "True" else "False" end;

-----

select
  counts as "count",
  case when '%{valIsText}'='False' then FARITH('/',S1A,counts) else "0" end as "average",
  case when '%{valIsText}'='False' then minval else "0" end as "minval",
  case when '%{valIsText}'='False' then maxval else "0" end as "maxval",
  case when '%{valIsText}'='False' then SQROOT( FARITH('/', '-', '*', counts, S2A, '*', S1A, S1A, '*', counts, '-', counts, 1)) else "0" end as "std"
from ( select
          min(minval) as minval,
          max(maxval) as maxval,
          SUM(N) as counts,
          FSUM(S2) as S2A,
          FSUM(S1) as S1A
       from %{input_global_tbl}
       where val !='NA'
);
