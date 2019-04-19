distributed create table supplier to 4 on s_suppkey as external
select
  cast(c1 as int) as s_suppkey,
  cast(c2 as text) as s_name,
  cast(c3 as text) as s_address,
  cast(c4 as int) as s_nationkey,
  cast(c5 as text) as s_phone,
  cast(c6 as float) as s_acctbal,
  cast(c7 as text) as s_comment,
  cast(c8 as text) as s_emtpy
from (file '/home/vaggelis/tpch-kit/tpch/datasets/supplier.tbl' delimiter:|);
