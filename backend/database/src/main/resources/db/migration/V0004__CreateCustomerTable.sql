CREATE TABLE IF NOT EXISTS Customers (
    CustomerID INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    CustomerName VARCHAR(50),
    BusinessType VARCHAR(10),
    MonthlyIncome DOUBLE(34,2),
    Status VARCHAR(15) DEFAULT 'Active',
    BirthDate DATE,
    Pep BOOLEAN DEFAULT 0,
    CountryOfBirth VARCHAR(2),
    CountryOfResidence VARCHAR(2)
);