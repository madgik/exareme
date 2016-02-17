distributed create temporary table date_dim as virtual
select
    cast(c1 as int) as d_date_sk,
    cast(c2 as text) as d_date_id,
    cast(c3 as text) as d_date,
    cast(c4 as int) as d_month_seq,
    cast(c5 as int) as d_week_seq,
    cast(c6 as int) as d_quarter_seq,
    cast(c7 as int) as d_year,
    cast(c8 as int) as d_dow,
    cast(c9 as int) as d_moy,
    cast(c10 as int) as d_dom,
    cast(c11 as int) as d_qoy,
    cast(c12 as int) as d_fy_year,
    cast(c13 as int) as d_fy_quarter_seq,
    cast(c14 as int) as d_fy_week_seq,
    cast(c15 as text) as d_day_name,
    cast(c16 as text) as d_quarter_name,
    cast(c17 as text) as d_holiday,
    cast(c18 as text) as d_weekend,
    cast(c19 as text) as d_following_holiday,
    cast(c20 as int) as d_first_dom,
    cast(c21 as int) as d_last_dom,
    cast(c22 as int) as d_same_day_ly,
    cast(c23 as int) as d_same_day_lq,
    cast(c24 as text) as d_current_day,
    cast(c25 as text) as d_current_week,
    cast(c26 as text) as d_current_month,
    cast(c27 as text) as d_current_quarter,
    cast(c28 as text) as d_current_year
from (file '~/exareme/lib/tpcds/data/date_dim.tbl.gz' delimiter:| fast:1);

distributed create temporary table item as virtual
select
  cast(c1 as int) as i_item_sk,
  cast(c2 as text) as i_item_id,
  cast(c3 as text) as i_rec_start_date,
  cast(c4 as text) as i_rec_end_date,
  cast(c5 as text) as i_item_desc,
  cast(c6 as float) as i_current_price,
  cast(c7 as float) as i_wholesale_cost,
  cast(c8 as int) as i_brand_id,
  cast(c9 as text) as i_brand,
  cast(c10 as int) as i_class_id,
  cast(c11 as text) as i_class,
  cast(c12 as int) as i_category_id,
  cast(c13 as text) as i_category,
  cast(c14 as int) as i_manufact_id,
  cast(c15 as text) as i_manufact,
  cast(c16 as text) as i_size,
  cast(c17 as text) as i_formulation,
  cast(c18 as text) as i_color,
  cast(c19 as text) as i_units,
  cast(c20 as text) as i_container,
  cast(c21 as int) as i_manager_id,
  cast(c22 as text) as i_product_name
from ( file '~/exareme/lib/tpcds/data/item.tbl.gz' delimiter:| fast:1);

distributed create temporary table web_sales to 4 on ws_item_sk as external
select
  cast(c1 as int) as ws_sold_date_sk,
  cast(c2 as int) as ws_sold_time_sk,
  cast(c3 as int) as ws_ship_date_sk,
  cast(c4 as int) as ws_item_sk,
  cast(c5 as int) as ws_bill_customer_sk,
  cast(c6 as int) as ws_bill_cdemo_sk,
  cast(c7 as int) as ws_bill_hdemo_sk,
  cast(c8 as int) as ws_bill_addr_sk,
  cast(c9 as int) as ws_ship_customer_sk,
  cast(c10 as int) as ws_ship_cdemo_sk,
  cast(c11 as int) as ws_ship_hdemo_sk,
  cast(c12 as int) as ws_ship_addr_sk,
  cast(c13 as int) as ws_web_page_sk,
  cast(c14 as int) as ws_web_site_sk,
  cast(c15 as int) as ws_ship_mode_sk,
  cast(c16 as int) as ws_warehouse_sk,
  cast(c17 as int) as ws_promo_sk,
  cast(c18 as int) as ws_order_number,
  cast(c19 as int) as ws_quantity,
  cast(c20 as float) as ws_wholesale_cost,
  cast(c21 as float) as ws_list_price,
  cast(c22 as float) as ws_sales_price,
  cast(c23 as float) as ws_ext_discount_amt,
  cast(c24 as float) as ws_ext_sales_price,
  cast(c25 as float) as ws_ext_wholesale_cost,
  cast(c26 as float) as ws_ext_list_price,
  cast(c27 as float) as ws_ext_tax,
  cast(c28 as float) as ws_coupon_amt,
  cast(c29 as float) as ws_ext_ship_cost,
  cast(c30 as float) as ws_net_paid,
  cast(c31 as float) as ws_net_paid_inc_tax,
  cast(c32 as float) as ws_net_paid_inc_ship,
  cast(c33 as float) as ws_net_paid_inc_ship_tax,
  cast(c24 as float) as ws_net_profit
from ( file '~/exareme/lib/tpcds/data/web_sales.tbl.gz' delimiter:| fast:1);



distributed create temporary table partial_result as direct
select
  i_item_desc,
  i_category,
  i_class,
  i_current_price,
  i_item_id,
  sum(ws_ext_sales_price) as itemrevenue,
  sum(ws_ext_sales_price)*100/sum(sum(ws_ext_sales_price)) over (partition by i_class) as revenueratio
from
	web_sales,
	item,
	date_dim
where
	web_sales.ws_item_sk = item.i_item_sk
  and item.i_category in ('Jewelry', 'Sports', 'Books')
  and web_sales.ws_sold_date_sk = date_dim.d_date_sk
	and date_dim.d_date between '2001-01-12' and '2001-02-11'
	and ws_sold_date between '2001-01-12' and '2001-02-11'
group by
	i_item_id,
	i_item_desc,
	i_category,
	i_class,
	i_current_price
order by
	i_category,
	i_class,
	i_item_id,
	i_item_desc,
	revenueratio
limit 100;
