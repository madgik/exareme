distributed create temporary table hospital_sums as
select
  colname,
  FSUM(S2) as S2A,
  FSUM(S1) as S1A,
  SUM(N) as NA
from hospital_partial_sums
group by colname;

distributed create table attr_stats as
select
  colname,
  FARITH('/',S1A,NA) as avgvalue,
  SQROOT(
    FARITH('/', '-', '*', NA, S2A, '*', S1A, S1A, '*', NA, '-', NA, 1)
  ) as stdvalue
from hospital_sums;
