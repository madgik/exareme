distributed create table partsupp to 3 on ps_partkey as virtual
select
  cast(c1 as int) as ps_partkey,
  cast(c2 as int) as ps_suppkey,
  cast(c3 as int) as ps_availqty,
  cast(c4 as float) as ps_supplycost,
  cast(c5 as text) as ps_comment
from (file '/home/exa/tpch-kit/datasets/partsupp.tbl.gz' delimiter:|);
