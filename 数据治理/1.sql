-- 按月查看总体销售额
select year(p_time) as yy, month(p_time ) as mm,count(*) from "data_yys"."purchase"

-- 按月查看新增注册人数
select year(p_time) as yy, month(p_time ) as mm,count(*) from "data_yys"."user"
