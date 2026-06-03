-- nella pratica identico a caso 1 ma funziona se il parser in entrambi i casi mi dice che è una inner
select * from c_orderline, m_product_hst m_product
 where c_orderline.m_product_id = m_product.m_product_id
   and ((select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = c_orderline.c_order_id) between m_product.hstfromdate and m_product.hsttodate)
