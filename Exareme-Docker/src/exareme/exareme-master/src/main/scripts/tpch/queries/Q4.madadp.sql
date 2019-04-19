distributed create temporary table sub_exists as
select distinct l_orderkey
from lineitem
where l_commitdate < l_receiptdate;

distributed create temporary table result_partial to 1 as direct
select o_orderpriority, count(l_orderkey) as order_count_partial
from 
  orders,
  sub_exists
where l_orderkey = o_orderkey
  and o_orderdate >= '1993-03-01'
  and o_orderdate < '1993-06-01'
group by
  o_orderpriority;

distributed create temporary table q4_result_8_temp as
select o_orderpriority, sum(order_count_partial) as order_count
from result_partial
group by
  o_orderpriority
order by
  o_orderpriority;
