distributed create temporary table ns_supp to 1 as
select 
  n1.n_name as supp_nation,
  s_suppkey
from 
  supplier,
  nation n1
where s_nationkey = n1.n_nationkey
  and (n1.n_name = 'IRAN' or n1.n_name = 'VIETNAM');

distributed create temporary table lns_supp_join as
select 
  supp_nation,
  l_orderkey,
  l_suppkey,
  strftime('%Y', l_shipdate) as l_year,
  (l_extendedprice * (1 - l_discount)) as volume_one
from 
  lineitem,
  ns_supp
where s_suppkey = l_suppkey
  and l_shipdate > '1995-01-01'
  and l_shipdate < '1996-12-31';

distributed create temporary table ns_cust to 1 as
select 
  n2.n_name as cust_nation,
  c_custkey
from 
  customer,
  nation n2
where c_nationkey = n2.n_nationkey
  and (n2.n_name = 'IRAN' or n2.n_name = 'VIETNAM');

distributed create temporary table ons_cust_join as
select 
  cust_nation,
  o_orderkey,
  o_custkey
from 
  orders,
  ns_cust
where c_custkey = o_custkey;

distributed create temporary table olnnsc_join to 1 as direct
select 
  supp_nation,
  cust_nation,
  l_year,
  sum(volume_one) as volume_partial
from 
  lns_supp_join,
  ons_cust_join
where l_orderkey = o_orderkey
group by
  supp_nation,
  cust_nation,
  l_year;

distributed create temporary table q7_result_8_temp as
select 
  supp_nation,
  cust_nation,
  l_year,
  sum(volume_partial) as revenue
from olnnsc_join
where supp_nation <> cust_nation
group by
  supp_nation,
  cust_nation,
  l_year
order by
  supp_nation,
  cust_nation,
  l_year;
