-- 按月查看城市、商品类别的销售额
select year(p.p_time) as y, month(p.p_time) as m,u.city as c ,p.goods_id as g,p.purchase_id as p 
from data_yys.purchase p 
left join data_yys.user u
on data_yys.purchase.user_id=data_yys.user.user_id
as temp_table

select temp_table.y, temp_table.m, temp_table.c,good.category , count(*)
from temp_table
left join data_yys.goods good
on temp_table.g=data_yys.goods.goods_id 
