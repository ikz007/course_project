CREATE TABLE IF NOT EXISTS Customer (
    CustomerID VARCHAR(50) NOT NULL,
    CustomerName VARCHAR(50) NOT NULL,
    BusinessType VARCHAR(10),
    MonthlyIncome NUMERIC(34,2) NOT NULL,
    Status VARCHAR(15) DEFAULT 'Active' NOT NULL,
    BirthDate VARCHAR(30) NOT NULL,
    Pep BOOLEAN DEFAULT 0 NOT NULL,
    CountryOfBirth VARCHAR(2) NOT NULL,
    CountryOfResidence VARCHAR(2) NOT NULL
);

CREATE UNIQUE INDEX Customer_CustomerID
ON Customer(CustomerID);