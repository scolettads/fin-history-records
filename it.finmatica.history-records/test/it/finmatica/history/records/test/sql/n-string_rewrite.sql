select N'DocT:'||dt.name 
from c_invoice,C_Doctype_HST dt 
where c_invoice.ad_client_id > 0
 and ((C_Invoice.dateacct) between dt.hstfromdate and dt.hsttodate)
group by dt.name having count(dt.name) > 0  
order by 1