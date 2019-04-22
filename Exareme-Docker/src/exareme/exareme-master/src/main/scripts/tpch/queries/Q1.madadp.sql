distributed create temporary table lineitem_partial to 1 as
select 
  l_returnflag,
  l_linestatus,
  sum(l_quantity) as sum_qty_PARTIAL,
  sum(l_extendedprice) as sum_base_price_PARTIAL,
  sum(l_extendedprice * (1 - l_discount)) as sum_disc_price_PARTIAL,
  sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) as sum_charge_PARTIAL,
  sum(l_quantity) as avg_qty_SUM,
  sum(l_extendedprice) as avg_price_SUM,
  sum(l_discount) as avg_disc_SUM,
  count(*) as count_order_PARTIAL
from 
  lineitem
where 
  l_shipdate <= '1998-12-01'
group by
	l_returnflag,
	l_linestatus;

distributed create temporary table q1_result_8_temp as
select
	l_returnflag,
	l_linestatus,
	sum(sum_qty_PARTIAL) as sum_qty,
	sum(sum_base_price_PARTIAL) as sum_base_price,
	sum(sum_disc_price_PARTIAL) as sum_disc_price,
	sum(sum_charge_PARTIAL) as sum_charge,
	(sum(avg_qty_SUM) / sum(count_order_PARTIAL)) as avg_qty,
	(sum(avg_price_SUM) / sum(count_order_PARTIAL)) as avg_price,
	(sum(avg_disc_SUM) / sum(count_order_PARTIAL)) as avg_disc,
	sum(count_order_PARTIAL) as count_order
from
	lineitem_partial
group by
	l_returnflag,
	l_linestatus
order by
	l_returnflag,
	l_linestatus;
