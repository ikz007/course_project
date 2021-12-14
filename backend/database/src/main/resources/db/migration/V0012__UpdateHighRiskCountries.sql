-- https://www.fatf-gafi.org/publications/high-risk-and-other-monitored-jurisdictions/documents/increased-monitoring-october-2021.html

UPDATE Country
SET HighRiskCountry = 1
WHERE CountryISO IN (
'AL', 'BB', 'BF',
'KH', 'KY', 'HT',
'JM', 'JO', 'ML',
'MT', 'MM', 'NI',
'PK', 'PA', 'PH',
'SN', 'SS', 'SY',
'TR', 'UG', 'YE',
'ZW'
);

