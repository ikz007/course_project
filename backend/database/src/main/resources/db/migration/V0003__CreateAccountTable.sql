CREATE TABLE IF NOT EXISTS Account(
    IBAN VARCHAR(50) NOT NULL,
    BBAN VARCHAR(50),
    AccountType VARCHAR(30) NOT NULL,
    OpenDate VARCHAR(25),
    CloseDate VARCHAR(25) DEFAULT '9999-01-01',
    Status VARCHAR(10) NOT NULL DEFAULT 'Active'
);

CREATE UNIQUE INDEX Account_IBAN
ON Account(IBAN);