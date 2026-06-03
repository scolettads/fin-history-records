-- nella pratica identico a caso 1 ma funziona se il parser in entrambi i casi mi dice che è una inner
select * from c_orderline, m_product 
 where c_orderline.m_product_id = m_product.m_product_id