-- LEFT JOIN la condizione DEVE finire nella on
select * from c_orderline left join m_product_hst m_product 
	on c_orderline.m_product_id = m_product.m_product_id and ((select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = c_orderline.c_order_id) between m_product.hstfromdate and m_product.hsttodate)