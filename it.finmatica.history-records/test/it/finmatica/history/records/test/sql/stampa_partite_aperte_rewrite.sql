-- impostata RV_OpenItem come sorgente di data come #RV_OpenItem.DateInvoiced#
-- storicizzato sia C_Activity che C_Activity_trl

SELECT
(SELECT NVL(C_BPartner.Value,'-1') ||'_'|| NVL(C_BPartner.Name,'-1') FROM (select * from C_BPartner_HST where (RV_OpenItem.DateInvoiced) between C_BPartner_HST.HSTFromDate and C_BPartner_HST.HSTToDate) C_BPartner WHERE RV_OpenItem.C_BPartner_ID=C_BPartner.C_BPartner_ID) AS AC_BPartner_ID,
RV_OpenItem.C_BPartner_ID AS C_BPartner_ID,
(SELECT NVL(C_Currency.ISO_Code,'-1') FROM C_Currency WHERE RV_OpenItem.C_Currency_ID=C_Currency.C_Currency_ID) AS BC_Currency_ID,
RV_OpenItem.C_Currency_ID AS C_Currency_ID,
(SELECT NVL(C_ConversionType.Name,'-1') FROM C_ConversionType WHERE RV_OpenItem.C_ConversionType_ID=C_ConversionType.C_ConversionType_ID) AS CC_ConversionType_ID,
RV_OpenItem.C_ConversionType_ID AS C_ConversionType_ID,
RV_OpenItem.DateInvoiced,
RV_OpenItem.DaysDue,
RV_OpenItem.DiscountAmt,
RV_OpenItem.DiscountDate,
RV_OpenItem.DocumentNo,
RV_OpenItem.DueDate,
RV_OpenItem.GrandTotal,
(SELECT NVL(C_Invoice.DocumentNo,'-1') ||'_'|| NVL(TO_CHAR(C_Invoice.DateInvoiced,'DD/MM/YYYY'),'-1') ||'_'|| NVL(CAST (C_Invoice.GrandTotal AS Text),'-1') ||'_'|| NVL(C_Invoice.VATLedgerNo,'-1') FROM C_Invoice WHERE RV_OpenItem.C_Invoice_ID=C_Invoice.C_Invoice_ID) AS DC_Invoice_ID,
RV_OpenItem.C_Invoice_ID AS C_Invoice_ID,
(SELECT NVL(TO_CHAR(C_InvoicePaySchedule.DueDate,'DD/MM/YYYY'),'-1') ||'_'|| NVL(CAST (C_InvoicePaySchedule.DueAmt AS Text),'-1') FROM C_InvoicePaySchedule WHERE RV_OpenItem.C_InvoicePaySchedule_ID=C_InvoicePaySchedule.C_InvoicePaySchedule_ID) AS EC_InvoicePaySchedule_ID,
RV_OpenItem.C_InvoicePaySchedule_ID AS C_InvoicePaySchedule_ID,
RV_OpenItem.NetDays,
RV_OpenItem.OpenAmt,
(SELECT NVL(C_Order.DocumentNo,'-1') ||'_'|| NVL(TO_CHAR(C_Order.DateOrdered,'DD/MM/YYYY'),'-1') FROM C_Order WHERE RV_OpenItem.C_Order_ID=C_Order.C_Order_ID) AS FC_Order_ID,
RV_OpenItem.C_Order_ID AS C_Order_ID,
RV_OpenItem.PaidAmt,
RV_OpenItem.IsPayScheduleValid,
RV_OpenItem.IsSOTrx,
RV_OpenItem.DateAcct,
(SELECT NVL(C_Activity_Trl.Name,'-1') FROM (select * from C_Activity_HST where (RV_OpenItem.DateInvoiced) between C_Activity_HST.HSTFromDate and C_Activity_HST.HSTToDate) C_Activity INNER JOIN (select * from C_Activity_TRL_HST where (RV_OpenItem.DateInvoiced) between C_Activity_TRL_HST.HSTFromDate and C_Activity_TRL_HST.HSTToDate) C_Activity_TRL ON (C_Activity.C_Activity_ID=C_Activity_Trl.C_Activity_ID AND C_Activity_Trl.AD_Language='it_IT') WHERE RV_OpenItem.C_Activity_ID=C_Activity.C_Activity_ID) AS GC_Activity_ID,
RV_OpenItem.C_Activity_ID AS C_Activity_ID,
RV_OpenItem.IsApproved,
(SELECT NVL(C_Campaign_Trl.Name,'-1') FROM C_Campaign INNER JOIN C_Campaign_TRL ON (C_Campaign.C_Campaign_ID=C_Campaign_Trl.C_Campaign_ID AND C_Campaign_Trl.AD_Language='it_IT') WHERE RV_OpenItem.C_Campaign_ID=C_Campaign.C_Campaign_ID) AS HC_Campaign_ID,
RV_OpenItem.C_Campaign_ID AS C_Campaign_ID,
(SELECT NVL(C_Charge_Trl.Name,'-1') FROM C_Charge INNER JOIN C_Charge_TRL ON (C_Charge.C_Charge_ID=C_Charge_Trl.C_Charge_ID AND C_Charge_Trl.AD_Language='it_IT') WHERE RV_OpenItem.C_Charge_ID=C_Charge.C_Charge_ID) AS IC_Charge_ID,
RV_OpenItem.C_Charge_ID AS C_Charge_ID,
RV_OpenItem.ChargeAmt,
J.Name AS JName,
RV_OpenItem.InvoiceCollectionType AS InvoiceCollectionType,
RV_OpenItem.DateOrdered,
RV_OpenItem.DatePrinted,
RV_OpenItem.Description,
RV_OpenItem.IsDiscountPrinted,
K.Name AS KName,
RV_OpenItem.DocAction AS DocAction,
L.Name AS LName,
RV_OpenItem.DocStatus AS DocStatus,
(SELECT NVL(C_DocType_Trl.Name,'-1') FROM C_DocType INNER JOIN C_DocType_TRL ON (C_DocType.C_DocType_ID=C_DocType_Trl.C_DocType_ID AND C_DocType_Trl.AD_Language='it_IT') WHERE RV_OpenItem.C_DocType_ID=C_DocType.C_DocType_ID) AS MC_DocType_ID,
RV_OpenItem.C_DocType_ID AS C_DocType_ID,
RV_OpenItem.DunningGrace,
(SELECT NVL(C_DunningLevel.Name,'-1') FROM C_DunningLevel WHERE RV_OpenItem.C_DunningLevel_ID=C_DunningLevel.C_DunningLevel_ID) AS NC_DunningLevel_ID,
RV_OpenItem.C_DunningLevel_ID AS C_DunningLevel_ID,
RV_OpenItem.GenerateTo,
RV_OpenItem.GrandTotal_Conv,
RV_OpenItem.IsInDispute,
RV_OpenItem.OpenAmt_Conv,
RV_OpenItem.POReference,
RV_OpenItem.IsPaid,
(SELECT NVL(C_BPartner_Location.Name,'-1') FROM C_BPartner_Location WHERE RV_OpenItem.C_BPartner_Location_ID=C_BPartner_Location.C_BPartner_Location_ID) AS OC_BPartner_Location_ID,
RV_OpenItem.C_BPartner_Location_ID AS C_BPartner_Location_ID,
(SELECT NVL(C_Payment.DocumentNo,'-1') ||'_'|| NVL(TO_CHAR(C_Payment.DateTrx,'DD/MM/YYYY'),'-1') ||'_'|| NVL(CAST (C_Payment.PayAmt AS Text),'-1') ||'_'|| NVL(C_Payment.A_Name,'-1') FROM C_Payment WHERE RV_OpenItem.C_Payment_ID=C_Payment.C_Payment_ID) AS PC_Payment_ID,
RV_OpenItem.C_Payment_ID AS C_Payment_ID,
RV_OpenItem.PaymentRule,
(SELECT NVL(C_PaymentTerm_Trl.Name,'-1') FROM C_PaymentTerm INNER JOIN C_PaymentTerm_TRL ON (C_PaymentTerm.C_PaymentTerm_ID=C_PaymentTerm_Trl.C_PaymentTerm_ID AND C_PaymentTerm_Trl.AD_Language='it_IT') WHERE RV_OpenItem.C_PaymentTerm_ID=C_PaymentTerm.C_PaymentTerm_ID) AS QC_PaymentTerm_ID,
RV_OpenItem.C_PaymentTerm_ID AS C_PaymentTerm_ID,
RV_OpenItem.Posted,
RV_OpenItem.IsTaxIncluded,
(SELECT NVL(M_PriceList_Trl.Name,'-1') FROM M_PriceList INNER JOIN M_PriceList_TRL ON (M_PriceList.M_PriceList_ID=M_PriceList_Trl.M_PriceList_ID AND M_PriceList_Trl.AD_Language='it_IT') WHERE RV_OpenItem.M_PriceList_ID=M_PriceList.M_PriceList_ID) AS RM_PriceList_ID,
RV_OpenItem.M_PriceList_ID AS M_PriceList_ID,
RV_OpenItem.IsPrinted,
RV_OpenItem.ProcessedOn,
RV_OpenItem.Processing,
(SELECT NVL(C_Project.Value,'-1') ||'_'|| NVL(C_Project.Name,'-1') FROM C_Project WHERE RV_OpenItem.C_Project_ID=C_Project.C_Project_ID) AS SC_Project_ID,
RV_OpenItem.C_Project_ID AS C_Project_ID,
(SELECT C_Invoice.DocumentNo FROM C_Invoice C_Invoice WHERE RV_OpenItem.Reversal_ID=C_Invoice.C_Invoice_ID) AS TReversal_ID,
RV_OpenItem.Reversal_ID AS Reversal_ID,
(SELECT NVL(M_RMA.DocumentNo,'-1') FROM M_RMA WHERE RV_OpenItem.M_RMA_ID=M_RMA.M_RMA_ID) AS UM_RMA_ID,
RV_OpenItem.M_RMA_ID AS M_RMA_ID,
(SELECT AD_User.Name FROM AD_User AD_User WHERE RV_OpenItem.SalesRep_ID=AD_User.AD_User_ID) AS VSalesRep_ID,
RV_OpenItem.SalesRep_ID AS SalesRep_ID,
RV_OpenItem.IsSelfService,
RV_OpenItem.SendEMail,
(SELECT C_DocType_Trl.Name FROM C_DocType C_DocType INNER JOIN C_DocType_TRL ON (C_DocType.C_DocType_ID=C_DocType_Trl.C_DocType_ID AND C_DocType_Trl.AD_Language='it_IT') WHERE RV_OpenItem.C_DocTypeTarget_ID=C_DocType.C_DocType_ID) AS WC_DocTypeTarget_ID,
RV_OpenItem.C_DocTypeTarget_ID AS C_DocTypeTarget_ID,
RV_OpenItem.TotalLines,
RV_OpenItem.IsTransferred,
(SELECT AD_Org.Value||'-'||AD_Org.Name FROM AD_Org AD_Org WHERE RV_OpenItem.AD_OrgTrx_ID=AD_Org.AD_Org_ID) AS XAD_OrgTrx_ID,
RV_OpenItem.AD_OrgTrx_ID AS AD_OrgTrx_ID,
(SELECT NVL(AD_User.Name,'-1') FROM AD_User WHERE RV_OpenItem.AD_User_ID=AD_User.AD_User_ID) AS YAD_User_ID,
RV_OpenItem.AD_User_ID AS AD_User_ID,
(SELECT C_ElementValue.Value||'-'||C_ElementValue_Trl.Name FROM C_ElementValue C_ElementValue INNER JOIN C_ElementValue_TRL ON (C_ElementValue.C_ElementValue_ID=C_ElementValue_Trl.C_ElementValue_ID AND C_ElementValue_Trl.AD_Language='it_IT') WHERE RV_OpenItem.User1_ID=C_ElementValue.C_ElementValue_ID) AS ZUser1_ID,
RV_OpenItem.User1_ID AS User1_ID,
(SELECT C_ElementValue.Value||'-'||C_ElementValue_Trl.Name FROM C_ElementValue C_ElementValue INNER JOIN C_ElementValue_TRL ON (C_ElementValue.C_ElementValue_ID=C_ElementValue_Trl.C_ElementValue_ID AND C_ElementValue_Trl.AD_Language='it_IT') WHERE RV_OpenItem.User2_ID=C_ElementValue.C_ElementValue_ID) AS AAUser2_ID,
RV_OpenItem.User2_ID AS User2_ID,
RV_OpenItem.VATLedgerDate,
RV_OpenItem.VATLedgerNo
FROM RV_OpenItem LEFT OUTER
JOIN AD_Ref_List XJ ON
(
   RV_OpenItem.InvoiceCollectionType=XJ.Value
   AND XJ.AD_Reference_ID=394
)
LEFT OUTER
JOIN AD_Ref_List_Trl J ON
(
   XJ.AD_Ref_List_ID=J.AD_Ref_List_ID
   AND J.AD_Language='it_IT'
)
LEFT OUTER
JOIN AD_Ref_List XK ON
(
   RV_OpenItem.DocAction=XK.Value
   AND XK.AD_Reference_ID=0
)
LEFT OUTER
JOIN AD_Ref_List_Trl K ON
(
   XK.AD_Ref_List_ID=K.AD_Ref_List_ID
   AND K.AD_Language='it_IT'
)
LEFT OUTER
JOIN AD_Ref_List XL ON
(
   RV_OpenItem.DocStatus=XL.Value
   AND XL.AD_Reference_ID=0
)
LEFT OUTER
JOIN AD_Ref_List_Trl L ON
(
   XL.AD_Ref_List_ID=L.AD_Ref_List_ID
   AND L.AD_Language='it_IT'
)
WHERE
(
   RV_OpenItem.AD_Org_ID=11.0
   AND RV_OpenItem.IsSOTrx='Y'
   AND RV_OpenItem.DaysDue BETWEEN -99999.0
   AND 99999.0
)
AND RV_OpenItem.AD_Client_ID IN(0,11)
AND RV_OpenItem.AD_Org_ID IN
(
   0,11,12,1000004,1000005,50000,50002,50001,50004,50006,50005,50007
)
AND
(
   XJ.AD_Ref_List_ID IS NULL OR XJ.AD_Ref_List_ID NOT IN
   (
      SELECT
      Record_ID
      FROM AD_Private_Access
      WHERE AD_Table_ID = 104
      AND AD_User_ID <> 100
      AND IsActive = 'Y'
   )
)
AND
(
   XK.AD_Ref_List_ID IS NULL OR XK.AD_Ref_List_ID NOT IN
   (
      SELECT
      Record_ID
      FROM AD_Private_Access
      WHERE AD_Table_ID = 104
      AND AD_User_ID <> 100
      AND IsActive = 'Y'
   )
)
AND
(
   XL.AD_Ref_List_ID IS NULL OR XL.AD_Ref_List_ID NOT IN
   (
      SELECT
      Record_ID
      FROM AD_Private_Access
      WHERE AD_Table_ID = 104
      AND AD_User_ID <> 100
      AND IsActive = 'Y'
   )
)
