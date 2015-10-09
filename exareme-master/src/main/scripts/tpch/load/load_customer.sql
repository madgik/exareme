distributed create table customer as virtual
select
  cast(c1 as int) as c_custkey,
  cast(c2 as text) as c_name,
  cast(c3 as text) as c_address,
  cast(c4 as int) as c_nationkey,
  cast(c5 as text) as c_phone,
  cast(c6 as float) as c_acctbal,
  cast(c7 as text) as c_mktsegment,
  cast(c8 as text) as c_comment
from (file '/home/alex/Datasets/tpch/customer.tbl.gz' delimiter:| fast:1);
