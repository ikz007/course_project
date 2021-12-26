INSERT INTO RuleTable (RuleJson) VALUES ('{"TransactionExceeds":{"amount":5000}}'),
('{"KeywordCheck":{"keywords":["terror","north korea"]}}'),
('{"HighRiskCountryCheck":{"countryList":["AL", "BB", "BF","KH", "KY", "HT","JM", "JO", "ML","MT", "MM", "NI","PK", "PA", "PH","SN", "SS", "SY","TR", "UG", "YE","ZW"]}}'),
('{"UnexpectedBehavior":{"timesBigger":3,"duration":"30 days"}}'),
('{"And":{"left":{"UndeclaredCountry":{"duration":"30 days"}},"right":{"TransactionExceeds":{"amount":1000}}}}');
