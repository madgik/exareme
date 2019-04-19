distributed create temporary table filtered_hospital as direct
select *
from hospital
where val<>'NA';

distributed create table hospital_partial_sums as direct
select
  colname,
  FSUM(FARITH('*', val, val)) as S2,
  FSUM(val) as S1,
  count(val) as N
from filtered_hospital
group by  colname;
