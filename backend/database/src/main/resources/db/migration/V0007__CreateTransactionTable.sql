CREATE TABLE IF NOT EXISTS Transaction (
    OurIBAN VARCHAR(50) NOT NULL,
    TheirIBAN VARCHAR(50) NOT NULL,
    Reference VARCHAR(100) NOT NULL,
    BookingDateTime VARCHAR(30) NOT NULL,
    TransactionCode VARCHAR(4) NOT NULL,
    DebitCredit CHAR(1) NOT NULL,
    Amount NUMERIC(32,2) NOT NULL,
    Currency VARCHAR(3) NOT NULL,
    Description VARCHAR(150),
    CountryCode VARCHAR(2) NOT NULL
);


CREATE UNIQUE INDEX Transactions_Reference
ON Transaction(Reference);