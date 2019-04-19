distributed create table hospital as external
select
  cast(c1 as int) as rid,
  cast(c2 as text) as colname,
  cast(c3 as int) as val
from file('%1$s');
