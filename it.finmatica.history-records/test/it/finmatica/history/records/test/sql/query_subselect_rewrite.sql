select * from
(select C_Document_Commission.C_DOCUMENT_COMMISSION_ID, C_Document_Commission.C_COMMISSIONLINE_ID, C_CommissionLine.C_Commission_ID,
        C_Document_Commission.M_PRODUCT_CATEGORY_ID, C_Document_Commission.C_INVOICE_ID, C_Invoice.C_CURRENCY_ID,C_Document_Commission.Description, 
        C_Document_Commission.AMOUNT, C_Document_Commission.BASEAMT, 
		CASE
                    WHEN cast(charat(cast(dt.docbasetype as character varying), 3) as character varying) = cast('C' as character varying) THEN (-1.0)
                    ELSE 1.0
        END*InvoicePaidToDateRiba(C_Invoice.C_INVOICE_ID, C_Invoice.C_CURRENCY_ID, NULL, ?)/C_Invoice.GRANDTOTAL as MultAppl,
		COALESCE((select sum(C_CommissionDetail.CommissionAmt) from C_CommissionDetail where C_CommissionDetail.C_DOCUMENT_COMMISSION_ID = C_Document_Commission.C_DOCUMENT_COMMISSION_ID),0) as PREAMOUNT,
		COALESCE((select sum(C_CommissionDetail.ActualAmt) from C_CommissionDetail where C_CommissionDetail.C_DOCUMENT_COMMISSION_ID = C_Document_Commission.C_DOCUMENT_COMMISSION_ID),0) as PREBASEAMT,
		charat(dt.docbasetype::character varying, 3)::text as notacred
 from C_Document_Commission, C_Invoice, C_CommissionLine, C_Doctype_HST dt
where C_Document_Commission.C_INVOICE_ID = C_Invoice.C_INVOICE_ID
  and C_Document_Commission.AMOUNT <>0
  and C_Document_Commission.ISSUSPENDED = 'N'
  and C_Invoice.DATEINVOICED <= ?
  and C_Invoice.AD_ORG_ID = ?
  and (?=0 OR C_Invoice.C_Invoice_ID = ?)
  and (?=0 OR C_Document_Commission.C_BPARTNER_ID = ?)
  and (?=0 OR C_Invoice.C_CURRENCY_ID=?)
  and C_CommissionLine.C_COMMISSIONLINE_ID = C_Document_Commission.C_COMMISSIONLINE_ID
  and dt.c_doctype_id = C_invoice.c_doctype_id
  and ((C_Invoice.dateacct) BETWEEN dt.HSTFromDate and dt.HSTToDate)) res  
where (notacred = 'C' and ((amount * MultAppl)-preamount) < (?*(-1)))
   or (notacred <> 'C' and ((amount * MultAppl)-preamount) > ?)
order by C_Commission_ID, C_COMMISSIONLINE_ID, M_PRODUCT_CATEGORY_ID, C_INVOICE_ID