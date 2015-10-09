distributed create temporary table orders_filtered to 8 on o_custkey as
select o_custkey, o_orderkey
from orders
where o_orderdate >= '1998-01-01'
  and o_orderdate < '1995-01-01';

distributed create temporary table oc_join to 8 on o_orderkey as direct
select o_orderkey, c_custkey, c_nationkey
from orders_filtered,
     customer
where o_custkey = c_custkey;

distributed create temporary table ocl_join to 8 on l_suppkey as direct
select 
    l_suppkey,
	(l_extendedprice * (1 - l_discount)) as revenue_one,
    c_nationkey
from lineitem,
     oc_join
where l_orderkey = o_orderkey;

distributed create temporary table nrs_join to 8 on s_suppkey as
select n_name, s_suppkey, s_nationkey
from region,
     nation,
     supplier
where r_name = 'MIDDLE EAST'
  and n_regionkey = r_regionkey
  and s_nationkey = n_nationkey;

distributed create temporary table nrscol_join to 1 as direct
select n_name,
       sum(revenue_one) as revenue_partial
from ocl_join,
     nrs_join
where l_suppkey = s_suppkey
  and c_nationkey = s_nationkey
group by n_name;

distributed create temporary table q5_result_8_temp as
select
  n_name,
  sum(revenue_partial) as revenue
from nrscol_join
group by n_name
order by revenue desc;
