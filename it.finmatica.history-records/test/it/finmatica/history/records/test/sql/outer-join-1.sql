-- LEFT JOIN la condizione DEVE finire nella on
select * from c_orderline left join m_product on c_orderline.m_product_id = m_product.m_product_id