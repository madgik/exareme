distributed create temporary table ns_join as
select
  n_name as nation,
  s_suppkey
from
  supplier,
  nation
where s_nationkey = n_nationkey;

distributed create temporary table pps_join to 1 as direct
select
  p_partkey,
  ps_suppkey,
  ps_partkey,
  ps_supplycost
from
  partsupp,
  part_8
where ps_partkey = p_partkey
  and p_name like '%sky%';

distributed create temporary table ppsnsl_join as
select
  nation,
  l_orderkey,
  l_partkey,
  p_partkey,
  (l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity) as amount
from
  lineitem,
  pps_join,
  ns_join
where l_suppkey = ps_suppkey
  and l_suppkey = s_suppkey
  and l_partkey = ps_partkey;

distributed create temporary table ppsnslo_join to 1 as direct
select
  nation,
  strftime('%Y', o_orderdate) as o_year,
  sum(amount) as sum_profit_partial
from
  ppsnsl_join,
  orders
where o_orderkey = l_orderkey
  and p_partkey = l_partkey
group by
  nation,
  o_year;

distributed create temporary table q9_result_8_temp as
select 
  nation,
  o_year,
  sum(sum_profit_partial) as sum_profit
from ppsnslo_join
group by nation, o_year
order by nation, o_year desc;
