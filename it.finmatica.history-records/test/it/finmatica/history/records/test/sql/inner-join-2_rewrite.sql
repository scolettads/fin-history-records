select * from m_product_hst m_product inner join c_orderline on c_orderline.m_product_id = m_product.m_product_id
 where c_orderline.qtyOrdered > 0 
 and ((select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = c_orderline.c_order_id) between m_product.hstfromdate and m_product.hsttodate)

