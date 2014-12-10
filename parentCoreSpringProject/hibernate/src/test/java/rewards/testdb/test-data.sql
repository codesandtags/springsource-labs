insert into T_ACCOUNT (NUMBER, NAME) values ('123456789', 'Keith and Keri Donald');
insert into T_ACCOUNT_CREDIT_CARD (ACCOUNT_ID, NUMBER) values (0, '1234123412341234');
insert into T_ACCOUNT_BENEFICIARY (ACCOUNT_ID, NAME, ALLOCATION_PERCENTAGE, SAVINGS) values (0, 'Annabelle', 0.50, 0.00);
insert into T_ACCOUNT_BENEFICIARY (ACCOUNT_ID, NAME, ALLOCATION_PERCENTAGE, SAVINGS) values (0, 'Corgan', 0.50, 0.00);

insert into T_RESTAURANT (MERCHANT_NUMBER, NAME, BENEFIT_PERCENTAGE, BENEFIT_AVAILABILITY_POLICY) values ('1234567890', 'AppleBees', 0.08, 'ALWAYS_AVAILABLE');