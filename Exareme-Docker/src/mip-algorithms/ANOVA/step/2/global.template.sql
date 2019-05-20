requirevars 'defaultDB'  'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'localsss';

hidden var 'sse' from select sum(sse) from  %{input_global_tbl};
hidden var 'sst' from select sum(sst) from  %{input_global_tbl};
hidden var 'ssregs' from select sum(sst)- sum(sse) from  %{input_global_tbl};

update defaultDB.globalAnovatbl
set sst = '%{sst}', sse = '%{sse}', ssregs = '%{ssregs}'
where no in ( select min(no) from defaultDB.globalAnovatbl where sst is null);

select * from defaultDB.globalAnovatbl;
