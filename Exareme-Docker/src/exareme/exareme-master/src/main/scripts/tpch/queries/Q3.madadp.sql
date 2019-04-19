distributed create temporary table c_select to 1 as
select c_custkey
from customer
where c_mktsegment = 'BUILDING';

distributed create temporary table co_join as
select o_orderdate, o_orderkey, o_shippriority
from orders,
     c_select
where o_orderdate < '1995-03-15'
  and c_custkey = o_custkey;

distributed create temporary table col_join to 1 as direct
select l_orderkey, sum(l_extendedprice * (1 - l_discount)) as revenue,
       o_orderdate, o_shippriority
from lineitem,
     co_join
where o_orderkey = l_orderkey
  and l_shipdate > '1995-03-15'
group by l_orderkey, o_orderdate, o_shippriority
order by revenue desc, o_orderdate
limit 10;

distributed create temporary table q3_result_8_temp as
select l_orderkey, revenue, o_orderdate, o_shippriority
from col_join
order by  revenue desc, o_orderdate
limit 10;
