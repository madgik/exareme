distributed create temporary table crn1 to 1 as
select c_custkey
from nation n1,
     region,
     customer
where r_name = 'MIDDLE EAST'
  and n_regionkey = r_regionkey
  and c_nationkey = n1.n_nationkey;

distributed create temporary table crn1o as
select o_orderkey,
       strftime('%Y', o_orderdate) as o_year
from orders,
     crn1
where o_custkey = c_custkey
  and o_orderdate > '1995-01-01'
  and o_orderdate < '1996-12-31';

distributed create temporary table crn1ol as direct
select l_suppkey,
       l_partkey,
       o_year,
       l_extendedprice * (1 - l_discount) as volume
from lineitem,
     crn1o
where l_orderkey = o_orderkey;

distributed create temporary table part_f to 1 as
select p_partkey
from part
where p_type = 'PROMO ANODIZED STEEL';

distributed create temporary table all_nations to 1 as
select 
  o_year,
  n2.n_name as nation,
  sum(case
      when n2.n_name = 'IRAN' then volume
      else 0
    end) as iran_sum_partial,
  sum(volume) as sum_partial
from crn1ol,
     part_f,
     supplier,
     nation n2
where s_suppkey = l_suppkey
  and s_nationkey = n2.n_nationkey
  and p_partkey = l_partkey
group by o_year, nation;

distributed create temporary table q8_result_8_temp as
select o_year,
  sum(case
    when nation = 'IRAN' then iran_sum_partial
    else 0
  end) / sum(sum_partial) as mkt_share
from all_nations
group by
  o_year
order by
  o_year;
