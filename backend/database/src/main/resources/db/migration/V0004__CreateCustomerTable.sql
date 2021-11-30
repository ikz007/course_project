CREATE TABLE IF NOT EXISTS Customer (
    CustomerID VARCHAR(50),
    CustomerName VARCHAR(50),
    BusinessType VARCHAR(10),
    MonthlyIncome DOUBLE(34,2),
    Status VARCHAR(15) DEFAULT 'Active',
    BirthDate DATE,
    Pep BOOLEAN DEFAULT 0,
    CountryOfBirth VARCHAR(2),
    CountryOfResidence VARCHAR(2)
);

CREATE UNIQUE INDEX Customer_CustomerID
ON Customer(CustomerID);