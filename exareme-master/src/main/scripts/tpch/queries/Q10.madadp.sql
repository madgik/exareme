distributed create temporary table ol_join to 8 on o_custkey as direct
select
  l_orderkey,
  o_orderkey,
  o_custkey,
  (l_extendedprice * (1 - l_discount)) as revenue_one
from
  lineitem,
  orders
where o_orderkey = l_orderkey
  and o_orderdate >= '1993-04-01'
  and o_orderdate < '1993-07-01'
  and l_returnflag = 'R';

distributed create temporary table nc_join as
select c_custkey, c_name, c_acctbal, n_name, c_address, c_phone, c_comment
from customer,
     nation
where c_nationkey = n_nationkey;

distributed create temporary table olnc_join to 1 as direct
select
  c_custkey,
  c_name,
  c_acctbal,
  n_name,
  c_address,
  c_phone,
  c_comment,
  sum(revenue_one) as revenue_PARTIAL
from
  ol_join,
  nc_join
where c_custkey = o_custkey
group by c_custkey, c_name, c_acctbal, c_phone, n_name, c_address, c_comment;

distributed create temporary table q10_result_8_temp as
select 
  c_custkey,
  c_name,
  sum(revenue_PARTIAL) as revenue,
  c_acctbal,
  n_name,
  c_address,
  c_phone,
  c_comment
from olnc_join
group by c_custkey, c_name, c_acctbal, c_phone, n_name, c_address, c_comment
order by revenue desc
limit 20;
