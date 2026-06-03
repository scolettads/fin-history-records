select N'DocT:'||dt.name 
from c_invoice,c_doctype dt 
where c_invoice.ad_client_id > 0
group by dt.name having count(dt.name) > 0  order by 1