INSERT INTO ScenarioSetting VALUES
('HighRiskCountries', 'SELECT CountryISO FROM Country WHERE HighRiskCountry = 1'),
('TransactionThreshold', 'SELECT 5000 as Threshold'),
('TransactionKeyword', 'SELECT KeywordCode FROM TransactionKeyword');