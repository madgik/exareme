distributed create temporary table t1 to %2$s on eid as external
select
  cast(c1 as int) as eid,
  cast(c2 as text) as ename,
  cast(c3 as int) as age,
  cast(c4 as float) as salary
from file('%3$s');

distributed create temporary table t2 to %2$s on eid as external
select
  cast(c1 as int) as eid,
  cast(c2 as text) as ename,
  cast(c3 as int) as age,
  cast(c4 as float) as salary
from file('%3$s');


distributed create table %1$s to 1 as direct
select *
from t1, t2
where t1.eid = t2.eid;

