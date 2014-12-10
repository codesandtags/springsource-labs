package rewards.internal.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import common.money.MonetaryAmount;
import common.money.Percentage;

/**
 * Tests the JDBC account repository with a test data source to verify data access and relational-to-object mapping
 * behavior works as expected.
 */
public class JdbcAccountRepositoryTests {

	private JdbcAccountRepository repository;

	private DataSource dataSource;

	@Before
	public void setUp() throws Exception {
		dataSource = createTestDataSource();
		repository = new JdbcAccountRepository();
		repository.setDataSource(dataSource);
	}

	@Test
	public void testFindAccountByCreditCard() {
		Account account = repository.findByCreditCard("1234123412341234");
		// assert the returned account contains what you expect given the state of the database
		assertNotNull("account should never be null", account);
		assertEquals("wrong entity id", Long.valueOf(0), account.getEntityId());
		assertEquals("wrong account number", "123456789", account.getNumber());
		assertEquals("wrong name", "Keith and Keri Donald", account.getName());
		assertEquals("wrong beneficiary collection size", 2, account.getBeneficiaries().size());

		Beneficiary b1 = account.getBeneficiary("Annabelle");
		assertNotNull("Annabelle should be a beneficiary", b1);
		assertEquals("wrong savings", MonetaryAmount.valueOf("0.00"), b1.getSavings());
		assertEquals("wrong allocation percentage", Percentage.valueOf("50%"), b1.getAllocationPercentage());

		Beneficiary b2 = account.getBeneficiary("Corgan");
		assertNotNull("Corgan should be a beneficiary", b2);
		assertEquals("wrong savings", MonetaryAmount.valueOf("0.00"), b2.getSavings());
		assertEquals("wrong allocation percentage", Percentage.valueOf("50%"), b2.getAllocationPercentage());
	}

	@Test
	public void testFindAccountByCreditCardNoAccount() {
		try {
			repository.findByCreditCard("bogus");
			fail("Should've failed");
		} catch (EmptyResultDataAccessException e) {
			// expected
		}
	}

	@Test
	public void testUpdateBeneficiaries() throws SQLException {
		Account account = repository.findByCreditCard("1234123412341234");
		account.makeContribution(MonetaryAmount.valueOf("8.00"));
		repository.updateBeneficiaries(account);
		verifyBeneficiaryTableUpdated();
	}

	private void verifyBeneficiaryTableUpdated() throws SQLException {
		String sql = "select SAVINGS from T_ACCOUNT_BENEFICIARY where NAME = ? and ACCOUNT_ID = ?";
		PreparedStatement stmt = dataSource.getConnection().prepareStatement(sql);

		// assert Annabelle has $4.00 savings now
		stmt.setString(1, "Annabelle");
		stmt.setLong(2, 0L);
		ResultSet rs = stmt.executeQuery();
		rs.next();
		assertEquals(MonetaryAmount.valueOf("4.00"), MonetaryAmount.valueOf(rs.getString(1)));

		// assert Corgan has $4.00 savings now
		stmt.setString(1, "Corgan");
		stmt.setLong(2, 0L);
		rs = stmt.executeQuery();
		rs.next();
		assertEquals(MonetaryAmount.valueOf("4.00"), MonetaryAmount.valueOf(rs.getString(1)));
	}

	private DataSource createTestDataSource() {
		return new EmbeddedDatabaseBuilder()
			.setName("rewards")
			.addScript("/rewards/testdb/schema.sql")
			.addScript("/rewards/testdb/test-data.sql")
			.build();
	}
}
