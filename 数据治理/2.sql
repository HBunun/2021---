-- 按月查看城市销售额
select year(p.p_time) as y, month(p.p_time) as m,u.city,count(*) 
from data_yys.purchase p 
left join data_yys.user u
on data_yys.purchase.user_id=data_yys.user.user_id
