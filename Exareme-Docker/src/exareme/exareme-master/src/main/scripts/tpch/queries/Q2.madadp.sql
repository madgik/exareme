distributed create temporary table n_supp to 1 as
select n_name, s_suppkey, s_acctbal, s_name, s_address, s_phone, s_comment
from
  region,
  nation,
  supplier
where r_name = 'AFRICA'
  and r_regionkey = n_regionkey
  and s_nationkey = n_nationkey;

distributed create temporary table n_supp_psupp as
select n_name, ps_suppkey, ps_partkey, ps_supplycost, s_acctbal, s_name, s_address, s_phone, s_comment
from
  partsupp,
  n_supp
where ps_suppkey = s_suppkey;

distributed create temporary table sub_min as
select
  min(ps_supplycost) as m,
  ps_partkey as pskey
from n_supp_psupp
group by ps_partkey;

distributed create temporary table n_supp_psupp_part as direct
select n_name, p_partkey, p_mfgr, ps_supplycost, s_acctbal, s_name, s_address, s_phone, s_comment
from
  part,
  n_supp_psupp
where p_partkey = ps_partkey
  and p_size = 7
  and p_type like '%STEEL';

distributed create temporary table result_partial to 1 as direct
select s_acctbal, s_name, n_name, p_partkey, p_mfgr, s_address, s_phone, s_comment
from
  n_supp_psupp_part,
  sub_min
where p_partkey = pskey
  and ps_supplycost = sub_min.m
order by
  s_acctbal desc,
  n_name,
  s_name,
  p_partkey
limit 100;

distributed create temporary table q2_result_8_temp as
select s_acctbal, s_name, n_name, p_partkey, p_mfgr, s_address, s_phone, s_comment
from result_partial
order by
  s_acctbal desc,
  n_name,
  s_name,
  p_partkey
limit 100;
