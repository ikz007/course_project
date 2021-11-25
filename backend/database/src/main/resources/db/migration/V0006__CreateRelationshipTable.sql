CREATE TABLE IF NOT EXISTS Relationships(
    CustomerID INT(11) NOT NULL,
    IBAN VARCHAR(50),
    StartDate DATE,
    EndDate DATE DEFAULT '9999-01-01',
    FOREIGN KEY (CustomerID)
    REFERENCES Customers(CustomerID),
    FOREIGN KEY (IBAN)
    REFERENCES Account(IBAN)
);