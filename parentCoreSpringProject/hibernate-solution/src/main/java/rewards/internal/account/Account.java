package rewards.internal.account;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import rewards.AccountContribution;
import rewards.AccountContribution.Distribution;

import common.money.MonetaryAmount;
import common.money.Percentage;

/**
 * An account for a member of the reward network. An account has one or more beneficiaries whose allocations must add up
 * to 100%.
 * 
 * An account can make contributions to its beneficiaries. Each contribution is distributed among the beneficiaries
 * based on an allocation.
 * 
 * An entity. An aggregate (has associated beneficiaries and credit-cards).
 */
@Entity
@Table(name = "T_ACCOUNT")
public class Account {

	@Id
	@Column(name = "ID")
	private Integer entityId;

	private String number;

	private String name;

	@OneToMany
	@JoinColumn(name="ACCOUNT_ID")
	private Set<Beneficiary> beneficiaries = new HashSet<Beneficiary>();

	@OneToMany
	@JoinColumn(name="ACCOUNT_ID")
	private Set<CreditCard> creditCards = new HashSet<CreditCard>();

	@SuppressWarnings("unused")
	private Account() {
		// Required by JPA
	}

	/**
	 * Create a new account.
	 * @param number the account number
	 * @param name the name on the account
	 */
	public Account(String number, String name) {
		this.number = number;
		this.name = name;
	}

	/**
	 * Returns the entity identifier used to internally distinguish this entity
	 * among other entities of the same type in the system. Should typically
	 * only be called by privileged data access infrastructure code such as an
	 * Object Relational Mapper (ORM) and not by application code.
	 * 
	 * @return the internal entity identifier
	 */
	protected Integer getEntityId() {
		return entityId;
	}

	/**
	 * Sets the internal entity identifier - should only be called by privileged
	 * data access code (repositories that work with an Object Relational Mapper
	 * (ORM)) or unit tests. Should never be set by application code explicitly.
	 * Hence it is package local.
	 * 
	 * @param entityId
	 *            the internal entity identifier
	 */
	void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	/**
	 * Returns the number used to uniquely identify this account.
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Returns the name on file for this account.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Add a single beneficiary with a 100% allocation percentage.
	 * @param beneficiaryName the name of the beneficiary (should be unique)
	 */
	public void addBeneficiary(String beneficiaryName) {
		addBeneficiary(beneficiaryName, Percentage.oneHundred());
	}

	/**
	 * Add a single beneficiary with the specified allocation percentage.
	 * @param beneficiaryName the name of the beneficiary (should be unique)
	 * @param allocationPercentage the beneficiary's allocation percentage within this account
	 */
	public void addBeneficiary(String beneficiaryName, Percentage allocationPercentage) {
		beneficiaries.add(new Beneficiary(beneficiaryName, allocationPercentage));
	}

	/**
	 * Validation check that returns true only if the total beneficiary allocation adds up to 100%.
	 */
	public boolean isValid() {
		Percentage totalPercentage = Percentage.zero();
		for (Beneficiary b : beneficiaries) {
			try {
				totalPercentage = totalPercentage.add(b.getAllocationPercentage());
			} catch (IllegalArgumentException e) {
				// total would have been over 100% - return invalid
				return false;
			}
		}
		if (totalPercentage.equals(Percentage.oneHundred())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Make a monetary contribution to this account. The contribution amount is distributed among the account's
	 * beneficiaries based on each beneficiary's allocation percentage.
	 * @param amount the total amount to contribute
	 * @param contribution the contribution summary
	 */
	public AccountContribution makeContribution(MonetaryAmount amount) {
		if (!isValid()) {
			throw new IllegalStateException(
					"Cannot make contributions to this account: it has invalid beneficiary allocations");
		}
		Set<Distribution> distributions = distribute(amount);
		return new AccountContribution(getNumber(), amount, distributions);
	}

	/**
	 * Distribute the contribution amount among this account's beneficiaries.
	 * @param amount the total contribution amount
	 * @return the individual beneficiary distributions
	 */
	private Set<Distribution> distribute(MonetaryAmount amount) {
		Set<Distribution> distributions = new HashSet<Distribution>(beneficiaries.size());
		for (Beneficiary beneficiary : beneficiaries) {
			MonetaryAmount distributionAmount = amount.multiplyBy(beneficiary.getAllocationPercentage());
			beneficiary.credit(distributionAmount);
			Distribution distribution = new Distribution(beneficiary.getName(), distributionAmount, beneficiary
					.getAllocationPercentage(), beneficiary.getSavings());
			distributions.add(distribution);
		}
		return distributions;
	}

	/**
	 * Returns the beneficiaries for this account. Callers should not attempt to hold on or modify the returned set.
	 * This method should only be used transitively; for example, called to facilitate account reporting.
	 * @return the beneficiaries of this account
	 */
	public Set<Beneficiary> getBeneficiaries() {
		return Collections.unmodifiableSet(beneficiaries);
	}

	/**
	 * Returns a single account beneficiary. Callers should not attempt to hold on or modify the returned object. This
	 * method should only be used transitively; for example, called to facilitate reporting or testing.
	 * @param name the name of the beneficiary e.g "Annabelle"
	 * @return the beneficiary object
	 */
	public Beneficiary getBeneficiary(String name) {
		for (Beneficiary b : beneficiaries) {
			if (b.getName().equals(name)) {
				return b;
			}
		}
		throw new IllegalArgumentException("No such beneficiary with name '" + name + "'");
	}

	/**
	 * Get the credit cards owned by this account.
	 * 
	 * @return A set of cards - this is a copy, so changes to the set returned
	 *         won't affect the account.
	 */
	public Set<CreditCard> getCreditCards() {
		return Collections.unmodifiableSet(creditCards);
	}
	
	/**
	 * Used to restore an allocated beneficiary. Should only be called by the repository responsible for reconstituting
	 * this account.
	 * @param beneficiary the beneficiary
	 */
	void restoreBeneficiary(Beneficiary beneficiary) {
		beneficiaries.add(beneficiary);
	}

	public String toString() {
		return "Number = '" + number + "', name = " + name + "', beneficiaries = " + beneficiaries;
	}
}