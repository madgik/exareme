requirevars 'defaultDB' 'outputformat' ;
attach database '%{defaultDB}' as defaultDB;

update defaultDB.globaltree set nextnode = ""  where nextnode ="-";
update defaultDB.globaltree set leafval = ""  where leafval ="?";

drop table if exists defaultDB.id3resultl;
create table defaultDB.id3resultl ('result') ;

insert into id3resultl select * from (
totabulardataresourceformat title:ID3_TABLE types:int,text,text,int,text
select no  as `id`,colname as `colname`, colval as `edge`,nextnode as `childnodes` ,leafval  as `leafval` from defaultDB.globaltree)
where '%{outputformat}'= 'pfa';

insert into id3resultl
select * from (treetojson select no,tabletojson(no,colname,colval,nextnode,leafval,"id,colname,edge,childnodes,leafval") from defaultDB.globaltree
group by no)
where '%{outputformat}'= 'json';

insert into id3resultl
select tabletojson(no,result, "no,result") from
(formattreetotableoutput select no as id ,jgroup(jpack(colname,colval,nextnode,leafval)) as nodeinfo
from defaultDB.globaltree group by no order by id)
where '%{outputformat}'= 'wekaviewer';

select result from defaultDB.id3resultl where result not null;


-- select no as currentnode,"" as parentnode, colname, colval, nextnode, leafval from globaltree where no=1
-- union
-- select no as currentnode, parentnode, colname, colval, nextnode, leafval  from globaltree,
-- (select no as parentnode, nextnode as cur from globaltree)
-- where cur = no ;
--
-- update mytree set parentnode=0 where parentnode="";
-- update mytree set currentnode =7 where parentnode="" and leafval="none";
--
--
-- highchartsunburst select currentnode,parentnode, colname, colval, nextnode, leafval,weight from mytree,
-- (select currentnode as currennode1 , 1.0/cast(count(*) as real) as weight from mytree
-- group by currentnode)
-- where currentnode =currennode1;
