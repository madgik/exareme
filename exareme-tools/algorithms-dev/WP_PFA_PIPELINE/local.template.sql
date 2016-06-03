requirevars 'defaultDB' 'input_local_tbl' 'local_pfa';

create temporary table inputlocaltbl as
select * from %{input_local_tbl};

select * from (madtitus 'pfa:%{local_pfa}' select * from inputlocaltbl);
