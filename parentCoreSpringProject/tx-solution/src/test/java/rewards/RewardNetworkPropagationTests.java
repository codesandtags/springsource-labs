package rewards;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * A system test that demonstrates how propagation settings affect transactional execution.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/rewards/system-test-config-requires-new.xml")
public class RewardNetworkPropagationTests {

	/**
	 * The object being tested.
	 */
	@Autowired
	private RewardNetwork rewardNetwork;

	/**
	 * A template to use for test verification
	 */
	private JdbcTemplate template;

	/**
	 * Manages transaction manually
	 */
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	public void initJdbcTemplate(DataSource dataSource) {
		this.template = new JdbcTemplate(dataSource);
	}

	@Test
	public void testPropagation() {
		// Open a transaction for testing
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		Dining dining = Dining.createDining("100.00", "1234123412341234", "1234567890");
		rewardNetwork.rewardAccountFor(dining);

		// Rollback the transaction test transaction
		transactionManager.rollback(status);

		String sql = "select SAVINGS from T_ACCOUNT_BENEFICIARY where NAME = ?";
		assertEquals(Double.valueOf(4.00), template.queryForObject(sql, Double.class, "Annabelle"));
		assertEquals(Double.valueOf(4.00), template.queryForObject(sql, Double.class, "Corgan"));
	}
}