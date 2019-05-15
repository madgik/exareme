requirevars  'defaultDB' 'model'  'iterationNumber' ;
attach database '%{defaultDB}' as defaultDB;

--var 'iterationNumber' 0;
--var 'model' from select tabletojson(colname,val,classval,average,sigma,probability, "colname,val,classval,average,sigma,probability")  as model from defaultdb.global_probabilities;

var 'classname' from select val from defaultDB.local_inputvariables where  variablename = 'classname';

drop table if exists defaultDB.local_probabilities;
create table defaultDB.local_probabilities as  select jsontotable('%{model}');

drop table if exists defaultDB.testingset;
create table defaultDB.testingset as
select rid,colname,val,idofset from defaultDB.local_inputTBL
where idofset in (select %{iterationNumber});

var 'file' from select  'Testingset'||%{iterationNumber}||'.csv';
output '%{file}' header:t fromeav select rid,colname,val from defaultDB.testingset;

drop table if exists defaultDB.tempprobabilities;
create table defaultDB.tempprobabilities as
--For the non categorical values: Compute probability density function for the normal distribution
select rid,testing_table.colname,testing_table.val, probabilities_table.classval, normpdf(testing_table.val,average,sigma) as probability
from defaultDB.testingset as testing_table ,defaultDB.local_probabilities as probabilities_table
where probabilities_table.colname = testing_table.colname
and probabilities_table.colname <> var('classname')
and probabilities_table.colname in (select colname1 from defaultDB.local_variablesdatatype_Existing where categorical <> 'Yes')
union
--For the categorical values:
select testing_table.rid as rid ,testing_table.colname as colname ,testing_table.val as val, classval, probability
from (select * from defaultDB.testingset
               where colname <> var('classname')
			   and   colname in (select colname1
			                     from defaultDB.local_variablesdatatype_Existing
								 where categorical ='Yes')) as testing_table,
	defaultDB.local_probabilities as probabilities_table
where probabilities_table.colname = testing_table.colname and probabilities_table.val = testing_table.val
union
--For the colname="classname"
select rid,colname,val,classval,probability from
((select distinct rid from defaultDB.testingset)
 cross join
(select * from defaultDB.local_probabilities as probabilities_table where colname = var('classname')));

--select * from defaultDB.tempprobabilities order by rid, classval;

drop table if exists defaultDB.tempposteriorprobability; -- Mono ton arithmith ypologizw. Arkei gia na kanw thn sugkrish
create table defaultDB.tempposteriorprobability as
select rid,classval,FMULT(probability) as probability
from defaultDB.tempprobabilities
group by rid,classval;

drop table if exists defaultDB.posteriorprobability; -- Mono ton arithmith ypologizw. Arkei gia na kanw thn sugkrish
create table defaultDB.posteriorprobability as
select rid,classval,probability/totalprobability as probability
from defaultDB.tempposteriorprobability,
     (select rid as rid1,sum(probability) as totalprobability from defaultDB.tempposteriorprobability group by rid)
where rid=rid1;

--select * from defaultDB.posteriorprobability;

--------------------------------------------

drop table if exists defaultDB.local_predictions;
create table defaultDB.local_predictions as
select %{iterationNumber}, testingtable.val as actualclass, bayesnaiveresults.classval as predictedclass --, bayesnaiveresults.rid as rid
from (select rid,classval,max(probability) from defaultDB.posteriorprobability group by rid) as bayesnaiveresults,
	 (select * from defaultDB.testingset where colname = var('classname')) as testingtable
where bayesnaiveresults.rid=testingtable.rid;


drop table if exists defaultDB.local_tempconfusionmatrix;
create table defaultDB.local_tempconfusionmatrix as
select %{iterationNumber} as iterationNumber , val1 as actualclass,val2 as predictedclass, 0 as val from
(select distinct val as val1 from defaultDB.local_probabilities where colname = var('classname')),
(select distinct val as val2 from defaultDB.local_probabilities where colname = var('classname'));

insert into defaultDB.local_tempconfusionmatrix
select %{iterationNumber} as iterationNumber,actualclass,predictedclass,count(*) as val
from ( select bayesnaiveresults.rid,testingtable.val as actualclass, bayesnaiveresults.classval as predictedclass, 1 as val
       from (select rid,classval,max(probability) from defaultDB.posteriorprobability group by rid) as bayesnaiveresults,
		    (select * from defaultDB.testingset where colname = var('classname')) as testingtable
       where bayesnaiveresults.rid=testingtable.rid)
group by actualclass,predictedclass;

drop table if exists defaultDB.local_confusionmatrix;
create table defaultDB.local_confusionmatrix as
select iterationNumber,actualclass,predictedclass,max(val) as val
from defaultDB.local_tempconfusionmatrix
group by iterationNumber,actualclass,predictedclass;

select * from defaultDB.local_confusionmatrix;
