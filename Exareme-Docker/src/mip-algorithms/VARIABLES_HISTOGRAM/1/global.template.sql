requirevars 'input_global_tbl' 'column1' 'column2' 'nobuckets';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.localResult'; --DELETE

-------------------------------------------------------------------------------------
var 'column1IsText' from select min(column1IsText) from %{input_global_tbl};
var 'column1IsCategoricalNumber' from select min(column1IsCategoricalNumber) from %{input_global_tbl};
var 'column1IsCategorical' from select max(%{column1IsText},%{column1IsCategoricalNumber});


drop table if exists defaultDB.globalResult; --DELETE
create table defaultDB.globalResult as --DELETE
-- 1. case when column1 is not text
select * from (
select  colname,
        minvalue,
        maxvalue,
	N,
	%{column1IsCategorical} as column1IsCategorical
from ( select colname,
              min(minvalue) as minvalue,
              max(maxvalue) as maxvalue,
              sum(N) as N
       from %{input_global_tbl}
       where colname = '%{column1}') 
where colname = '%{column1}'
)
where %{column1IsCategorical}=0


union all 
 -- 2. case when column1 is text 
select null,null, null, null, %{column1IsCategorical} as column1IsCategorical
where %{column1IsCategorical}=1;

select * from defaultDB.globalResult; --DELETE 
