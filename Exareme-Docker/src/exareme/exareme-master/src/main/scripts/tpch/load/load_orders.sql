distributed create table orders to 3 on o_orderkey as virtual
select
  cast(c1 as int) as o_orderkey,
  cast(c2 as int) as o_custkey,
  cast(c3 as text) as o_orderstatus,
  cast(c4 as float) as o_totalprice,
  cast(c5 as text) as o_orderdate,
  cast(c6 as int) as o_orderpriority,
  cast(c7 as text) as o_clerk,
  cast(c8 as int) as o_shippriority,
  cast(c9 as text) as o_comment
from (file '/home/exa/tpch-kit/datasets/orders.tbl.gz' delimiter:|);
