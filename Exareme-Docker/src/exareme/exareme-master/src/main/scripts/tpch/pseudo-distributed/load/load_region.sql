distributed create table region as virtual
select
  cast(c1 as int) as r_regionkey,
  cast(c2 as text) as r_name,
  cast(c3 as text) as r_comment
from (file '/home/vaggelis/tpch-kit/tpch/datasets/region.tbl' delimiter:|);
