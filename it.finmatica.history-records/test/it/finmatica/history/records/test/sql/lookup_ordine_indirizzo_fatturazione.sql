--sostituito per subquery + id
-- subquery: (select XHSTX.dateordered from c_order XHSTX where XHSTX.c_order_id = #c_orderline.c_order_id#)
-- id: 123

SELECT
C_BPartner_Location.C_BPartner_Location_ID,
NULL,
NVL(C_BPartner_Location.Name,'-1'),
C_BPartner_Location.IsActive
FROM C_BPartner_Location
WHERE C_BPartner_Location.AD_Client_ID IN(0,11)
AND C_BPartner_Location.AD_Org_ID IN
(
   0,11,12,1000004,1000005,50000,50002,50001,50004,50006,50005,50007
)
AND
(
   C_BPartner_Location.C_BPartner_Location_ID IS NULL OR C_BPartner_Location.C_BPartner_Location_ID NOT IN
   (
      SELECT
      Record_ID
      FROM AD_Private_Access
      WHERE AD_Table_ID = 293
      AND AD_User_ID <> 100
      AND IsActive = 'Y'
   )
)
AND C_BPartner_Location.C_BPartner_ID=117
AND C_BPartner_Location.IsBillTo='Y'
AND C_BPartner_Location.IsActive='Y'
ORDER BY 3
