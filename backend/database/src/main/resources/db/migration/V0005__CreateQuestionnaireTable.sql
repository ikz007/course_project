CREATE TABLE IF NOT EXISTS Questionnaire (
    QuestionnaireID VARCHAR(50),
    CustomerID VARCHAR(50),
    Country VARCHAR(2),
    MonthlyTurnover NUMERIC(32,2),
    AnnualTurnover NUMERIC(32,2),
    Reason VARCHAR(1000),
    Active BOOLEAN DEFAULT 1,
    FOREIGN KEY (CustomerID)
    REFERENCES Customer(CustomerID)
);

CREATE UNIQUE INDEX Questionnaire_QuestionnaireID
ON Questionnaire(QuestionnaireID);
