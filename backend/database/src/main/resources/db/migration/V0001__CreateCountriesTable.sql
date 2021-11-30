CREATE TABLE IF NOT EXISTS Country(
    CountryISO VARCHAR(2) NOT NULL,
    CountryName VARCHAR(100),
    HighRiskCountry BOOLEAN DEFAULT 0
);

CREATE UNIQUE INDEX Countries_CountryISO
ON Country(CountryISO);


