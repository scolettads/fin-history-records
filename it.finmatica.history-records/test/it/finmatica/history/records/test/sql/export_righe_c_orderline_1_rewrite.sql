--sostituito per subquery + id
-- subquery: (select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#)
-- id: 1007279

SELECT
DateDelivered,
PriceCost,
IsActive,
(select productType from (select * from M_Product_HST where (select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = 1007279) between M_Product_HST.HSTFromDate and M_Product_HST.HSTToDate) M_Product where M_Product.M_Product_ID = C_OrderLine.M_Product_ID) AS ProductType,
C_Currency_ID,
DateInvoiced,
C_OrderLine_ID,
PriceLimit,
IsDescription,
C_ProjectTask_ID,
RRStartDate,
Ref_OrderLine_ID,
(SELECT isforcedmnt from (select * from m_product_HST where (select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = 1007279) between m_product_HST.HSTFromDate and m_product_HST.HSTToDate) m_product where m_product.m_product_id=c_orderline.m_product_id) AS IsProductForcedMnt,
RRAmt,
AD_Client_ID,
AD_Org_ID,
C_Order_ID,
C_BPartner_ID,
C_BPartner_Location_ID,
DatePromised,
DateOrdered,
Line,
M_Warehouse_ID,
M_Product_ID,
C_Charge_ID,
M_AttributeSetInstance_ID,
S_ResourceAssignment_ID,
IsMntLine,
NPeriod,
UomPeriod,
IsSuppLine,
IsMaterialIncl,
IsResIncl,
CIG,
Description,
QtyEntered,
C_UOM_ID,
QtyOrdered,
QtyDelivered,
QtyReserved,
QtyInvoiced,
M_Shipper_ID,
PriceEntered,
PriceList,
PriceActual,
Discount,
FreightAmt,
C_Tax_ID,
LIT_CompositeDisc,
PriceImported,
C_Project_ID,
C_Campaign_ID,
C_ProjectPhase_ID,
C_Activity_ID,
AD_OrgTrx_ID,
User1_ID,
User2_ID,
LIT_LineAmtBeforeDisc,
LIT_LineDocDiscVal,
LineNetAmt,
QtyLostSales,
Processed,
LIT_IsCashOrdAdv,
LIT_AdvPerc,
CreateShipment,
CreateProduction,
Created,
CreatedBy,
Updated,
UpdatedBy
FROM C_OrderLine
WHERE (C_OrderLine.C_Order_ID=1007279)
AND C_OrderLine.AD_Client_ID IN(0,11)
AND C_OrderLine.AD_Org_ID IN
(
   0,11,12,1000004,1000005,50000,50002,50001,50004,50006,50005,50007
)
AND
(
   C_OrderLine.C_OrderLine_ID IS NULL OR C_OrderLine.C_OrderLine_ID NOT IN
   (
      SELECT
      Record_ID
      FROM AD_Private_Access
      WHERE AD_Table_ID = 260
      AND AD_User_ID <> 100
      AND IsActive = 'Y'
   )
)
ORDER BY Line
