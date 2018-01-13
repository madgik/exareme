select execprogram(null, 'java', '-jar', 'Serialization.jar', 'bicycles.arff', '13-15');

create temp table tree(bin blob);
insert into tree(bin) values (execprogram(null,'cat','tree.ser')) ;
select bin from tree;
