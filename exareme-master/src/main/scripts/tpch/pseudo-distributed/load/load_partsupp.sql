distributed create table partsupp to 4 on ps_partkey as external
select
  cast(c1 as int) as ps_partkey,
  cast(c2 as int) as ps_suppkey,
  cast(c3 as int) as ps_availqty,
  cast(c4 as float) as ps_supplycost,
  cast(c5 as text) as ps_comment
from (file '/home/vaggelis/tpch-kit/tpch/datasets/partsupp.tbl' delimiter:|);
