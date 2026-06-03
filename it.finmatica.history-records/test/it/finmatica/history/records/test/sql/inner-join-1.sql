select * 
from c_orderline inner join m_product on c_orderline.m_product_id = m_product.m_product_id
where c_orderline.qtyOrdered > 0