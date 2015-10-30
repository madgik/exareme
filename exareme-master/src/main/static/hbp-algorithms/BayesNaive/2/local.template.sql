requirevars 'input_local_tbl' 'prv_output_global_tbl' 'classname' 'random_rid' 'random_colname' 'random_val';

create temporary table newdata as
select var('random_rid') as rid, var('random_colname') as colname, var('random_val') as val;

select classval, max(probability)
from (
select classval, FMULT(cast(quantity as float)/cast(totalquantity as float)) as probability
from (
select *
from (  select cc.classval as classval,newdata.val as val, quantity,totalquantity
        from %{prv_output_global_tbl} as cc, newdata,
          ( select classval, sum(quantity) as totalquantity
            from %{prv_output_global_tbl}
            where colname=var('classname')
            group by  val,colname) as c
       where cc.colname = newdata.colname and cc.val = newdata.val  and c.classval = cc.classval
       group by cc.classval,newdata.val
       union
       select d1.val as classval, 'total' as val, d1.quantity as quantity, d2.totalquantity as totalquantity
       from (select val, sum(quantity) as  quantity
             from %{prv_output_global_tbl}
             where colname=var('classname') group by val ) as d1,
            (select sum(quantity) as totalquantity
             from %{prv_output_global_tbl}
             where colname=var('classname')) as d2)
)
group by classval
);

