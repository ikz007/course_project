INSERT INTO Account VALUES ('DE89370400440532013000', '40532013000', 'INDIVIDUAL', cast(NOW() as date), '9999-01-01', 'ACTIVE'),
('AT611904300234573201', '00234573201', 'INDIVIDUAL', cast(NOW() as date), '9999-01-01', 'ACTIVE'),
('AT611904300234573202', '00234573202', 'INDIVIDUAL', cast(NOW() as date), '9999-01-01', 'ACTIVE'),
('FR1420041010050500013', '10050500013', 'INDIVIDUAL', cast(NOW() as date), '9999-01-01', 'ACTIVE');

INSERT INTO Customer VALUES ('000001', 'Test Customer 1', null, 30000, 'Active', '1990-01-01', 1, 'AT', 'AT'),
('000002', 'Test Customer 2', null, 2000, 'Active', '1990-02-01', 1, 'FR', 'KR'),
('000003', 'Test Customer 3', null, 100000, 'Active', '1970-07-04', 1, 'DE', 'DE');

INSERT INTO Questionnaire VALUES ('1', '000001', 'FR', 10000, 13000, 'Relatives', 1),
('2', '000002', 'AF', 100000, 1200000, 'Business', 0);

INSERT INTO Relationship VALUES ('1', '000001', 'AT611904300234573201', '2020-02-20', '9999-01-01'),
('2', '000001', 'AT611904300234573202', '2020-03-20', '9999-01-01'),
('3', '000002', 'FR1420041010050500013', '2020-03-20', '9999-01-01'),
('4', '000003', 'DE89370400440532013000', '2020-03-20', '9999-01-01');