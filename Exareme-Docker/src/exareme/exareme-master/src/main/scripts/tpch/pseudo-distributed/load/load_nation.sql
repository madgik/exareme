distributed create table nation as virtual
select
  cast(c1 as int) as n_nationkey,
  cast(c2 as text) as n_name,
  cast(c3 as int) as n_regionkey,
  cast(c4 as text) as n_comment
from (file '/home/vaggelis/tpch-kit/tpch/datasets/nation.tbl' delimiter:|);
