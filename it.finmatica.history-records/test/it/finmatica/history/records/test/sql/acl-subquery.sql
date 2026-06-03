-- Subselect in 'where'
select
*
from c_orderline
inner join m_inoutline on c_orderline.c_orderline_id = m_inoutline.c_orderline_id
where c_orderline.m_product_id in (select m_product.m_product_id from m_product where m_product.name like'%Fertilizer%')
