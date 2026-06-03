select * 
from m_product inner join c_orderline on c_orderline.m_product_id = m_product.m_product_id
where c_orderline.qtyOrdered > 0