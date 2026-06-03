-- simulazione si lookup che arriva da una maschera sostituzione secca per data (31/12/2017)

SELECT
COUNT(*)
FROM
(
   SELECT
   bp.C_BPartner_ID,
   bp.Value AS Value,
   bp.Name AS Name,
   NVL(c.Name,'-1'),
   c.AD_User_ID,
   bp.SO_CreditLimit-bp.SO_CreditUsed AS SO_CreditAvailable,
   bp.SO_CreditUsed AS SO_CreditUsed,
   c.Phone AS Phone,
   a.Postal AS Postal,
   a.City AS City,
   bp.TotalOpenBalance AS TotalOpenBalance,
   bp.ActualLifetimeValue AS Revenue,
   a.Address1 AS Address1,
   l.IsShipTo AS IsShipTo,
   l.IsBillTo AS IsBillTo,
   NVL(l.Name,'-1'),
   l.C_BPartner_Location_ID
   FROM C_BPartner_HST bp LEFT OUTER
   JOIN C_BPartner_Location_HST l ON
   (
      bp.C_BPartner_ID=l.C_BPartner_ID
      AND l.IsActive='Y'
   )
   AND ((DATE'2017-12-31') between l.HSTFromDate and l.HSTToDate)
   LEFT OUTER
   JOIN AD_User c ON
   (
      bp.C_BPartner_ID=c.C_BPartner_ID
      AND
      (
         c.C_BPartner_Location_ID IS NULL OR c.C_BPartner_Location_ID=l.C_BPartner_Location_ID
      )
      AND c.IsActive='Y'
   )
   LEFT OUTER
   JOIN C_Location a ON (l.C_Location_ID=a.C_Location_ID)
   WHERE bp.IsActive='Y'
   AND bp.IsSummary='N'
   AND bp.IsActive='Y'
   AND upper(replace(translate(bp.Name,'[@.-+_,:''"]', ' '), ' ','')) Like '?'
   AND bp.IsCustomer = '?'
   AND bp.AD_Client_ID IN(0,11)
   AND bp.AD_Org_ID IN
   (
      0,11,12,1000004,1000005,50000,50002,50001,50004,50006,50005,50007
   )
   AND
   (
      bp.C_BPartner_ID IS NULL OR bp.C_BPartner_ID NOT IN
      (
         SELECT
         Record_ID
         FROM AD_Private_Access
         WHERE AD_Table_ID = 291
         AND AD_User_ID <> 100
         AND IsActive = 'Y'
      )
   )
   AND
   (
      l.C_BPartner_Location_ID IS NULL OR l.C_BPartner_Location_ID NOT IN
      (
         SELECT
         Record_ID
         FROM AD_Private_Access
         WHERE AD_Table_ID = 293
         AND AD_User_ID <> 100
         AND IsActive = 'Y'
      )
   )
   AND
   (
      c.AD_User_ID IS NULL OR c.AD_User_ID NOT IN
      (
         SELECT
         Record_ID
         FROM AD_Private_Access
         WHERE AD_Table_ID = 114
         AND AD_User_ID <> 100
         AND IsActive = 'Y'
      )
   )
   AND
   (
      a.C_Location_ID IS NULL OR a.C_Location_ID NOT IN
      (
         SELECT
         Record_ID
         FROM AD_Private_Access
         WHERE AD_Table_ID = 162
         AND AD_User_ID <> 100
         AND IsActive = 'Y'
      )
   )
   AND ((DATE'2017-12-31') between bp.HSTFromDate and bp.HSTToDate)
)
a
