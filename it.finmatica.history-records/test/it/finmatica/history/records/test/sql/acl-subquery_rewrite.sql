-- Subselect in 'where'
select
*
from c_orderline
inner join m_inoutline on c_orderline.c_orderline_id = m_inoutline.c_orderline_id
where c_orderline.m_product_id in (select m_product.m_product_id from (
   select * from m_product_hst
   where
   (
      select
      XHSTX.dateordered
      from c_order XHSTX
      where XHSTX.c_order_id = c_orderline.c_order_id
   )
   between m_product_hst.hstfromdate and m_product_hst.hsttodate
)
m_product where m_product.name like'%Fertilizer%')
