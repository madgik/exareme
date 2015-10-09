distributed create table lineitem as virtual
select
  cast(c1 as int) as l_orderkey,
  cast(c2 as int) as l_partkey,
  cast(c3 as int) as l_suppkey,
  cast(c4 as int) as l_linenumber,
  cast(c5 as int) as l_quantity,
  cast(c6 as float) as l_extendedprice,
  cast(c7 as float) as l_discount,
  cast(c8 as float) as l_tax,
  cast(c9 as text) as l_returnflag,
  cast(c10 as text) as l_linestatus,
  cast(c11 as text) as l_shipdate,
  cast(c12 as text) as l_commitdate,
  cast(c13 as text) as l_receiptdate,
  cast(c14 as text) as l_shipinstruct,
  cast(c15 as text) as l_shipmode,
  cast(c16 as text) as l_comment
from (file '/home/exa/tpch-kit/datasets/lineitem.tbl.gz' delimiter:|);
