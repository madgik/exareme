distributed create table %1$s to %2$s on eid as
select
  cast(c1 as int) as eid,
  cast(c2 as text) as ename,
  cast(c3 as int) as age,
  cast(c4 as float) as salary
from file('%3$s');