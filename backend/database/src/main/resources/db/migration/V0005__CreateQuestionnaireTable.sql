CREATE TABLE IF NOT EXISTS Questionnaire (
    CustomerID INT(11) NOT NULL,
    Country VARCHAR(2),
    MonthlyTurnover DOUBLE(32,2),
    AnnualTurnover DOUBLE(32,2),
    Reason VARCHAR(1000),
    CreatedAt TIMESTAMP,
    FOREIGN KEY (CustomerID)
    REFERENCES Customers(CustomerID)
);